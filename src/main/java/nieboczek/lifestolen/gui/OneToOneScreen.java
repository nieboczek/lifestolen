package nieboczek.lifestolen.gui;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import net.minecraft.network.chat.Component;

public abstract class OneToOneScreen extends Screen {
    private CachedOrthoProjectionMatrixBuffer projectionBuffer;
    private MouseButtonEvent lastUnscaledEvent;
    private double lastMouseX;
    private double lastMouseY;
    private double lastHorizontalAmount;
    private double lastVerticalAmount;

    protected OneToOneScreen(Component component) {
        super(component);
    }

    @Override
    protected void init() {
        Window window = minecraft.getWindow();
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

        Window window = minecraft.getWindow();
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

        int scaledMouseX = mouseX * guiScale;
        int scaledMouseY = mouseY * guiScale;

        renderOneToOne(graphics, scaledMouseX, scaledMouseY, partialTick);

        graphics.pose().popMatrix();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouse, boolean isDoubleClick) {
        return mouseClickedOneToOne(scaleMouseEvent(mouse), isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent mouse, double mouseX, double mouseY) {
        int guiScale = minecraft.getWindow().getGuiScale();
        double scaledMouseX = mouseX * guiScale;
        double scaledMouseY = mouseY * guiScale;

        lastMouseX = mouseX;
        lastMouseY = mouseY;

        return mouseDraggedOneToOne(scaleMouseEvent(mouse), scaledMouseX, scaledMouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int guiScale = minecraft.getWindow().getGuiScale();
        double scaledMouseX = mouseX * guiScale;
        double scaledMouseY = mouseY * guiScale;
        double scaledHorizontalAmount = horizontalAmount * guiScale;
        double scaledVerticalAmount = verticalAmount * guiScale;

        lastMouseX = mouseX;
        lastMouseY = mouseY;
        lastHorizontalAmount = horizontalAmount;
        lastVerticalAmount = verticalAmount;

        return mouseScrolledOneToOne(scaledMouseX, scaledMouseY, scaledHorizontalAmount, scaledVerticalAmount);
    }


    protected abstract void renderOneToOne(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);

    protected boolean mouseClickedOneToOne(MouseButtonEvent event, boolean isDoubleClick) {
        boolean bl = super.mouseClicked(lastUnscaledEvent, isDoubleClick);
        lastUnscaledEvent = null;
        return bl;
    }

    protected boolean mouseDraggedOneToOne(MouseButtonEvent event, double mouseX, double mouseY) {
        boolean bl = super.mouseDragged(lastUnscaledEvent, lastMouseX, lastMouseY);
        lastUnscaledEvent = null;
        return bl;
    }

    protected boolean mouseScrolledOneToOne(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return super.mouseScrolled(lastMouseX, lastMouseY, lastHorizontalAmount, lastVerticalAmount);
    }


    private MouseButtonEvent scaleMouseEvent(MouseButtonEvent event) {
        lastUnscaledEvent = event;
        int guiScale = minecraft.getWindow().getGuiScale();
        return new MouseButtonEvent(event.x() * guiScale, event.y() * guiScale, event.buttonInfo());
    }

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
