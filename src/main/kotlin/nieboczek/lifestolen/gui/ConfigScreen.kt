package nieboczek.lifestolen.gui

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.config.setting.ColorSetting
import nieboczek.lifestolen.config.setting.Setting
import nieboczek.lifestolen.module.Module

class ConfigScreen : Screen(Component.literal(Lifestolen.CLIENT_NAME)) {
    private var selectedCategory = Module.Category.COMBAT
    private var scrollOffset = 0
    private var capturingKeybind: Setting<Int>? = null

    private val tabY = 4
    private val tabH = 18
    private val tabGap = 4
    private val listY get() = tabY + tabH + tabGap + 4
    private val listH get() = height - listY - 4
    private val colX = 8
    private val colW get() = width - colX * 2
    private val entryH = 18
    private val settingH = 14
    private val gap = 1

    private data class Region(val x1: Int, val y1: Int, val x2: Int, val y2: Int) {
        fun has(mx: Double, my: Double) = mx >= x1 && mx < x2 && my >= y1 && my < y2
    }

    private data class ModEntry(val module: Module) {
        var expanded = false
    }

    private val entries = Lifestolen.modules.map { ModEntry(it) }
    private val clickRegions = mutableListOf<Pair<Region, () -> Unit>>()
    private var prevMouseX = 0
    private var prevMouseY = 0

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        prevMouseX = mouseX
        prevMouseY = mouseY
        clickRegions.clear()

        graphics.fill(0, 0, width, height, 0xD0000000.toInt())

        drawTabs(graphics)
        drawModuleList(graphics, mouseX, mouseY)

        if (capturingKeybind != null) {
            graphics.fill(0, 0, width, height, 0xAA000000.toInt())
            graphics.centeredText(font, Component.literal("Press a key to bind..."), width / 2, height / 2, 0xFFFF55)
        }
    }

    private fun drawTabs(graphics: GuiGraphicsExtractor) {
        val pw = 72
        val total = pw * 3 + tabGap * 2
        val sx = (width - total) / 2

        for ((i, cat) in Module.Category.entries.withIndex()) {
            val x = sx + i * (pw + tabGap)
            val active = cat == selectedCategory
            val fillC = if (active) 0xFF555555.toInt() else 0xFF333333.toInt()
            val textC = if (active) 0xFFFFFFFF.toInt() else 0xFF999999.toInt()
            val hl = prevMouseX >= x && prevMouseX < x + pw && prevMouseY >= tabY && prevMouseY < tabY + tabH

            if (hl && !active) {
                graphics.fill(x, tabY, x + pw, tabY + tabH, 0xFF444444.toInt())
            } else {
                graphics.fill(x, tabY, x + pw, tabY + tabH, fillC)
            }

            graphics.text(font, cat.toString(), x + (pw - font.width(cat.toString())) / 2, tabY + 5, textC)

            clickRegions.add(Region(x, tabY, x + pw, tabY + tabH) to {
                selectedCategory = cat
                scrollOffset = 0
            })
        }
    }

    private fun drawModuleList(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        graphics.enableScissor(colX, listY, colW, listH)

        var y = listY + scrollOffset
        val visible = entries.filter { it.module.category == selectedCategory }

        for (entry in visible) {
            y = drawModule(graphics, entry, y, mouseX, mouseY) + gap
        }

        graphics.disableScissor()
    }

    private fun drawModule(graphics: GuiGraphicsExtractor, entry: ModEntry, y: Int, mx: Int, my: Int): Int {
        val m = entry.module
        val bg = if (m.enabled) 0xFF1A4A1A.toInt() else 0xFF1A1A1A.toInt()
        val over = Region(colX, y, colX + colW, y + entryH).has(mx.toDouble(), my.toDouble())

        graphics.fill(colX, y, colX + colW, y + entryH, bg)

        if (over) {
            graphics.fill(colX, y, colX + colW, y + entryH, 0x22FFFFFF)
        }

        graphics.text(font, m.id, colX + 4, y + 5, if (m.enabled) 0xFFFFFFFF.toInt() else 0xFF888888.toInt())

        val togT = if (m.enabled) "ON" else "OFF"
        val togC = if (m.enabled) 0xFF55FF55.toInt() else 0xFFFF5555.toInt()
        val togW = 28
        val togX = colX + colW - 55
        graphics.text(font, togT, togX + (togW - font.width(togT)) / 2, y + 5, togC)

        val expT = if (entry.expanded) "-" else "+"
        graphics.text(font, expT, colX + colW - 14, y + 5, 0xFFAAAAAA.toInt())

        clickRegions.add(Region(colX, y, colX + colW, y + entryH) to { m.toggle() })
        clickRegions.add(Region(togX, y, togX + togW, y + entryH) to { m.toggle() })
        clickRegions.add(Region(colX + colW - 18, y, colX + colW, y + entryH) to { entry.expanded = !entry.expanded })

        var ny = y + entryH + gap

        if (entry.expanded) {
            for (s in m.settings) {
                if (s.name == "Enabled" || s.name == "Keybind") continue
                ny = drawSetting(graphics, m, s, ny, mx, my) + gap
            }
            val kb = m.settings.find { it.name == "Keybind" }
            if (kb != null) {
                ny = drawKeybind(graphics, m, kb as Setting<Int>, ny, mx, my) + gap
            }
        }

        return ny
    }

    private fun drawSetting(graphics: GuiGraphicsExtractor, m: Module, s: Setting<*>, y: Int, mx: Int, my: Int): Int {
        val indent = 10
        val x = colX + indent
        val w = colW - indent

        graphics.fill(x, y, x + w, y + settingH, 0xFF0F0F0F.toInt())
        graphics.text(font, s.name, x + 4, y + 3, 0xFFBBBBBB.toInt())

        val bw = 18

        when (s) {
            is nieboczek.lifestolen.config.setting.NumberSetting<*> -> {
                val step = s.step as? Number ?: return y + settingH
                val v = s.value as? Number ?: return y + settingH

                val minX = colX + colW - bw - 4
                val valX = minX - 50
                val plusX = valX - bw - 2

                val valR = Region(valX, y, valX + 50, y + settingH)
                val minR = Region(minX, y, minX + bw, y + settingH)
                val plusR = Region(plusX, y, plusX + bw, y + settingH)

                graphics.fill(minX, y, minX + bw, y + settingH, 0xFF333333.toInt())
                graphics.text(font, "-", minX + 5, y + 3, if (minR.has(mx.toDouble(), my.toDouble())) 0xFFFFFFFF.toInt() else 0xFFAAAAAA.toInt())

                val fmt = if (step is Float || step is Double) "%.1f" else "%d"
                val valHovered = valR.has(mx.toDouble(), my.toDouble())
                graphics.text(font, String.format(fmt, v), valX, y + 3, if (valHovered) 0xFFFFFFFF.toInt() else 0xFFDDDDDD.toInt())

                graphics.fill(plusX, y, plusX + bw, y + settingH, 0xFF333333.toInt())
                graphics.text(font, "+", plusX + 5, y + 3, if (plusR.has(mx.toDouble(), my.toDouble())) 0xFFFFFFFF.toInt() else 0xFFAAAAAA.toInt())

                clickRegions.add(valR to { minecraft.gui.setScreen(EditValueScreen(s as Setting<out Number>, this)) })
                clickRegions.add(minR to { adjNum(s, -1) })
                clickRegions.add(plusR to { adjNum(s, 1) })
            }

            is nieboczek.lifestolen.config.setting.RangeSetting<*, *> -> {
                val range = s.value as? IntRange ?: return y + settingH
                val rangeR = Region(colX + colW - 110, y, colX + colW, y + settingH)
                val rangeHovered = rangeR.has(mx.toDouble(), my.toDouble())
                graphics.text(font, "${range.start} - ${range.endInclusive}", colX + colW - 90, y + 3, if (rangeHovered) 0xFFFFFFFF.toInt() else 0xFFDDDDDD.toInt())
                clickRegions.add(rangeR to { minecraft.gui.setScreen(EditRangeScreen(s as Setting<IntRange>, this)) })
            }

            is ColorSetting -> {
                val sw = 12
                val swX = colX + colW - sw - 6
                graphics.fill(swX, y + 1, swX + sw, y + 1 + sw, if (s.value == -1) 0xFF888888.toInt() else s.value)
                clickRegions.add(Region(swX - 2, y, swX + sw + 2, y + settingH) to {
                    minecraft.gui.setScreen(ColorPickerScreen(s, this))
                })
            }

            else -> {
                if (s.value is Boolean) {
                    val bT = if (s.value as Boolean) "ON" else "OFF"
                    val bC = if (s.value as Boolean) 0xFF55FF55.toInt() else 0xFFFF5555.toInt()
                    val bR = Region(colX + colW - 36, y, colX + colW, y + settingH)
                    graphics.fill(bR.x1, y, bR.x2, y + settingH, 0xFF222222.toInt())
                    graphics.text(font, bT, bR.x1 + (36 - font.width(bT)) / 2, y + 3, bC)
                    clickRegions.add(bR to {
                        @Suppress("UNCHECKED_CAST")
                        (s as Setting<Boolean>).value = !s.value
                    })
                }
            }
        }
        return y + settingH
    }

    private fun drawKeybind(graphics: GuiGraphicsExtractor, m: Module, s: Setting<Int>, y: Int, mx: Int, my: Int): Int {
        val x = colX + 10
        val w = colW - 10

        graphics.fill(x, y, x + w, y + settingH, 0xFF0F0F0F.toInt())
        graphics.text(font, "Keybind", x + 4, y + 3, 0xFFBBBBBB.toInt())

        val keyName = if (m.keybind <= 0) "NONE"
                      else InputConstants.Type.KEYSYM.getOrCreate(m.keybind).displayName.string
        val display = if (capturingKeybind == s) "..." else keyName
        val textC = if (capturingKeybind == s) 0xFFFF55 else 0xFFDDDDDD.toInt()
        val kbR = Region(colX + colW - 60, y, colX + colW, y + settingH)

        graphics.fill(kbR.x1, y, kbR.x2, y + settingH, 0xFF222222.toInt())
        graphics.text(font, display, kbR.x1 + (60 - font.width(display)) / 2, y + 3, textC)

        clickRegions.add(kbR to { capturingKeybind = s })
        return y + settingH
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        if (capturingKeybind != null) return true
        if (event.buttonInfo().button() != 0) return false

        val mx = event.x()
        val my = event.y()

        for ((region, action) in clickRegions.reversed()) {
            if (region.has(mx, my)) {
                action()
                return true
            }
        }
        return false
    }

    override fun mouseScrolled(x: Double, y: Double, scrollX: Double, scrollY: Double): Boolean {
        val visible = entries.filter { it.module.category == selectedCategory }
        val totalH = visible.sumOf { e ->
            var h = entryH + gap
            if (e.expanded) {
                for (s in e.module.settings) {
                    if (s.name == "Enabled") continue
                    h += settingH + gap
                }
            }
            h
        }
        val maxScroll = listH - totalH
        scrollOffset = if (maxScroll >= 0) 0
                       else (scrollOffset + (scrollY * 24).toInt()).coerceIn(maxScroll, 0)
        return true
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (capturingKeybind != null) {
            capturingKeybind!!.value = if (event.isEscape) 0 else event.key()
            capturingKeybind = null
            return true
        }
        if (event.isEscape) {
            onClose()
            return true
        }
        return super.keyPressed(event)
    }

    override fun isPauseScreen() = false

    @Suppress("UNCHECKED_CAST")
    private fun adjNum(s: Setting<*>, dir: Int) {
        if (s !is nieboczek.lifestolen.config.setting.NumberSetting<*>) return
        val step = s.step as? Number ?: return

        when (s.value) {
            is Int -> {
                val ns = (s.value as Int) + step.toInt() * dir
                val a = s.allowed as IntRange
                (s as Setting<Int>).value = ns.coerceIn(a.start, a.endInclusive)
            }
            is Float -> {
                val ns = (s.value as Float) + step.toFloat() * dir
                val a = s.allowed as ClosedFloatingPointRange<Float>
                (s as Setting<Float>).value = ns.coerceIn(a.start, a.endInclusive)
            }
            is Double -> {
                val ns = (s.value as Double) + step.toDouble() * dir
                val a = s.allowed as ClosedFloatingPointRange<Double>
                (s as Setting<Double>).value = ns.coerceIn(a.start, a.endInclusive)
            }
        }
    }
}
