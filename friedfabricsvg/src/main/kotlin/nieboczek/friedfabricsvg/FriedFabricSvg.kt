package nieboczek.friedfabricsvg

import com.mojang.blaze3d.platform.NativeImage
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.ReloadableResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import nieboczek.friedfabricsvg.api.SvgHandle
import nieboczek.friedfabricsvg.api.SvgRenderOptions
import nieboczek.friedfabricsvg.api.SvgSource
import nieboczek.friedfabricsvg.cache.SvgCache
import nieboczek.friedfabricsvg.cache.SvgCacheEntry
import nieboczek.friedfabricsvg.cache.SvgCacheKey
import nieboczek.friedfabricsvg.parse.SvgDocumentCache
import nieboczek.friedfabricsvg.parse.SvgParser
import nieboczek.friedfabricsvg.render.SvgRasterizer
import nieboczek.friedfabricsvg.texture.SvgTextureAllocator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@Environment(EnvType.CLIENT)
object FriedFabricSvg : ClientModInitializer {
    const val MOD_ID = "friedfabricsvg"

    val log: Logger = LoggerFactory.getLogger("FriedFabricSvg")
    private val cache = SvgCache(512)
    private val worker = Executors.newSingleThreadExecutor { r ->
        Thread(r, "$MOD_ID-worker").also { it.isDaemon = true }
    }

    private val isOnRenderThread: Boolean get() = Thread.currentThread() == Minecraft.getInstance().runningThread


    override fun onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register {
            val mc = Minecraft.getInstance()
            mc.execute { SvgTextureAllocator.getOrCreateErrorTexture() }

            val resourceManager = mc.resourceManager
            if (resourceManager is ReloadableResourceManager) {
                resourceManager.registerReloadListener(
                    ResourceManagerReloadListener {
                        invalidateAll()
                        SvgDocumentCache.clear()
                    }
                )
            }
        }
        ClientLifecycleEvents.CLIENT_STOPPING.register {
            SvgDocumentCache.clear()
            shutdown()
        }
    }

    fun loadSvg(source: SvgSource): SvgHandle = SvgParser.parse(source)

    fun getOrCreateTexture(
        handle: SvgHandle,
        width: Int,
        height: Int,
        options: SvgRenderOptions = SvgRenderOptions.DEFAULT
    ): Identifier {
        if (!handle.isValid) return errorTexture()

        val pixelW = if (width < 1) 1 else width
        val pixelH = if (height < 1) 1 else height
        val key = SvgCacheKey(handle, pixelW, pixelH, options)

        val cached = cache.get(key)
        if (cached != null) return cached.identifier

        if (isOnRenderThread) {
            log.warn(
                "getOrCreateTexture called from render thread with uncached SVG. " +
                        "Use getTextureAsync() to avoid freezing. Returning fallback."
            )
            scheduleAsync(handle, pixelW, pixelH, options, key)
            return errorTexture()
        }

        return rasterizeSync(handle, pixelW, pixelH, options, key)
    }

    fun getTextureAsync(
        handle: SvgHandle,
        width: Int,
        height: Int,
        options: SvgRenderOptions = SvgRenderOptions.DEFAULT
    ): CompletableFuture<Identifier> {
        if (!handle.isValid) return CompletableFuture.completedFuture(errorTexture())

        val pixelW = if (width < 1) 1 else width
        val pixelH = if (height < 1) 1 else height
        val key = SvgCacheKey(handle, pixelW, pixelH, options)

        val cached = cache.get(key)
        if (cached != null) return CompletableFuture.completedFuture(cached.identifier)

        val future = CompletableFuture<Identifier>()
        submitAsyncWork(handle, pixelW, pixelH, options, key, future)
        return future
    }

    fun invalidate(handle: SvgHandle) {
        val keysToRemove = cache.entries()
            .filter { it.key.handle == handle }
            .map { it.key }
        keysToRemove.forEach { cache.remove(it) }
    }

    fun invalidateAll() {
        cache.clear()
    }

    private fun shutdown() {
        worker.shutdownNow()
        cache.clear()
    }

    private fun errorTexture(): Identifier = SvgTextureAllocator.getOrCreateErrorTexture()

    private fun rasterizeSync(
        handle: SvgHandle,
        width: Int,
        height: Int,
        options: SvgRenderOptions,
        key: SvgCacheKey
    ): Identifier {
        val doc = handle.document ?: return errorTexture()
        val latch = CountDownLatch(1)
        var result: Identifier = errorTexture()

        worker.submit {
            try {
                val nativeImage = SvgRasterizer.rasterize(doc, width, height, options)
                if (nativeImage != null) {
                    submitUpload(key, nativeImage) { id -> result = id; latch.countDown() }
                } else {
                    latch.countDown()
                }
            } catch (e: Exception) {
                log.warn("Async rasterization failed", e)
                latch.countDown()
            }
        }

        latch.await()
        return result
    }

    private fun scheduleAsync(
        handle: SvgHandle,
        width: Int,
        height: Int,
        options: SvgRenderOptions,
        key: SvgCacheKey
    ) {
        val doc = handle.document ?: return
        worker.submit {
            try {
                val nativeImage = SvgRasterizer.rasterize(doc, width, height, options)
                if (nativeImage != null) {
                    Minecraft.getInstance().execute {
                        submitUploadAndCache(key, nativeImage)
                    }
                }
            } catch (e: Exception) {
                log.warn("Async rasterization failed", e)
            }
        }
    }

    private fun submitAsyncWork(
        handle: SvgHandle,
        width: Int,
        height: Int,
        options: SvgRenderOptions,
        key: SvgCacheKey,
        future: CompletableFuture<Identifier>
    ) {
        val doc = handle.document ?: run {
            future.complete(errorTexture())
            return
        }

        worker.submit {
            try {
                val nativeImage = SvgRasterizer.rasterize(doc, width, height, options)
                if (nativeImage != null) {
                    Minecraft.getInstance().execute {
                        val id = submitUploadAndCache(key, nativeImage)
                        future.complete(id)
                    }
                } else {
                    future.complete(errorTexture())
                }
            } catch (e: Exception) {
                log.warn("Async rasterization failed", e)
                future.complete(errorTexture())
            }
        }
    }

    private fun submitUpload(key: SvgCacheKey, nativeImage: NativeImage, onComplete: (Identifier) -> Unit) {
        val id = Identifier.fromNamespaceAndPath(MOD_ID, key.identifierPath)
        Minecraft.getInstance().execute {
            SvgTextureAllocator.upload(id, { "$MOD_ID/${key.identifierPath}" }, nativeImage)
            val tex = Minecraft.getInstance().textureManager.getTexture(id) as? DynamicTexture ?: return@execute
            cache.put(key, SvgCacheEntry(key, id, tex, System.nanoTime()))
            onComplete(id)
        }
    }

    private fun submitUploadAndCache(key: SvgCacheKey, nativeImage: NativeImage): Identifier {
        val id = Identifier.fromNamespaceAndPath(MOD_ID, key.identifierPath)
        SvgTextureAllocator.upload(id, { "$MOD_ID/${key.identifierPath}" }, nativeImage)
        val texture = Minecraft.getInstance().textureManager.getTexture(id) as? DynamicTexture
        if (texture != null) {
            cache.put(key, SvgCacheEntry(key, id, texture, System.nanoTime()))
        }
        return id
    }
}
