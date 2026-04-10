package nieboczek.lifestolen.gui;

import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.TrueTypeGlyphProvider;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.GlyphStitcher;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.client.gui.font.providers.FreeTypeUtil;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import nieboczek.lifestolen.Lifestolen;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FreeType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

public final class FontLoader {
    private FontLoader() {
    }

    public static Font loadUiFont() {
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        Identifier fontIdentifier = Lifestolen.id("ui_font.ttf");

        try (
                InputStream is = manager.open(fontIdentifier);
                MemoryStack stack = MemoryStack.stackPush();
        ) {
            ByteBuffer buf = TextureUtil.readResource(is);
            PointerBuffer pb = stack.mallocPointer(1);

            FreeTypeUtil.assertError(
                    FreeType.FT_New_Memory_Face(FreeTypeUtil.getLibrary(), buf, 0, pb),
                    "Loading font"
            );
            FT_Face face = FT_Face.create(pb.get());

            TrueTypeGlyphProvider provider = new TrueTypeGlyphProvider(buf, face, 16f, 8f, 0f, 0f, "");
            return new Font(new UIFontProvider(provider));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class UIFontProvider implements Font.Provider, AutoCloseable {
        private final FontSet set = new FontSet(new GlyphStitcher(Minecraft.getInstance().getTextureManager(), Lifestolen.id("ui_font")));

        private UIFontProvider(GlyphProvider provider) {
            set.reload(List.of(new GlyphProvider.Conditional(provider, FontOption.Filter.ALWAYS_PASS)), Set.of(FontOption.UNIFORM));
        }

        @Override
        public GlyphSource glyphs(FontDescription description) {
            return set.source(false);
        }

        @Override
        public EffectGlyph effect() {
            return set.whiteGlyph();
        }

        @Override
        public void close() {
            set.close();
        }
    }
}
