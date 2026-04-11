package nieboczek.lifestolen.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import nieboczek.lifestolen.Lifestolen;
import nieboczek.lifestolen.config.ConfigManager;
import nieboczek.lifestolen.module.Module;
import org.joml.Matrix3x2f;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModuleGui extends OneToOneScreen {
    private final List<Module<?>> modules = Lifestolen.modules;
    private boolean awaitingBind = false;
    private boolean ignoreNextOpenGuiToggle = false;
    private Module<?> selectedModuleForBind = null;
    private Map<Module.Category, List<Module<?>>> categories = new LinkedHashMap<>();

    private static final int SLIDER_WIDTH = 160;
    private static final int SLIDER_HEIGHT = 32;
    private static final float SLIDER_MIN = 1f;
    private static final float SLIDER_MAX = 4f;

    private boolean draggingSlider = false;
    private float textScale;

    public ModuleGui() {
        super(Component.literal(Lifestolen.CLIENT_NAME), Lifestolen.uiFont);
        textScale = Lifestolen.cfg.textScale;
    }

    @Override
    public void renderOneToOne(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xC0090A0F);

        // render here

        renderTextScaleSlider(graphics, mouseX, mouseY);
    }

    @Override
    protected void rebuildWidgets() {
        clearWidgets();
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (awaitingBind && selectedModuleForBind != null) {
            int keycode = keyEvent.key();

            if (keycode == GLFW.GLFW_KEY_ESCAPE) {
                awaitingBind = false;
                selectedModuleForBind = null;
                return true;
            }

            if (keycode == GLFW.GLFW_KEY_BACKSPACE || keycode == GLFW.GLFW_KEY_DELETE) {
                selectedModuleForBind.keybind = 0;
            } else {
                selectedModuleForBind.keybind = keycode;
            }

            ignoreNextOpenGuiToggle = true;
            awaitingBind = false;
            selectedModuleForBind = null;
            ConfigManager.saveConfig();
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    @Override
    public boolean mouseClickedOneToOne(MouseButtonEvent mouse, boolean isDoubleClick) {
        int sliderX = width - SLIDER_WIDTH - 8;
        int sliderY = height - SLIDER_HEIGHT - 8;

        if (mouse.button() == 0 && mouse.x() >= sliderX && mouse.x() < sliderX + SLIDER_WIDTH && mouse.y() >= sliderY && mouse.y() < sliderY + SLIDER_HEIGHT) {
            float ratio = (float) (mouse.x() - sliderX) / SLIDER_WIDTH;
            ratio = Math.clamp(ratio, 0f, 1f);

            textScale = SLIDER_MIN + ratio * (SLIDER_MAX - SLIDER_MIN);
            textScale = (textScale / 25) * 25;
            draggingSlider = true;

            Lifestolen.cfg.textScale = textScale;
            ConfigManager.saveConfig();
            return true;
        }

        return super.mouseClickedOneToOne(mouse, isDoubleClick);
    }

    @Override
    public boolean mouseDraggedOneToOne(MouseButtonEvent mouse, double draggedDistanceX, double draggedDistanceY) {
        if (draggingSlider) {
            int sliderX = width - SLIDER_WIDTH - 8;
            float ratio = (float) (mouse.x() - sliderX) / SLIDER_WIDTH;
            ratio = Math.clamp(ratio, 0f, 1f);
            textScale = SLIDER_MIN + ratio * (SLIDER_MAX - SLIDER_MIN);
            textScale = (textScale / 25) * 25;

            Lifestolen.cfg.textScale = textScale;
            return true;
        }

        return super.mouseDraggedOneToOne(mouse, draggedDistanceX, draggedDistanceY);
    }

    @Override
    public boolean mouseReleasedOneToOne(MouseButtonEvent mouse) {
        if (draggingSlider && mouse.button() == 0) {
            draggingSlider = false;
            ConfigManager.saveConfig();
            return true;
        }
        return super.mouseReleasedOneToOne(mouse);
    }

    private void renderTextScaleSlider(GuiGraphics graphics, int mouseX, int mouseY) {
        int sliderX = width - SLIDER_WIDTH - 8;
        int sliderY = height - SLIDER_HEIGHT - 8;

        String label = "Scale: " + (int) (textScale * 100f) + "%";
        int labelWidth = (int) (font.width(label) * textScale);
        int labelY = sliderY - (int) (6.5f * textScale) + SLIDER_HEIGHT;
        drawScaledString(graphics, label, sliderX - labelWidth - 4, labelY, 0xFFAAAAAA);

        renderRoundedBox(graphics, sliderX, sliderY, SLIDER_WIDTH, SLIDER_HEIGHT, 4, 0xFF2A2A2A, 0xFF4A4A4A);

        float ratio = (textScale - SLIDER_MIN) / (SLIDER_MAX - SLIDER_MIN);
        int knobWidth = 8;
        int knobX = sliderX + (int) ((SLIDER_WIDTH - knobWidth) * ratio);
        graphics.fill(knobX, sliderY + 2, knobX + knobWidth, sliderY + SLIDER_HEIGHT - 2, 0xFF55AAFF);

        if (mouseX >= sliderX && mouseX < sliderX + SLIDER_WIDTH && mouseY >= sliderY && mouseY < sliderY + SLIDER_HEIGHT) {
            graphics.fill(sliderX, sliderY, sliderX + SLIDER_WIDTH, sliderY + SLIDER_HEIGHT, 0x20FFFFFF);
        }
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
        int lineHeight = (int) (15f * textScale);
        int offset = 0;

        for (FormattedCharSequence line : font.split(Component.literal(text), maxWidth)) {
            drawScaledString(graphics, line, x, y + offset, color);
            offset += lineHeight;
        }
    }

    public void drawScaledString(GuiGraphics graphics, String text, int x, int y, int color) {
        drawScaledString(graphics, Language.getInstance().getVisualOrder(FormattedText.of(text)), x, y, color);
    }

    public void drawScaledString(GuiGraphics graphics, FormattedCharSequence text, int x, int y, int color) {
        graphics.pose().pushMatrix();
        graphics.pose().scale(textScale, textScale);
        int scaledX = (int) (x / textScale);
        int scaledY = (int) (y / textScale);
        graphics.drawString(font, text, scaledX, scaledY, color, false);
        graphics.pose().popMatrix();
    }

    private int brighten(int color) {
        int alpha = color & 0xFF000000;
        int red = Math.min(((color >> 16) & 0xFF) + 24, 255);
        int green = Math.min(((color >> 8) & 0xFF) + 24, 255);
        int blue = Math.min((color & 0xFF) + 24, 255);
        return alpha | (red << 16) | (green << 8) | blue;
    }

    private void renderRoundedBox(GuiGraphics graphics, int x, int y, int width, int height, float radius, int backgroundColor, int borderColor) {
        graphics.guiRenderState.submitGuiElement(new RoundedBoxRenderState(
                RenderPipelines.GUI,
                TextureSetup.noTexture(),
                new Matrix3x2f(graphics.pose()),
                x, y, width, height,
                radius,
                backgroundColor, borderColor,
                graphics.scissorStack.peek()
        ));
    }

    @Override
    public void onClose() {
        awaitingBind = false;
        ignoreNextOpenGuiToggle = false;
        super.onClose();
    }
}