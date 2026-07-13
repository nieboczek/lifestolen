package nieboczek.lifestolen.gui.friedsvg

import com.github.weisj.jsvg.SVGDocument
import com.github.weisj.jsvg.parser.LoaderContext
import com.github.weisj.jsvg.parser.SVGLoader
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import java.io.ByteArrayInputStream
import java.net.URI
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

object SvgParser {
    private val digestThreadLocal = ThreadLocal.withInitial { MessageDigest.getInstance("SHA-256") }
    private val loaderContext = LoaderContext.createDefault()
    private val cache = ConcurrentHashMap<String, SVGDocument>()

    fun putCache(hashHex: String, document: SVGDocument) {
        cache[hashHex] = document
    }

    fun getCached(hashHex: String): SVGDocument? = cache[hashHex]
    fun clearCache() = cache.clear()

    fun parse(source: Identifier): SvgHandle {
        val (bytes, uriString) = readBytes(source) ?: return invalidHandle()

        val hash = digestThreadLocal.get().digest(bytes)
        val hashHex = hash.joinToString("") { "%02x".format(it) }

        val existing = getCached(hashHex)
        if (existing != null) {
            return SvgHandle(hash, existing)
        }

        val loader = SVGLoader()
        val document = try {
            ByteArrayInputStream(bytes).use { stream ->
                loader.load(stream, uriString?.let { URI(it) }, loaderContext)
            }
        } catch (e: Exception) {
            FriedSvg.log.warn("Failed to parse SVG", e)
            null
        }

        if (document == null) {
            FriedSvg.log.warn("SVG parsing returned null (invalid or empty SVG)")
            return invalidHandle()
        }

        putCache(hashHex, document)
        return SvgHandle(hash, document)
    }

    private fun readBytes(id: Identifier): Pair<ByteArray, String?>? {
        return try {
            val resource = Minecraft.getInstance().resourceManager.getResource(id)
            val bytes = resource.orElseThrow { Exception("Resource not found: $id") }.open().readAllBytes()
            bytes to "resource:$id"
        } catch (e: Exception) {
            FriedSvg.log.warn("Failed to read SVG source: ${e.message}")
            null
        }
    }

    private fun invalidHandle(): SvgHandle = SvgHandle(ByteArray(0), null)
}