package nieboczek.lifestolen.gui

import com.mojang.blaze3d.ProjectionType
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer
import net.minecraft.network.chat.Component

abstract class OneToOneScreen protected constructor(component: Component, font: Font) :
    Screen(Minecraft.getInstance(), font, component) {
    private var projectionBuffer: CachedOrthoProjectionMatrixBuffer? = null
    private var lastUnscaledEvent: MouseButtonEvent? = null
    private var lastMouseX = 0.0
    private var lastMouseY = 0.0
    private var lastHorizontalAmount = 0.0
    private var lastVerticalAmount = 0.0

    override fun init() {
        val window = minecraft.window
        width = window.width
        height = window.height
    }

    override fun resize(x: Int, y: Int) {
        rebuildWidgets()
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (projectionBuffer == null) {
            projectionBuffer = CachedOrthoProjectionMatrixBuffer("lifestolen_gui", 1000.0f, 11000.0f, true)
        }

        val window = minecraft.window
        val guiScale = window.guiScale
        width = window.width
        height = window.height

        for (renderable in renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick)
        }

        graphics.renderDeferredElements()

        graphics.pose().pushMatrix()
        graphics.pose().scale(1.0f / guiScale, 1.0f / guiScale)

        RenderSystem.setProjectionMatrix(
            projectionBuffer!!.getBuffer(width.toFloat() / guiScale, height.toFloat() / guiScale),
            ProjectionType.ORTHOGRAPHIC
        )

        val scaledMouseX = mouseX * guiScale
        val scaledMouseY = mouseY * guiScale

        renderOneToOne(graphics, scaledMouseX, scaledMouseY, partialTick)

        graphics.pose().popMatrix()
    }

    override fun mouseClicked(mouse: MouseButtonEvent, isDoubleClick: Boolean): Boolean {
        return mouseClickedOneToOne(scaleMouseEvent(mouse), isDoubleClick)
    }

    override fun mouseDragged(mouse: MouseButtonEvent, draggedDistanceX: Double, draggedDistanceY: Double): Boolean {
        val guiScale = minecraft.window.guiScale
        val scaledDistanceX = draggedDistanceX * guiScale
        val scaledDistanceY = draggedDistanceY * guiScale

        lastMouseX = draggedDistanceX
        lastMouseY = draggedDistanceY

        return mouseDraggedOneToOne(scaleMouseEvent(mouse), scaledDistanceX, scaledDistanceY)
    }

    override fun mouseReleased(mouse: MouseButtonEvent): Boolean {
        return mouseReleasedOneToOne(scaleMouseEvent(mouse))
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {
        val guiScale = minecraft.window.guiScale
        val scaledMouseX = mouseX * guiScale
        val scaledMouseY = mouseY * guiScale
        val scaledHorizontalAmount = horizontalAmount * guiScale
        val scaledVerticalAmount = verticalAmount * guiScale

        lastMouseX = mouseX
        lastMouseY = mouseY
        lastHorizontalAmount = horizontalAmount
        lastVerticalAmount = verticalAmount

        return mouseScrolledOneToOne(scaledMouseX, scaledMouseY, scaledHorizontalAmount, scaledVerticalAmount)
    }


    protected abstract fun renderOneToOne(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float)

    protected open fun mouseClickedOneToOne(mouse: MouseButtonEvent, isDoubleClick: Boolean): Boolean {
        val bl = super.mouseClicked(lastUnscaledEvent!!, isDoubleClick)
        lastUnscaledEvent = null
        return bl
    }

    protected open fun mouseDraggedOneToOne(
        mouse: MouseButtonEvent,
        draggedDistanceX: Double,
        draggedDistanceY: Double
    ): Boolean {
        val bl = super.mouseDragged(lastUnscaledEvent!!, lastMouseX, lastMouseY)
        lastUnscaledEvent = null
        return bl
    }

    protected open fun mouseReleasedOneToOne(mouse: MouseButtonEvent): Boolean {
        val bl = super.mouseReleased(lastUnscaledEvent!!)
        lastUnscaledEvent = null
        return bl
    }

    protected open fun mouseScrolledOneToOne(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {
        return super.mouseScrolled(lastMouseX, lastMouseY, lastHorizontalAmount, lastVerticalAmount)
    }


    private fun scaleMouseEvent(event: MouseButtonEvent): MouseButtonEvent {
        lastUnscaledEvent = event
        val guiScale = minecraft.window.guiScale
        return MouseButtonEvent(event.x() * guiScale, event.y() * guiScale, event.buttonInfo())
    }

    override fun onClose() {
        projectionBuffer?.close()
        super.onClose()
    }

    override fun isPauseScreen(): Boolean {
        return false
    }
}
