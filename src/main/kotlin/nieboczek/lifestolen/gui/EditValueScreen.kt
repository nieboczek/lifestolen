package nieboczek.lifestolen.gui

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.network.chat.Component
import nieboczek.lifestolen.config.setting.Setting

class EditValueScreen(
    private val setting: Setting<out Number>,
    private val parent: Screen
) : Screen(Component.literal("Edit Value")) {

    private val buffer = StringBuilder(setting.value.toString())
    private var cursorPos = buffer.length

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        graphics.fillGradient(0, 0, width, height, 0xC0101010.toInt(), 0xD0101010.toInt())

        val boxX = width / 2 - 80
        val boxY = height / 2 - 15
        val boxW = 160
        val boxH = 22

        graphics.fill(boxX - 1, boxY - 1, boxX + boxW + 1, boxY + boxH + 1, 0xFFFFFFFF.toInt())
        graphics.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0xFF222222.toInt())

        val display = buffer.toString()
        val textX = boxX + 4
        val textY = boxY + (boxH - 9) / 2
        graphics.text(font, display, textX, textY, 0xFFFFFFFF.toInt())

        val cursorX = textX + font.width(display.substring(0, cursorPos.coerceAtMost(display.length)))
        if (System.currentTimeMillis() % 1000 < 500) {
            graphics.fill(cursorX, textY + 1, cursorX + 1, textY + 9, 0xFFFFFFFF.toInt())
        }

        graphics.centeredText(font, Component.literal("Enter = confirm, Esc = cancel"), width / 2, boxY + 36, 0xFF999999.toInt())
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (event.isEscape) {
            minecraft.gui.setScreen(parent)
            return true
        }

        if (event.isSelection()) {
            applyValue()
            return true
        }

        if (event.key() == 259) {
            if (cursorPos > 0 && buffer.isNotEmpty()) {
                buffer.deleteAt(cursorPos - 1)
                cursorPos--
            }
            return true
        }

        if (event.key() == 263) {
            cursorPos = (cursorPos - 1).coerceAtLeast(0)
            return true
        }

        if (event.key() == 262) {
            cursorPos = (cursorPos + 1).coerceAtMost(buffer.length)
            return true
        }

        return super.keyPressed(event)
    }

    override fun charTyped(event: CharacterEvent): Boolean {
        val c = event.codepoint().toChar()
        if (isValidChar(c) && buffer.length < 20) {
            buffer.insert(cursorPos, c)
            cursorPos++
            return true
        }
        return super.charTyped(event)
    }

    private fun isValidChar(c: Char): Boolean {
        val test = StringBuilder(buffer).insert(cursorPos, c).toString()
        return when (setting.value) {
            is Int -> test.toIntOrNull() != null || test == "-" || test.isEmpty()
            is Float -> test.toFloatOrNull() != null || test == "-" || test == "." || test == "-." || test.isEmpty()
            is Double -> test.toDoubleOrNull() != null || test == "-" || test == "." || test == "-." || test.isEmpty()
            else -> false
        }
    }

    private fun applyValue() {
        val text = buffer.toString()
        if (text.isEmpty() || text == "-" || text == "." || text == "-.") {
            minecraft.gui.setScreen(parent)
            return
        }

        try {
            when (setting.value) {
                is Int -> {
                    val v = text.toInt()
                    val a = (setting as? nieboczek.lifestolen.config.setting.NumberSetting<*>)?.allowed as? IntRange
                    if (a != null) (setting as Setting<Int>).value = v.coerceIn(a.start, a.endInclusive)
                    else (setting as Setting<Int>).value = v
                }
                is Float -> {
                    val v = text.toFloat()
                    val a = (setting as? nieboczek.lifestolen.config.setting.NumberSetting<*>)?.allowed as? ClosedFloatingPointRange<Float>
                    if (a != null) (setting as Setting<Float>).value = v.coerceIn(a.start, a.endInclusive)
                    else (setting as Setting<Float>).value = v
                }
                is Double -> {
                    val v = text.toDouble()
                    val a = (setting as? nieboczek.lifestolen.config.setting.NumberSetting<*>)?.allowed as? ClosedFloatingPointRange<Double>
                    if (a != null) (setting as Setting<Double>).value = v.coerceIn(a.start, a.endInclusive)
                    else (setting as Setting<Double>).value = v
                }
            }
        } catch (_: NumberFormatException) {}

        minecraft.gui.setScreen(parent)
    }

    override fun isPauseScreen() = false
}
