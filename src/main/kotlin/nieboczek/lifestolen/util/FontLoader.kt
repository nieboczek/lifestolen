package nieboczek.lifestolen.util

import com.mojang.blaze3d.font.GlyphBitmap
import com.mojang.blaze3d.font.GlyphProvider
import com.mojang.blaze3d.font.TrueTypeGlyphProvider
import com.mojang.blaze3d.font.UnbakedGlyph
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.platform.TextureUtil
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.GpuTexture
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.font.FontOption
import net.minecraft.client.gui.font.FontSet
import net.minecraft.client.gui.font.GlyphStitcher
import net.minecraft.client.gui.font.glyphs.BakedGlyph
import net.minecraft.client.gui.font.providers.FreeTypeUtil
import net.minecraft.network.chat.FontDescription
import nieboczek.lifestolen.Lifestolen
import org.lwjgl.system.MemoryStack
import org.lwjgl.util.freetype.FT_Face
import org.lwjgl.util.freetype.FreeType
import java.io.IOException
import java.lang.AutoCloseable

object FontLoader {
    fun loadUiFont(): Font {
        val manager = Minecraft.getInstance().resourceManager
        val fontIdentifier = Lifestolen.identifier("fonts/pt-root-ui.medium.ttf")

        try {
            manager.open(fontIdentifier).use { inputStream ->
                MemoryStack.stackPush().use { stack ->
                    val buf = TextureUtil.readResource(inputStream)
                    val pb = stack.mallocPointer(1)

                    FreeTypeUtil.assertError(
                        FreeType.FT_New_Memory_Face(FreeTypeUtil.getLibrary(), buf, 0, pb),
                        "Loading font"
                    )
                    val face = FT_Face.create(pb.get())

                    val oversample = 8f
                    val provider = TrueTypeGlyphProvider(buf, face, 16f, oversample, 0f, 4f, "")
                    val coloredProvider = ColoredGlyphProvider(provider, face, oversample)
                    return Font(UIFontProvider(coloredProvider))
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private class UIFontProvider(provider: GlyphProvider) : Font.Provider, AutoCloseable {
        private val set =
            FontSet(GlyphStitcher(Minecraft.getInstance().textureManager, Lifestolen.identifier("ui_font")))

        init {
            set.reload(
                listOf(GlyphProvider.Conditional(provider, FontOption.Filter.ALWAYS_PASS)),
                setOf(FontOption.UNIFORM)
            )
        }

        override fun glyphs(description: FontDescription) = set.source(false)
        override fun effect() = set.whiteGlyph()
        override fun close() = set.close()
    }

    private class ColoredGlyphProvider(
        private val delegate: GlyphProvider,
        private val face: FT_Face,
        private val oversample: Float,
    ) : GlyphProvider {
        override fun getGlyph(codepoint: Int): UnbakedGlyph? {
            val original = delegate.getGlyph(codepoint) ?: return null
            return ColoredUnbakedGlyph(original, face, codepoint, oversample)
        }

        override fun getSupportedGlyphs() = delegate.supportedGlyphs
        override fun close() = delegate.close()
    }

    private class ColoredUnbakedGlyph(
        private val delegate: UnbakedGlyph,
        private val face: FT_Face,
        private val codepoint: Int,
        private val oversample: Float,
    ) : UnbakedGlyph {
        override fun info() = delegate.info()

        override fun bake(stitcher: UnbakedGlyph.Stitcher): BakedGlyph {
            synchronized(face) {
                val glyphIndex = FreeType.FT_Get_Char_Index(face, codepoint.toLong())
                if (glyphIndex == 0) return stitcher.missing

                FreeTypeUtil.assertError(
                    FreeType.FT_Load_Glyph(face, glyphIndex, FreeType.FT_LOAD_BITMAP_METRICS_ONLY or FreeType.FT_LOAD_NO_BITMAP),
                    "Loading glyph metrics U+$codepoint"
                )
                val slot = face.glyph()!!
                val ftBitmap = slot.bitmap()
                val width = ftBitmap.width()
                val height = ftBitmap.rows()

                if (width <= 0 || height <= 0) return stitcher.missing

                val bearingX = slot.bitmap_left().toFloat() / oversample
                val bearingY = slot.bitmap_top().toFloat() / oversample

                val bitmap = ColoredGlyphBitmap(face, glyphIndex, width, height, bearingX, bearingY, oversample)
                return stitcher.stitch(info(), bitmap)
            }
        }
    }

    private class ColoredGlyphBitmap(
        private val face: FT_Face,
        private val glyphIndex: Int,
        private val width: Int,
        private val height: Int,
        private val bearingLeft: Float,
        private val bearingTop: Float,
        private val oversample: Float,
    ) : GlyphBitmap {
        override fun getPixelWidth() = width
        override fun getPixelHeight() = height
        override fun getOversample() = oversample
        override fun getBearingLeft() = bearingLeft
        override fun getBearingTop() = bearingTop
        override fun isColored() = true

        override fun upload(x: Int, y: Int, texture: GpuTexture) {
            synchronized(face) {
                FreeTypeUtil.assertError(
                    FreeType.FT_Load_Glyph(face, glyphIndex, FreeType.FT_LOAD_RENDER),
                    "Loading glyph for upload"
                )
                val slot = face.glyph()!!
                val ftBitmap = slot.bitmap()

                NativeImage(NativeImage.Format.RGBA, width, height, false).use { image ->
                    val buffer = ftBitmap.buffer(width * height)!!
                    for (row in 0 until height) {
                        for (col in 0 until width) {
                            val coverage = buffer.get(row * width + col).toInt() and 0xFF
                            image.setPixel(col, row, (coverage shl 24) or 0x00FFFFFF)
                        }
                    }
                    RenderSystem.getDevice().createCommandEncoder().writeToTexture(texture, image, 0, 0, x, y)
                }
            }
        }
    }
}
