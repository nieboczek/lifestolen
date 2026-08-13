package nieboczek.lifestolen.gui.widget

import net.minecraft.client.gui.GuiGraphicsExtractor
import nieboczek.lifestolen.gui.Fonts.fontBig
import nieboczek.lifestolen.gui.render.blurredRoundedRect
import nieboczek.lifestolen.gui.render.rect
import kotlin.math.ceil

class CategoryWidget(val name: String, val modules: List<ModuleWidget>) : Widget() {
    fun render(graphics: GuiGraphicsExtractor, idx: Int, categoriesSize: Int, screenWidth: Int) {
        val categoryGap = 8
        val marginTop = 8
        val namePadding = 2
        val moduleVPadding = 2
        val moduleHPadding = 4
        val paddingHorizontal = categoryGap * 2
        val categoryWidth =
            (screenWidth - (paddingHorizontal * 2) - ((categoriesSize - 1) * categoryGap)) / categoriesSize
        val moduleWidth = categoryWidth - (moduleHPadding * 2)

        val lineY = marginTop + namePadding + ScreenState.FONT_BIG_HEIGHT + namePadding
        val lineHeight = ScreenState.OUTLINE_WIDTH / ScreenState.guiScale * 1.5f
        val lineHeightCeil = ceil(lineHeight).toInt()
        val moduleStartY = lineY + lineHeightCeil + moduleVPadding

        val categoryX = paddingHorizontal + (idx * categoryWidth) + (idx * categoryGap)
        val moduleHeight = ScreenState.FONT_HEIGHT + (ScreenState.MODULE_INSIDE_V_PADDING * 2)
        val lastModuleBottom = moduleStartY + ((moduleVPadding + moduleHeight) * modules.size)
        val expandedHeight = modules.fold(0) { acc, state -> acc + state.computeExpandedHeight() }
        val neededHeight = lastModuleBottom - marginTop + moduleVPadding + expandedHeight

        graphics.blurredRoundedRect(
            categoryX,
            marginTop,
            categoryWidth,
            neededHeight,
            0x92000000.toInt(),
            ScreenState.OUTLINE_COLOR,
            ScreenState.OUTLINE_WIDTH,
            8f,
            16f,
        )

        val nameX = categoryX + ((categoryWidth - fontBig.width(name)) / 2)
        graphics.text(fontBig, name, nameX, marginTop + namePadding, ScreenState.rainbowColor, false)
        graphics.rect(
            categoryX + (ScreenState.OUTLINE_WIDTH / ScreenState.guiScale),
            lineY.toFloat(),
            categoryWidth - (ScreenState.OUTLINE_WIDTH / ScreenState.guiScale * 2f),
            lineHeight,
            ScreenState.OUTLINE_COLOR
        )

        var moduleY = moduleStartY
        for (module in modules) {
            module.render(graphics, categoryX + moduleHPadding, moduleY, moduleWidth, moduleHeight)
            moduleY += moduleVPadding + moduleHeight + module.computeExpandedHeight()
        }
    }

    override fun getVisibleChildren() = modules
}
