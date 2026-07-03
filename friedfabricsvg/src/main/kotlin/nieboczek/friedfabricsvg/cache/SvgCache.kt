package nieboczek.friedfabricsvg.cache

import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

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

    fun remove(key: SvgCacheKey): SvgCacheEntry? {
        accessOrder.remove(key)
        return entries.remove(key)
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

    fun containsKey(key: SvgCacheKey): Boolean = entries.containsKey(key)

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

    fun entries(): Collection<SvgCacheEntry> = entries.values

    fun size(): Int = entries.size
}
