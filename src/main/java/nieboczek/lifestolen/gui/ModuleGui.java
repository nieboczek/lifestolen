package nieboczek.lifestolen.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import nieboczek.lifestolen.Lifestolen;
import nieboczek.lifestolen.config.ConfigManager;
import nieboczek.lifestolen.module.Module;
import org.joml.Matrix3x2f;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class ModuleGui extends OneToOneScreen {
    private final List<Module<?>> modules = Lifestolen.modules;
    private boolean awaitingBind = false;
    private boolean ignoreNextOpenGuiToggle = false;

    public ModuleGui() {
        super(Component.literal("Lifestolen"));
    }

    @Override
    public void renderOneToOne(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xC0090A0F);

        // render here
    }

    @Override
    protected void rebuildWidgets() {
        clearWidgets();
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (awaitingBind && !modules.isEmpty()) {
            int keycode = keyEvent.key();
            Module<?> selectedModule = null /* replace with member of class or value here */;

            if (keycode == GLFW.GLFW_KEY_ESCAPE) {
                awaitingBind = false;
                return true;
            }

            if (keycode == GLFW.GLFW_KEY_BACKSPACE || keycode == GLFW.GLFW_KEY_DELETE) {
                selectedModule.keybind = 0;
            } else {
                selectedModule.keybind = keycode;
            }

            ignoreNextOpenGuiToggle = true;
            awaitingBind = false;
            ConfigManager.saveConfig();
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    public boolean isAwaitingBind() {
        return awaitingBind;
    }

    public boolean consumeOpenGuiToggleGuard() {
        boolean guarded = ignoreNextOpenGuiToggle;
        ignoreNextOpenGuiToggle = false;
        return guarded;
    }

    private void drawWrappedString(GuiGraphics graphics, String text, int x, int y, int maxWidth, int color) {
        int lineHeight = 11;
        int offset = 0;

        for (FormattedCharSequence line : font.split(Component.literal(text), maxWidth)) {
            graphics.drawString(font, line, x, y + offset, color, false);
            offset += lineHeight;
        }
    }

    private int brighten(int color) {
        int alpha = color & 0xFF000000;
        int red = Math.min(((color >> 16) & 0xFF) + 24, 255);
        int green = Math.min(((color >> 8) & 0xFF) + 24, 255);
        int blue = Math.min((color & 0xFF) + 24, 255);
        return alpha | (red << 16) | (green << 8) | blue;
    }

    private void renderRoundedBox(GuiGraphics graphics, int x0, int y0, int x1, int y1, float radius, int backgroundColor, int borderColor) {
        RenderPipeline pipeline = RenderPipelines.GUI;
        Matrix3x2f pose = new Matrix3x2f(graphics.pose());
        ScreenRectangle scissor = graphics.scissorStack.peek();

        graphics.guiRenderState.submitGuiElement(
                new RoundedBoxRenderState(pipeline, TextureSetup.noTexture(), pose, x0, y0, x1, y1, radius, backgroundColor, borderColor, scissor)
        );
    }

    @Override
    public void onClose() {
        awaitingBind = false;
        ignoreNextOpenGuiToggle = false;
        super.onClose();
    }
}
