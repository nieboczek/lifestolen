package nieboczek.lifestolen.gui.widget

import net.minecraft.client.gui.GuiGraphicsExtractor
import nieboczek.lifestolen.Lifestolen.font
import nieboczek.lifestolen.Lifestolen.fontSmall
import nieboczek.lifestolen.gui.render.rect
import nieboczek.lifestolen.gui.render.roundedRect
import nieboczek.lifestolen.gui.render.scissor
import nieboczek.lifestolen.module.Module
import org.lwjgl.glfw.GLFW

class ModuleWidget(val live: Module, val settings: List<SettingWidget<*>>) : Widget(), Hoverable {
    val clickHandler = ClickableWidget { button ->
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (ScreenState.currentlyConfiguring == this) {
                expanded = !expanded
            } else {
                ScreenState.currentlyConfiguring?.let { it.expanded = false }
                expanded = true
                ScreenState.currentlyConfiguring = this
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            live.toggle()
        }
        Clickable.Action.NONE
    }

    override var hovered = false
    override var hoverProgress = 0f
    var expanded = false
    var expandProgress = 0f
    var enabledProgress = if (live.enabled) 1f else 0f

    fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int, moduleWidth: Int, moduleHeight: Int) {
        val moduleInsideHPadding = 4

        val height = moduleHeight + computeExpandedHeight()
        val outlineColor = ScreenState.lerpOutlineColor(hoverProgress)
        graphics.roundedRect(x, y, moduleWidth, height, 0, outlineColor, ScreenState.OUTLINE_WIDTH, 4f)
        clickHandler.bounds = Bounds(x, y, moduleWidth, moduleHeight)
        bounds = Bounds(x, y, moduleWidth, height)

        val moduleNameX = x + moduleInsideHPadding + ((moduleWidth - font.width(live.name)) / 2)
        val color = blendModuleColor(ScreenState.darkRainbowColor, ScreenState.rainbowColor)
        graphics.text(font, live.name, moduleNameX, y + ScreenState.MODULE_INSIDE_V_PADDING, color, false)

        if (expandProgress <= 0f) return

        val settingX = x + moduleInsideHPadding
        val rightAlignedX = x + moduleWidth - moduleInsideHPadding
        val settingWidth = moduleWidth - (moduleInsideHPadding * 2)
        var settingY =
            y + ScreenState.MODULE_INSIDE_V_PADDING + ScreenState.FONT_HEIGHT + ScreenState.MODULE_INSIDE_V_PADDING

        graphics.scissor(
            settingX,
            settingY - ScreenState.MODULE_INSIDE_V_PADDING,
            moduleWidth,
            computeExpandedHeight() + ScreenState.MODULE_INSIDE_V_PADDING
        ) {
            for (setting in settings) {
                if (setting.live.id == "Enabled") continue

                graphics.text(fontSmall, setting.live.name, settingX, settingY, -1, false)
                setting.render(graphics, rightAlignedX, settingY, settingWidth)

                if (ScreenState.debugMode) {
                    val fx = x.toFloat()
                    val w = moduleWidth.toFloat()
                    val h = 1f / ScreenState.guiScale
                    val fontHeight = ScreenState.FONT_SMALL_HEIGHT
                    graphics.rect(fx, settingY.toFloat(), w, h, 0xFFFF0000.toInt())
                    graphics.rect(fx, (settingY + (fontHeight / 2)).toFloat(), w, h, 0xFF00FF00.toInt())
                    graphics.rect(fx, (settingY + fontHeight).toFloat(), w, h, 0xFFFF0000.toInt())
                }

                settingY += ScreenState.SETTING_GAP + setting.calculateHeight()
            }
        }
    }

    override fun tick(dt: Float) {
        enabledProgress = if (live.enabled) (enabledProgress + dt).coerceAtMost(1f)
        else (enabledProgress - dt).coerceAtLeast(0f)

        expandProgress = if (expanded) (expandProgress + dt).coerceAtMost(1f)
        else (expandProgress - dt).coerceAtLeast(0f)
    }

    override fun getVisibleChildren(): List<Widget> {
        if (expandProgress == 0f) return listOf(clickHandler)
        if (expandProgress == 1f) return listOf(clickHandler) + settings

        var availableHeight = computeExpandedHeight() - ScreenState.MODULE_INSIDE_V_PADDING
        if (availableHeight <= 0) return listOf(clickHandler)

        val children = mutableListOf<Widget>(clickHandler)
        for (setting in settings) {
            availableHeight -= setting.calculateHeight()
            if (availableHeight < 0) return children

            children.add(setting)
            availableHeight -= ScreenState.SETTING_GAP
        }
        return children
    }

    fun computeExpandedHeight(): Int {
        if (expandProgress == 0f) return 0
        val baseHeight = settings.fold(0) { acc, state ->
            if (state.live.id == "Enabled") return@fold acc
            acc + state.calculateHeight()
        }
        val paddedHeight =
            baseHeight + ScreenState.MODULE_INSIDE_V_PADDING + (ScreenState.SETTING_GAP * (settings.size - 2))
        return (paddedHeight * expandProgress).toInt()
    }

    private fun blendModuleColor(darkRainbowColor: Int, rainbowColor: Int): Int {
        val baseColor = ScreenState.lerpColor(0xBBCCCCCC.toInt(), darkRainbowColor, enabledProgress)
        val hoverColor = ScreenState.lerpColor(0xBBFFFFFF.toInt(), rainbowColor, enabledProgress)
        return ScreenState.lerpColor(baseColor, hoverColor, hoverProgress)
    }
}
