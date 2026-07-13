package nieboczek.lifestolen.gui.friedsvg

import com.github.weisj.jsvg.SVGDocument

class SvgHandle internal constructor(
    private val contentHash: ByteArray,
    internal val document: SVGDocument?
) {
    internal val isValid: Boolean get() = document != null

    internal fun hashHex(): String = contentHash.joinToString("") { "%02x".format(it) }
    internal fun hashShortHex(): String = hashHex().take(16)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SvgHandle) return false
        return contentHash.contentEquals(other.contentHash)
    }

    override fun hashCode(): Int = contentHash.contentHashCode()
}
