package nieboczek.friedfabricsvg.parse

import com.github.weisj.jsvg.SVGDocument
import com.github.weisj.jsvg.parser.LoaderContext
import com.github.weisj.jsvg.parser.SVGLoader
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import nieboczek.friedfabricsvg.api.SvgHandle
import nieboczek.friedfabricsvg.api.SvgSource
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.security.MessageDigest

object SvgParser {
    private val log = LoggerFactory.getLogger("friedfabricsvg/SvgParser")
    private val digestThreadLocal = ThreadLocal.withInitial { MessageDigest.getInstance("SHA-256") }
    private val loaderContext = LoaderContext.createDefault()

    fun parse(source: SvgSource): SvgHandle {
        val (bytes, uriString) = readBytes(source) ?: return invalidHandle()

        val hash = digestThreadLocal.get().digest(bytes)
        val hashHex = hash.joinToString("") { "%02x".format(it) }

        val existing = SvgDocumentCache.get(hashHex)
        if (existing != null) {
            return SvgHandle(hash, existing)
        }

        val loader = SVGLoader()
        val document = try {
            ByteArrayInputStream(bytes).use { stream ->
                loader.load(stream, uriString?.let { java.net.URI(it) }, loaderContext)
            }
        } catch (e: Exception) {
            log.warn("Failed to parse SVG", e)
            null
        }

        if (document == null) {
            log.warn("SVG parsing returned null (invalid or empty SVG)")
            return invalidHandle()
        }

        SvgDocumentCache.put(hashHex, document)
        return SvgHandle(hash, document)
    }

    private fun readBytes(source: SvgSource): Pair<ByteArray, String?>? {
        return try {
            when (source) {
                is SvgSource.Resource -> {
                    val id = Identifier.fromNamespaceAndPath(source.namespace, source.path)
                    val resource = Minecraft.getInstance().resourceManager.getResource(id)
                    val bytes = resource.orElseThrow {
                        Exception("Resource not found: $id")
                    }.open().readAllBytes()
                    bytes to "resource:$id"
                }
                is SvgSource.StringContent -> {
                    source.xml.toByteArray(StandardCharsets.UTF_8) to "<string>"
                }
                is SvgSource.Stream -> {
                    source.stream.use { it.readAllBytes() } to "<stream>"
                }
                is SvgSource.FilePath -> {
                    Files.readAllBytes(source.path) to source.path.toUri().toString()
                }
            }
        } catch (e: Exception) {
            log.warn("Failed to read SVG source: ${e.message}")
            null
        }
    }

    private fun invalidHandle(): SvgHandle = SvgHandle(ByteArray(0), null)
}
