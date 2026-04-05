package nieboczek.lifestolen.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class GuiRenderer {
    public static final GuiRenderer INSTANCE = new GuiRenderer();

    private int offset = 0;

    private GuiRenderer() {}

    public void render(GuiGraphics context) {
        Font font = Minecraft.getInstance().font;
        String text = "KupaDupa v2.1.3.7";

        // Create rainbow color using HSB
        float hue = (offset % 360) / 360f;
        int color = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);

        offset += 2; // Speed of color cycling

        context.drawString(font, text, 4, 4, color, true);
    }
}