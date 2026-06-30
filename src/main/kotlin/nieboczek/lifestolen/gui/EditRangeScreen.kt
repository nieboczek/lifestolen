package nieboczek.lifestolen.gui

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.network.chat.Component
import nieboczek.lifestolen.config.setting.Setting

class EditRangeScreen(
    private val setting: Setting<IntRange>,
    private val parent: Screen
) : Screen(Component.literal("Edit Range")) {

    private val current = setting.value

    private val minBuf = StringBuilder(current.start.toString())
    private val maxBuf = StringBuilder(current.endInclusive.toString())

    private var activeField = 0 // 0 = min, 1 = max
    private var cursorPos = intArrayOf(minBuf.length, maxBuf.length)

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        graphics.fillGradient(0, 0, width, height, 0xC0101010.toInt(), 0xD0101010.toInt())

        val boxW = 160
        val boxH = 22
        val centerX = width / 2
        val startY = height / 2 - 40

        drawField(graphics, centerX - boxW / 2, startY, boxW, boxH, "Min", minBuf, cursorPos[0], activeField == 0)

        drawField(graphics, centerX - boxW / 2, startY + boxH + 16, boxW, boxH, "Max", maxBuf, cursorPos[1], activeField == 1)

        graphics.centeredText(font, Component.literal("Enter = confirm, Tab = switch, Esc = cancel"), width / 2, startY + 80, 0xFF999999.toInt())
    }

    private fun drawField(graphics: GuiGraphicsExtractor, x: Int, y: Int, w: Int, h: Int, label: String, buf: StringBuilder, cursor: Int, active: Boolean) {
        val borderColor = if (active) 0xFFFFAA00.toInt() else 0xFFFFFFFF.toInt()
        graphics.text(font, label, x, y - 11, 0xFFBBBBBB.toInt())
        graphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, borderColor)
        graphics.fill(x, y, x + w, y + h, 0xFF222222.toInt())

        val display = buf.toString()
        val textX = x + 4
        val textY = y + (h - 9) / 2
        graphics.text(font, display, textX, textY, 0xFFFFFFFF.toInt())

        if (active && System.currentTimeMillis() % 1000 < 500) {
            val cursorX = textX + font.width(display.substring(0, cursor.coerceAtMost(display.length)))
            graphics.fill(cursorX, textY + 1, cursorX + 1, textY + 9, 0xFFFFFFFF.toInt())
        }
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

        if (event.key() == 258) {
            activeField = 1 - activeField
            return true
        }

        val buf = if (activeField == 0) minBuf else maxBuf
        val cp = cursorPos[activeField]

        if (event.key() == 259) {
            if (cp > 0 && buf.isNotEmpty()) {
                buf.deleteAt(cp - 1)
                cursorPos[activeField]--
            }
            return true
        }

        if (event.key() == 263) {
            cursorPos[activeField] = (cp - 1).coerceAtLeast(0)
            return true
        }

        if (event.key() == 262) {
            cursorPos[activeField] = (cp + 1).coerceAtMost(buf.length)
            return true
        }

        return super.keyPressed(event)
    }

    override fun charTyped(event: CharacterEvent): Boolean {
        val c = event.codepoint().toChar()
        if (isValidChar(c, activeField) && getBuf().length < 10) {
            val buf = getBuf()
            val cp = cursorPos[activeField]
            buf.insert(cp, c)
            cursorPos[activeField]++
            return true
        }
        return super.charTyped(event)
    }

    private fun getBuf() = if (activeField == 0) minBuf else maxBuf

    private fun isValidChar(c: Char, field: Int): Boolean {
        val buf = if (field == 0) minBuf else maxBuf
        val test = StringBuilder(buf).insert(cursorPos[field], c).toString()
        return test.toIntOrNull() != null || test == "-" || test.isEmpty()
    }

    private fun applyValue() {
        val minText = minBuf.toString()
        val maxText = maxBuf.toString()
        if (minText.isEmpty() || maxText.isEmpty() || minText == "-" || maxText == "-") {
            minecraft.gui.setScreen(parent)
            return
        }

        try {
            val min = minText.toInt()
            val max = maxText.toInt()
            if (min <= max) {
                setting.value = IntRange(min, max)
            }
        } catch (_: NumberFormatException) {}

        minecraft.gui.setScreen(parent)
    }

    override fun isPauseScreen() = false
}
