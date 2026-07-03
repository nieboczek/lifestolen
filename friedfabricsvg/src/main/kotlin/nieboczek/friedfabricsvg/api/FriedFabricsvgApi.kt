@file:Suppress("unused")

package nieboczek.friedfabricsvg.api

import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import nieboczek.friedfabricsvg.cache.SvgCache
import nieboczek.friedfabricsvg.cache.SvgCacheEntry
import nieboczek.friedfabricsvg.cache.SvgCacheKey
import nieboczek.friedfabricsvg.parse.SvgParser
import nieboczek.friedfabricsvg.render.SvgRasterizer
import nieboczek.friedfabricsvg.texture.SvgTextureAllocator
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.function.Supplier

object FriedFabricsvgApi {
    private val log = LoggerFactory.getLogger("friedfabricsvg/Api")
    private val config = SvgConfig()
    private val cache = SvgCache(config.maxCacheEntries)
    private val worker = Executors.newSingleThreadExecutor { r ->
        Thread(r, "friedfabricsvg-worker").also { it.isDaemon = true }
    }

    private val isOnRenderThread: Boolean get() = Thread.currentThread() == Minecraft.getInstance().runningThread

    internal fun initialize() {
        // Force error texture creation on render thread
        Minecraft.getInstance().execute {
            SvgTextureAllocator.getOrCreateErrorTexture()
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
            log.warn("getOrCreateTexture called from render thread with uncached SVG. " +
                    "Use getTextureAsync() to avoid freezing. Returning fallback.")
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

    val currentConfig: SvgConfig get() = config

    internal fun drainUploads() {
        // Uploads are dispatched immediately via Minecraft.execute()
    }

    internal fun shutdown() {
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
        val id = Identifier.fromNamespaceAndPath("friedfabricsvg", key.identifierPath)
        Minecraft.getInstance().execute {
            SvgTextureAllocator.upload(id, Supplier { "friedfabricsvg/${key.identifierPath}" }, nativeImage)
            val tex = Minecraft.getInstance().textureManager.getTexture(id) as? net.minecraft.client.renderer.texture.DynamicTexture ?: return@execute
            cache.put(key, SvgCacheEntry(key, id, tex, System.nanoTime()))
            onComplete(id)
        }
    }

    private fun submitUploadAndCache(key: SvgCacheKey, nativeImage: NativeImage): Identifier {
        val id = Identifier.fromNamespaceAndPath("friedfabricsvg", key.identifierPath)
        SvgTextureAllocator.upload(id, Supplier { "friedfabricsvg/${key.identifierPath}" }, nativeImage)
        val texture = Minecraft.getInstance().textureManager.getTexture(id) as? net.minecraft.client.renderer.texture.DynamicTexture
        if (texture != null) {
            cache.put(key, SvgCacheEntry(key, id, texture, System.nanoTime()))
        }
        return id
    }
}
