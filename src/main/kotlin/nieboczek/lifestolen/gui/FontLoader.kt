package nieboczek.lifestolen.gui

import com.mojang.blaze3d.font.GlyphProvider
import com.mojang.blaze3d.font.TrueTypeGlyphProvider
import com.mojang.blaze3d.platform.TextureUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GlyphSource
import net.minecraft.client.gui.font.FontOption
import net.minecraft.client.gui.font.FontSet
import net.minecraft.client.gui.font.GlyphStitcher
import net.minecraft.client.gui.font.glyphs.EffectGlyph
import net.minecraft.client.gui.font.providers.FreeTypeUtil
import net.minecraft.network.chat.FontDescription
import nieboczek.lifestolen.Lifestolen
import org.lwjgl.system.MemoryStack
import org.lwjgl.util.freetype.FT_Face
import org.lwjgl.util.freetype.FreeType
import java.io.IOException
import java.lang.AutoCloseable
import java.nio.ByteBuffer

object FontLoader {
    fun loadUiFont(): Font {
        val manager = Minecraft.getInstance().resourceManager
        val fontIdentifier = Lifestolen.id("ui_font.ttf")

        try {
            manager.open(fontIdentifier).use { stream ->
                MemoryStack.stackPush().use { stack ->
                    val buf: ByteBuffer = TextureUtil.readResource(stream)
                    val pb = stack.mallocPointer(1)

                    FreeTypeUtil.assertError(
                        FreeType.FT_New_Memory_Face(FreeTypeUtil.getLibrary(), buf, 0, pb),
                        "Loading font"
                    )

                    val face = FT_Face.create(pb.get())
                    val provider = TrueTypeGlyphProvider(buf, face, 16f, 8f, 0f, 0f, "")
                    return Font(UIFontProvider(provider))
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private class UIFontProvider(provider: GlyphProvider) : Font.Provider, AutoCloseable {
        private val set = FontSet(GlyphStitcher(Minecraft.getInstance().textureManager, Lifestolen.id("ui_font")))

        init {
            set.reload(
                listOf(GlyphProvider.Conditional(provider, FontOption.Filter.ALWAYS_PASS)),
                setOf(FontOption.UNIFORM)
            )
        }

        override fun glyphs(description: FontDescription): GlyphSource {
            return set.source(false)
        }

        override fun effect(): EffectGlyph {
            return set.whiteGlyph()
        }

        override fun close() {
            set.close()
        }
    }
}
