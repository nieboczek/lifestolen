package nieboczek.lifestolen.gui.widget

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.MouseButtonEvent
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.gui.drawRoundedBox

class Slider(
    anchor: Anchor,
    private val min: Float,
    private val max: Float,
    initialValue: Float,
    private val step: Float = 1f,
    private val onValueChanged: (Float) -> Unit = {},
    private val onEndAdjusting: (Float) -> Unit = {}
) : Widget(anchor) {
    var value: Float = initialValue
        private set

    private var dragging = false

    companion object {
        private const val KNOB_WIDTH = 8
    }

    override fun renderWidget(graphics: GuiGraphics, mouseX: Double, mouseY: Double, partialTicks: Float) {
        graphics.drawRoundedBox(x, y, width, height, 4f, -0xd5d5d6, -0xb5b5b6)

        val ratio = (value - min) / (max - min)
        val knobX = x + ((width - KNOB_WIDTH) * ratio).toInt()
        graphics.fill(knobX, y + 2, knobX + KNOB_WIDTH, y + height - 2, -0xaa5501)

        if (isMouseOverWidget(mouseX, mouseY)) {
            graphics.fill(x, y, x + width, y + height, 0x20FFFFFF)
        }
    }

    override fun isMouseOverWidget(mouseX: Double, mouseY: Double): Boolean {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height
    }

    override fun mouseClicked(mouse: MouseButtonEvent, isDoubleClick: Boolean): Boolean {
        if (mouse.button() == 0) {
            setValueFromMouse(mouse.x)
            dragging = true
            return true
        }
        return false
    }

    override fun mouseDragged(
        mouse: MouseButtonEvent,
        draggedDistanceX: Double,
        draggedDistanceY: Double
    ): Boolean {
        if (dragging) {
            setValueFromMouse(mouse.x)
            return true
        }
        return false
    }

    override fun mouseReleased(mouse: MouseButtonEvent): Boolean {
        if (dragging && mouse.button() == 0) {
            dragging = false
            onEndAdjusting(value)
            return true
        }
        return super.mouseReleased(mouse)
    }

    private fun setValueFromMouse(mouseX: Double) {
        val ratio = (mouseX - x - KNOB_WIDTH / 2).toFloat() / (width - KNOB_WIDTH)
        val clampedRatio = ratio.coerceIn(0f, 1f)
        var newValue = min + clampedRatio * (max - min)

        if (step > 0f) {
            newValue = (newValue / step).toInt() * step
        }

        newValue = newValue.coerceIn(min, max)

        if (newValue != value) {
            value = newValue
            onValueChanged(newValue)
        }
    }
}