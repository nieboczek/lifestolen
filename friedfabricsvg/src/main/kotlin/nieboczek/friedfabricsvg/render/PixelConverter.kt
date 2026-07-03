package nieboczek.friedfabricsvg.render

import com.mojang.blaze3d.platform.NativeImage
import java.awt.image.BufferedImage

object PixelConverter {
    fun convert(source: BufferedImage, target: NativeImage) {
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
