package nieboczek.lifestolen.gui.widget

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.ContainerEventHandler
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.MouseButtonEvent

abstract class Widget(val anchor: Anchor) : Renderable, ContainerEventHandler, NarratableEntry {
    protected val x
        get() = calculatedPosition.first
    protected val y
        get() = calculatedPosition.second
    protected val width
        get() = anchor.width
    protected val height
        get() = anchor.height

    private val calculatedPosition = anchor.calculatePosition()
    private var dragging = false

    protected abstract fun renderWidget(graphics: GuiGraphics, mouseX: Double, mouseY: Double, partialTicks: Float)
    protected abstract fun isMouseOverWidget(mouseX: Double, mouseY: Double): Boolean

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        renderWidget(guiGraphics, mouseX.toDouble(), mouseY.toDouble(), partialTicks)
    }


    protected fun isMouseOverWidget(mouse: MouseButtonEvent): Boolean {
        return isMouseOverWidget(mouse.x(), mouse.y())
    }

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
        return isMouseOverWidget(mouseX, mouseY)
    }

    override fun setFocused(guiEventListener: GuiEventListener?) {
        // no
    }

    override fun children(): MutableList<GuiEventListener> {
        return mutableListOf()
    }

    override fun isDragging(): Boolean {
        return dragging
    }

    override fun setDragging(bl: Boolean) {
        dragging = bl
    }

    override fun getFocused(): GuiEventListener? {
        return null
    }

    override fun setFocused(bl: Boolean) {
        // no
    }

    override fun isFocused(): Boolean {
        return false
    }

    override fun narrationPriority(): NarratableEntry.NarrationPriority {
        return NarratableEntry.NarrationPriority.NONE
    }

    override fun isActive(): Boolean {
        return false
    }

    override fun updateNarration(narrationElementOutput: NarrationElementOutput) {
        // no
    }
}
