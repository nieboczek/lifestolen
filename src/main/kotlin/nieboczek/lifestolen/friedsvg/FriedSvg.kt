package nieboczek.lifestolen.friedsvg

import com.mojang.blaze3d.platform.NativeImage
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.ReloadableResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import nieboczek.lifestolen.Lifestolen
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

object FriedSvg {
    val log: Logger = LoggerFactory.getLogger("FriedSvg")

    private val mc = Minecraft.getInstance()
    private val cache = SvgCache(64)
    private val worker = Executors.newSingleThreadExecutor { task ->
        Thread(task, "friedsvg-worker").also { it.isDaemon = true }
    }

    fun initialize() {
        ClientLifecycleEvents.CLIENT_STARTED.register { mc ->
            mc.execute { SvgRasterizer.getOrCreateErrorTexture() }

            val resourceManager = mc.resourceManager
            if (resourceManager is ReloadableResourceManager) {
                resourceManager.registerReloadListener(ResourceManagerReloadListener { invalidateCache() })
            }
        }
        ClientLifecycleEvents.CLIENT_STOPPING.register {
            invalidateCache()
            worker.shutdownNow()
        }
    }

    fun loadSvg(source: Identifier) = SvgParser.parse(source)

    fun invalidateCache() {
        cache.clear()
        SvgParser.clearCache()
    }

    fun getTextureAsync(
        handle: SvgHandle, width: Int, height: Int, options: SvgRenderOptions = SvgRenderOptions()
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

    private fun errorTexture() = SvgRasterizer.getOrCreateErrorTexture()

    private fun rasterizeSync(
        handle: SvgHandle, width: Int, height: Int, options: SvgRenderOptions, key: SvgCacheKey
    ): Identifier {
        val doc = handle.document ?: return errorTexture()
        val latch = CountDownLatch(1)
        var result: Identifier = errorTexture()

        worker.submit {
            try {
                val nativeImage = SvgRasterizer.rasterize(doc, width, height, options)
                if (nativeImage != null) {
                    submitUpload(key, nativeImage) {
                        result = it
                        latch.countDown()
                    }
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

    private fun scheduleAsync(handle: SvgHandle, width: Int, height: Int, options: SvgRenderOptions, key: SvgCacheKey) {
        val doc = handle.document ?: return
        worker.submit {
            try {
                val nativeImage = SvgRasterizer.rasterize(doc, width, height, options)
                if (nativeImage != null) {
                    mc.execute {
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
                    mc.execute {
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
        val id = Lifestolen.identifier(key.createIdentifier())
        mc.execute {
            SvgRasterizer.uploadTexture(id, { "${Lifestolen.MOD_ID}/${key.createIdentifier()}" }, nativeImage)

            val texture = mc.textureManager.getTexture(id) as? DynamicTexture ?: return@execute
            cache.put(key, SvgCacheEntry(key, id, texture, System.nanoTime()))
            onComplete(id)
        }
    }

    private fun submitUploadAndCache(key: SvgCacheKey, nativeImage: NativeImage): Identifier {
        val id = Lifestolen.identifier(key.createIdentifier())
        SvgRasterizer.uploadTexture(id, { "${Lifestolen.MOD_ID}/${key.createIdentifier()}" }, nativeImage)

        val texture = mc.textureManager.getTexture(id) as? DynamicTexture
        if (texture != null) cache.put(key, SvgCacheEntry(key, id, texture, System.nanoTime()))
        return id
    }
}
