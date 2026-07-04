package nieboczek.friedfabricsvg.render

import com.github.weisj.jsvg.SVGDocument
import com.github.weisj.jsvg.view.ViewBox
import com.mojang.blaze3d.platform.NativeImage
import nieboczek.friedfabricsvg.FriedFabricSvg
import nieboczek.friedfabricsvg.api.RenderMode
import nieboczek.friedfabricsvg.api.SvgRenderOptions
import java.awt.RenderingHints
import java.awt.image.BufferedImage

object SvgRasterizer {
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
            convertBufferedToNative(image, nativeImage)
            nativeImage
        } catch (e: Exception) {
            FriedFabricSvg.log.warn("Failed to rasterize SVG at ${width}x${height}", e)
            null
        }
    }

    fun convertBufferedToNative(source: BufferedImage, target: NativeImage) {
        val w = source.width
        val h = source.height
        val pixels = IntArray(w * h)
        source.getRGB(0, 0, w, h, pixels, 0, w)
        var i = 0
        for (y in 0 until h) {
            for (x in 0 until w) {
                target.setPixel(x, y, pixels[i])
                i++
            }
        }
    }
}
