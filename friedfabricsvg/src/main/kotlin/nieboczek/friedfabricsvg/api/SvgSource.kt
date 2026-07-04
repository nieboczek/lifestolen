package nieboczek.friedfabricsvg.api

import net.minecraft.resources.Identifier
import java.io.InputStream
import java.nio.file.Path

sealed class SvgSource {
    data class Resource(val identifier: Identifier) : SvgSource()
    data class StringContent(val xml: String) : SvgSource()
    data class Stream(val stream: InputStream) : SvgSource()
    data class FilePath(val path: Path) : SvgSource()
}
