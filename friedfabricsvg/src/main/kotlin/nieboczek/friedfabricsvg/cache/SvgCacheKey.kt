package nieboczek.friedfabricsvg.cache

import nieboczek.friedfabricsvg.api.SvgHandle
import nieboczek.friedfabricsvg.api.SvgRenderOptions
import java.util.Objects

class SvgCacheKey(
    val handle: SvgHandle,
    val pixelWidth: Int,
    val pixelHeight: Int,
    val options: SvgRenderOptions
) {
    val identifierPath: String
        get() = "svg/${handle.hashShortHex()}/${pixelWidth}x${pixelHeight}_${options.hashCode().toUInt().toString(16)}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SvgCacheKey) return false
        return handle == other.handle &&
                pixelWidth == other.pixelWidth &&
                pixelHeight == other.pixelHeight &&
                options == other.options
    }

    override fun hashCode(): Int = Objects.hash(handle, pixelWidth, pixelHeight, options)
}
