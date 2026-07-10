package nieboczek.lifestolen.gui

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.network.chat.Component
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.module.Module
import java.awt.Color

class ConfigScreen : Screen(Minecraft.getInstance(), Lifestolen.font, Component.literal(Lifestolen.CLIENT_NAME)) {
    companion object {
        private val categories = Module.Category.entries.map { category ->
            CategoryData(
                category.toString(),
                Lifestolen.modules.filter { it.category == category },
            )
        }
    }

    private val fontBig = Lifestolen.fontBig
    private var rainbowColorOffset = 0

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        val outlineColor = 0x77888888

        val fontBigHeight = 12
        val fontHeight = 8
        val outlineWidth = 2
        val categoryGap = 8
        val marginTop = 8
        val namePadding = 2
        val moduleVPadding = 2
        val moduleHPadding = 4
        val moduleNameVPadding = 4
        val moduleNameHPadding = 4
        val moduleHeight = fontHeight + (moduleNameVPadding * 2)
        val paddingHorizontal = categoryGap * 2
        val categoryWidth = (width - (paddingHorizontal * 2) - ((categories.size - 1) * categoryGap)) / categories.size
        val moduleWidth = categoryWidth - (moduleHPadding * 2)
        val lineY = marginTop + namePadding + fontBigHeight + namePadding

        val guiScale = minecraft.window.guiScale.toFloat()
        val lineHeight = outlineWidth / guiScale * 1.5f
        val moduleStartY = lineY + kotlin.math.ceil(lineHeight).toInt() + moduleVPadding

        rainbowColorOffset += 1
        val hue = (rainbowColorOffset % 360) / 360f
        val rainbowColor = Color.HSBtoRGB(hue, 0.5f, 1f)

        categories.forEachIndexed { idx, category ->
            val categoryX = paddingHorizontal + (idx * categoryWidth) + (idx * categoryGap)
            val lastModuleBottom = moduleStartY + ((moduleVPadding + moduleHeight) * category.modules.size)
            val neededHeight = lastModuleBottom - marginTop + moduleVPadding

            graphics.blurredRoundedRect(
                categoryX,
                marginTop,
                categoryWidth,
                neededHeight,
                0x77000000,
                outlineColor,
                outlineWidth,
                8f,
                16f,
            )

            val nameX = categoryX + ((categoryWidth - fontBig.width(category.name)) / 2)
            graphics.text(fontBig, category.name, nameX, marginTop + namePadding, rainbowColor, false)
            graphics.rect(
                categoryX + (outlineWidth / guiScale),
                lineY.toFloat(),
                categoryWidth - (outlineWidth / guiScale * 2),
                lineHeight,
                outlineColor
            )

            val moduleX = categoryX + moduleHPadding
            category.modules.forEachIndexed { idx, module ->
                val moduleY = moduleStartY + ((moduleVPadding + moduleHeight) * idx)
                graphics.roundedRect(moduleX, moduleY, moduleWidth, moduleHeight, 0, outlineColor, outlineWidth, 4f)

                val moduleNameX = moduleX + moduleNameHPadding + ((moduleWidth - font.width(module.id)) / 2)
                val color = if (module.enabled) rainbowColor else 0xBBCCCCCC.toInt()
                graphics.text(font, module.id, moduleNameX, moduleY + moduleNameVPadding, color, false)
            }
        }
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        val guiKey = KeyMappingHelper.getBoundKeyOf(minecraft.options.keySocialInteractions).value
        if (event.key == guiKey || event.isEscape) {
            onClose()
            return true
        }
        return super.keyPressed(event)
    }

    override fun extractTransparentBackground(graphics: GuiGraphicsExtractor) = graphics.blurBeforeThisStratum()
    override fun isInGameUi() = true
    override fun isPauseScreen() = false

    class CategoryData(var name: String, val modules: List<Module>)
}
