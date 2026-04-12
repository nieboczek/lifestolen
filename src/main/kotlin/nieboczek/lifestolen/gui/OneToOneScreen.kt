package nieboczek.lifestolen.gui

import com.mojang.blaze3d.ProjectionType
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer
import net.minecraft.locale.Language
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import nieboczek.lifestolen.Lifestolen

abstract class OneToOneScreen protected constructor(component: Component, font: Font) :
    Screen(Minecraft.getInstance(), font, component) {
    protected var textScale = Lifestolen.cfg!!.textScale

    private var projectionBuffer: CachedOrthoProjectionMatrixBuffer? = null

    override fun init() {
        recalculateWindowDimensions()
        rebuildWidgets()
    }

    override fun resize(x: Int, y: Int) {
        recalculateWindowDimensions()
        rebuildWidgets()
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (projectionBuffer == null) {
            projectionBuffer = CachedOrthoProjectionMatrixBuffer("lifestolen_gui", 1000.0f, 11000.0f, true)
        }

        val guiScale = minecraft.window.guiScale

        graphics.pose().pushMatrix()
        graphics.pose().scale(1.0f / guiScale, 1.0f / guiScale)

        RenderSystem.setProjectionMatrix(
            projectionBuffer!!.getBuffer(width.toFloat() / guiScale, height.toFloat() / guiScale),
            ProjectionType.ORTHOGRAPHIC
        )

        val scaledMouseX = mouseX * guiScale
        val scaledMouseY = mouseY * guiScale

        renderOneToOne(graphics, scaledMouseX, scaledMouseY, partialTick)

        for (renderable in renderables) {
            renderable.render(graphics, scaledMouseX, scaledMouseY, partialTick)
        }

        graphics.pose().popMatrix()
    }

    override fun mouseClicked(mouse: MouseButtonEvent, isDoubleClick: Boolean): Boolean {
        return mouseClickedOneToOne(scaleMouseEvent(mouse), isDoubleClick)
    }

    override fun mouseDragged(mouse: MouseButtonEvent, draggedDistanceX: Double, draggedDistanceY: Double): Boolean {
        val guiScale = minecraft.window.guiScale
        val scaledDistanceX = draggedDistanceX * guiScale
        val scaledDistanceY = draggedDistanceY * guiScale

        return mouseDraggedOneToOne(scaleMouseEvent(mouse), scaledDistanceX, scaledDistanceY)
    }

    override fun mouseReleased(mouse: MouseButtonEvent): Boolean {
        return mouseReleasedOneToOne(scaleMouseEvent(mouse))
    }

    override fun mouseScrolled(
        mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double
    ): Boolean {
        val guiScale = minecraft.window.guiScale
        val scaledMouseX = mouseX * guiScale
        val scaledMouseY = mouseY * guiScale
        val scaledHorizontalAmount = horizontalAmount * guiScale
        val scaledVerticalAmount = verticalAmount * guiScale

        return mouseScrolledOneToOne(scaledMouseX, scaledMouseY, scaledHorizontalAmount, scaledVerticalAmount)
    }

    protected fun drawScaledString(graphics: GuiGraphics, text: String, x: Int, y: Int, color: Int) {
        drawScaledString(graphics, Language.getInstance().getVisualOrder(FormattedText.of(text)), x, y, color)
    }

    protected fun drawScaledString(graphics: GuiGraphics, text: FormattedCharSequence, x: Int, y: Int, color: Int) {
        graphics.pose().pushMatrix()
        graphics.pose().scale(textScale, textScale)

        val scaledX = (x / textScale).toInt()
        val scaledY = (y / textScale).toInt()

        graphics.drawString(font, text, scaledX, scaledY, color, false)
        graphics.pose().popMatrix()
    }


    protected abstract fun renderOneToOne(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float)

    protected open fun mouseClickedOneToOne(mouse: MouseButtonEvent, isDoubleClick: Boolean): Boolean {
        return super.mouseClicked(mouse, isDoubleClick)
    }

    protected open fun mouseDraggedOneToOne(
        mouse: MouseButtonEvent, draggedDistanceX: Double, draggedDistanceY: Double
    ): Boolean {
        return super.mouseDragged(mouse, draggedDistanceX, draggedDistanceY)
    }

    protected open fun mouseReleasedOneToOne(mouse: MouseButtonEvent): Boolean {
        return super.mouseReleased(mouse)
    }

    protected open fun mouseScrolledOneToOne(
        mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double
    ): Boolean {
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }


    private fun scaleMouseEvent(event: MouseButtonEvent): MouseButtonEvent {
        val guiScale = minecraft.window.guiScale
        return MouseButtonEvent(event.x() * guiScale, event.y() * guiScale, event.buttonInfo())
    }

    private fun recalculateWindowDimensions() {
        val window = minecraft.window
        width = window.width
        height = window.height
    }

    override fun onClose() {
        projectionBuffer?.close()
        super.onClose()
    }

    override fun isPauseScreen(): Boolean {
        return false
    }
}
