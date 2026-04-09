package nieboczek.lifestolen.gui;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import net.minecraft.network.chat.Component;

public abstract class OneToOneScreen extends Screen {
    private CachedOrthoProjectionMatrixBuffer projectionBuffer;

    protected OneToOneScreen(Component component) {
        super(component);
    }

    @Override
    protected void init() {
        Window window = Minecraft.getInstance().getWindow();
        width = window.getWidth();
        height = window.getHeight();
    }

    @Override
    public void resize(int i, int j) {
        rebuildWidgets();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (projectionBuffer == null) {
            projectionBuffer = new CachedOrthoProjectionMatrixBuffer("lifestolen_gui", 1000.0F, 11000.0F, true);
        }

        Window window = Minecraft.getInstance().getWindow();
        int guiScale = window.getGuiScale();
        
        width = window.getWidth();
        height = window.getHeight();

        for (Renderable renderable : renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }
        
        graphics.renderDeferredElements();

        graphics.pose().pushMatrix();
        graphics.pose().scale(1.0f / guiScale, 1.0f / guiScale);

        RenderSystem.setProjectionMatrix(
                projectionBuffer.getBuffer((float) width / guiScale, (float) height / guiScale),
                ProjectionType.ORTHOGRAPHIC
        );

        renderOneToOne(graphics, mouseX, mouseY, partialTick);

        graphics.pose().popMatrix();
    }

    protected abstract void renderOneToOne(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);

    @Override
    public void onClose() {
        projectionBuffer.close();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
