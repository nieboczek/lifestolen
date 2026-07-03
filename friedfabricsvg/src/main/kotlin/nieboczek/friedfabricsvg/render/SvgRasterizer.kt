package nieboczek.friedfabricsvg.render

import com.github.weisj.jsvg.SVGDocument
import com.github.weisj.jsvg.view.ViewBox
import com.mojang.blaze3d.platform.NativeImage
import nieboczek.friedfabricsvg.api.RenderMode
import nieboczek.friedfabricsvg.api.SvgRenderOptions
import org.slf4j.LoggerFactory
import java.awt.RenderingHints
import java.awt.image.BufferedImage

object SvgRasterizer {
    private val log = LoggerFactory.getLogger("friedfabricsvg/SvgRasterizer")

    fun rasterize(
        document: SVGDocument,
        width: Int,
        height: Int,
        options: SvgRenderOptions
    ): NativeImage? {
        if (width <= 0 || height <= 0) return null

        return try {
            val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            val g = image.createGraphics()

            if (options.antialias) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            }
            if (options.renderMode == RenderMode.QUALITY) {
                g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
            }

            document.render(null, g, ViewBox(width.toFloat(), height.toFloat()))
            g.dispose()

            val nativeImage = NativeImage(NativeImage.Format.RGBA, width, height, false)
            PixelConverter.convert(image, nativeImage)
            nativeImage
        } catch (e: Exception) {
            log.warn("Failed to rasterize SVG at ${width}x${height}", e)
            null
        }
    }
}
