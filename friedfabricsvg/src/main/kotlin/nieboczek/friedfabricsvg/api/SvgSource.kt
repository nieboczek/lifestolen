@file:JvmName("SvgSource")

package nieboczek.friedfabricsvg.api

import java.io.InputStream
import java.nio.file.Path

sealed class SvgSource {
    data class Resource(val namespace: String, val path: String) : SvgSource()
    data class StringContent(val xml: String) : SvgSource()
    data class Stream(val stream: InputStream) : SvgSource()
    data class FilePath(val path: Path) : SvgSource()
}
