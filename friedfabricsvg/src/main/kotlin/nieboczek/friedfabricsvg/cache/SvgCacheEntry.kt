package nieboczek.friedfabricsvg.cache

import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.resources.Identifier

class SvgCacheEntry(
    val key: SvgCacheKey,
    val identifier: Identifier,
    val texture: DynamicTexture,
    var lastAccessNanos: Long
)
