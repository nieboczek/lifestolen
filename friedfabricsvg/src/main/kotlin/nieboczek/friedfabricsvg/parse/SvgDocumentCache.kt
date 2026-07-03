package nieboczek.friedfabricsvg.parse

import com.github.weisj.jsvg.SVGDocument
import java.util.concurrent.ConcurrentHashMap

object SvgDocumentCache {
    private val cache = ConcurrentHashMap<String, SVGDocument>()

    fun get(hashHex: String): SVGDocument? = cache[hashHex]

    fun put(hashHex: String, document: SVGDocument) {
        cache[hashHex] = document
    }

    fun clear() {
        cache.clear()
    }
}
