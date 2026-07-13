package nieboczek.lifestolen.gui.friedsvg

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.resources.Identifier
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

class SvgCacheKey(
    val handle: SvgHandle, val pixelWidth: Int, val pixelHeight: Int, val options: SvgRenderOptions
) {
    fun createIdentifier() =
        "svg/${handle.hashShortHex()}/${pixelWidth}x${pixelHeight}_${options.hashCode().toUInt().toString(16)}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SvgCacheKey) return false
        return handle == other.handle && pixelWidth == other.pixelWidth && pixelHeight == other.pixelHeight && options == other.options
    }

    override fun hashCode() = Objects.hash(handle, pixelWidth, pixelHeight, options)
}

class SvgCacheEntry(
    val key: SvgCacheKey, val identifier: Identifier, val texture: DynamicTexture, var lastAccessNanos: Long
)

class SvgCache(private val maxEntries: Int) {
    private val entries = ConcurrentHashMap<SvgCacheKey, SvgCacheEntry>()
    private val accessOrder = ConcurrentLinkedDeque<SvgCacheKey>()

    fun get(key: SvgCacheKey): SvgCacheEntry? {
        val entry = entries[key] ?: return null
        touch(key)
        return entry
    }

    fun put(key: SvgCacheKey, entry: SvgCacheEntry) {
        entries[key] = entry
        accessOrder.addLast(key)
        evictIfNeeded()
    }

    fun clear() {
        val iter = entries.values.iterator()
        while (iter.hasNext()) {
            val entry = iter.next()
            iter.remove()
            accessOrder.remove(entry.key)
            entry.texture.close()
        }
        accessOrder.clear()
    }

    private fun touch(key: SvgCacheKey) {
        accessOrder.remove(key)
        accessOrder.addLast(key)
        entries[key]?.let { it.lastAccessNanos = System.nanoTime() }
    }

    private fun evictIfNeeded() {
        while (entries.size > maxEntries) {
            val oldest = accessOrder.pollFirst() ?: break
            val removed = entries.remove(oldest) ?: continue
            Minecraft.getInstance().textureManager.release(removed.identifier)
        }
    }
}
