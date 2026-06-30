package nieboczek.lifestolen.gui

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import nieboczek.lifestolen.config.setting.ColorSetting

class ColorPickerScreen(
    private val setting: ColorSetting,
    private val parent: Screen
) : Screen(Component.literal("Pick Color")) {

    private var dragTarget: String? = null
    private val clickRegions = mutableListOf<Pair<Region, () -> Unit>>()

    private data class Region(val x1: Int, val y1: Int, val x2: Int, val y2: Int) {
        fun has(mx: Double, my: Double) = mx >= x1 && mx < x2 && my >= y1 && my < y2
    }

    private var curR = (setting.value ushr 16) and 0xFF
    private var curG = (setting.value ushr 8) and 0xFF
    private var curB = setting.value and 0xFF
    private var curA = (setting.value ushr 24) and 0xFF

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        clickRegions.clear()
        graphics.fillGradient(0, 0, width, height, 0xC0101010.toInt(), 0xD0101010.toInt())

        val cx = width / 2
        val sy = height / 2 - 60

        drawLabel(graphics, "Red", 0xFF4444, cx - 90, sy, mouseX, mouseY)
        curR = drawSlider(graphics, "R", curR, cx - 90, sy + 16, cx, mouseX, mouseY)

        drawLabel(graphics, "Green", 0x44FF44, cx - 90, sy + 44, mouseX, mouseY)
        curG = drawSlider(graphics, "G", curG, cx - 90, sy + 60, cx, mouseX, mouseY)

        drawLabel(graphics, "Blue", 0x4444FF, cx - 90, sy + 88, mouseX, mouseY)
        curB = drawSlider(graphics, "B", curB, cx - 90, sy + 104, cx, mouseX, mouseY)

        drawLabel(graphics, "Alpha", 0xFFFFFF, cx - 90, sy + 132, mouseX, mouseY)
        curA = drawSlider(graphics, "A", curA, cx - 90, sy + 148, cx, mouseX, mouseY)

        val color = packColor(curA, curR, curG, curB)
        val px = cx - 40
        val py = sy - 50
        graphics.fill(px, py, px + 80, py + 30, 0xFFFFFFFF.toInt())
        graphics.fill(px + 1, py + 1, px + 79, py + 29, color)

        val hex = String.format("%08X", color)
        graphics.centeredText(font, Component.literal("#$hex"), cx, py - 14, 0xAAAAAA)

        val btnX = cx - 30
        val btnY = sy + 180
        val btnW = 60
        val btnH = 16
        val over = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH
        graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, if (over) 0xFF555555.toInt() else 0xFF333333.toInt())
        graphics.centeredText(font, Component.literal("Done"), cx, btnY + 4, 0xFFFFFF)
        clickRegions.add(Region(btnX, btnY, btnX + btnW, btnY + btnH) to {
            setting.value = color
            minecraft.gui.setScreen(parent)
        })
    }

    private fun drawLabel(graphics: GuiGraphicsExtractor, name: String, color: Int, x: Int, y: Int, mx: Int, my: Int) {
        graphics.fill(x, y, x + 8, y + 8, color)
        graphics.text(font, name, x + 12, y, 0xCCCCCC)
    }

    private fun drawSlider(graphics: GuiGraphicsExtractor, id: String, value: Int, x: Int, y: Int, cx: Int, mx: Int, my: Int): Int {
        val sw = 160
        val sx = cx - sw / 2
        val barH = 8

        graphics.fill(sx, y + 4, sx + sw, y + 4 + barH, 0xFF222222.toInt())
        val filled = (value * sw / 255)
        graphics.fill(sx, y + 4, sx + filled, y + 4 + barH, 0xFF888888.toInt())

        val text = "$value"
        val tx = sx + sw + 10
        graphics.text(font, text, tx, y + 2, 0xFFFFFF)

        return value
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        if (event.buttonInfo().button() != 0) return false

        val mx = event.x()
        val my = event.y()

        val cx = width / 2
        val sy = height / 2 - 60
        val sw = 160
        val sx = cx - sw / 2

        for ((i, info) in listOf("R" to sy + 16, "G" to sy + 60, "B" to sy + 104, "A" to sy + 148).withIndex()) {
            val (id, y) = info
            if (mx >= sx && mx < sx + sw && my >= y + 4 && my < y + 12) {
                dragTarget = id
                val newVal = ((mx - sx) * 255 / sw).toInt().coerceIn(0, 255)
                setValue(i, newVal)
                return true
            }
        }

        for ((region, action) in clickRegions.reversed()) {
            if (region.has(mx, my)) {
                action()
                return true
            }
        }
        return false
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        dragTarget = null
        return super.mouseReleased(event)
    }

    override fun mouseDragged(event: MouseButtonEvent, dx: Double, dy: Double): Boolean {
        if (dragTarget != null && event.buttonInfo().button() == 0) {
            val cx = width / 2
            val sw = 160
            val sx = cx - sw / 2
            val newVal = ((event.x() - sx) * 255 / sw).toInt().coerceIn(0, 255)
            val index = when (dragTarget) {
                "R" -> 0; "G" -> 1; "B" -> 2; "A" -> 3; else -> return false
            }
            setValue(index, newVal)
            return true
        }
        return super.mouseDragged(event, dx, dy)
    }

    private fun setValue(index: Int, v: Int) {
        when (index) { 0 -> curR = v; 1 -> curG = v; 2 -> curB = v; 3 -> curA = v }
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (event.isEscape) {
            minecraft.gui.setScreen(parent)
            return true
        }
        return super.keyPressed(event)
    }

    override fun isPauseScreen() = false

    companion object {
        fun packColor(a: Int, r: Int, g: Int, b: Int): Int {
            return ((a.toLong() shl 24) or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong()).toInt()
        }
    }
}
