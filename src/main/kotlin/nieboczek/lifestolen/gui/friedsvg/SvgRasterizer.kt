package nieboczek.lifestolen.gui.friedsvg

import com.github.weisj.jsvg.SVGDocument
import com.github.weisj.jsvg.view.ViewBox
import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.resources.Identifier
import nieboczek.lifestolen.Lifestolen
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.util.function.Supplier

class SvgRenderOptions(
    val antialias: Boolean = true,
    val qualityMode: Boolean = true,
)

object SvgRasterizer {
    private val errorTexture by lazy(this::createErrorTexture)

    fun rasterize(
        document: SVGDocument, width: Int, height: Int, options: SvgRenderOptions
    ): NativeImage? {
        if (width <= 0 || height <= 0) return null
        return try {
            val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            val graphics = image.createGraphics()

            if (options.antialias) graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON
            )
            if (options.qualityMode) graphics.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE
            )

            document.render(null, graphics, ViewBox(width.toFloat(), height.toFloat()))
            graphics.dispose()

            val nativeImage = NativeImage(NativeImage.Format.RGBA, width, height, false)
            convertBufferedToNative(image, nativeImage)
            nativeImage
        } catch (e: Exception) {
            FriedSvg.log.warn("Failed to rasterize SVG at ${width}x${height}", e)
            null
        }
    }

    private fun convertBufferedToNative(source: BufferedImage, target: NativeImage) {
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

    fun getOrCreateErrorTexture(): Identifier = errorTexture

    fun uploadTexture(identifier: Identifier, label: Supplier<String>, nativeImage: NativeImage) {
        val texture = DynamicTexture(label, nativeImage)
        Minecraft.getInstance().textureManager.register(identifier, texture)
    }

    private fun createErrorTexture(): Identifier {
        val image = NativeImage(1, 1, true)
        val texture = DynamicTexture({ "${Lifestolen.MOD_ID}/error" }, image)
        val id = Identifier.fromNamespaceAndPath(Lifestolen.MOD_ID, "svg/error")
        Minecraft.getInstance().textureManager.register(id, texture)
        return id
    }
}
