package nieboczek.friedfabricsvg.api

data class SvgRenderOptions(
    val antialias: Boolean = true,
    val softClipping: Boolean = true,
    val renderMode: RenderMode = RenderMode.QUALITY
) {
    companion object {
        val DEFAULT = SvgRenderOptions()
    }
}

enum class RenderMode { QUALITY, PERFORMANCE }
