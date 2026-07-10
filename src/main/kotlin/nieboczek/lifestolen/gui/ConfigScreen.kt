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
        val categories = Module.Category.entries.map { category ->
            CategoryData(
                category.toString(),
                Lifestolen.modules.filter { it.category == category },
            )
        }
    }

    private var rainbowColorOffset = 0

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        val outlineColor = 0x77888888

        val fontHeight = 12
        val outlineWidth = 2
        val categoryGap = 8
        val marginTop = 8
        val namePadding = 2
        val paddingHorizontal = categoryGap * 2
        val categoryWidth = (width - (paddingHorizontal * 2) - ((categories.size - 1) * categoryGap)) / categories.size

        val guiScale = minecraft.window.guiScale.toFloat()
        rainbowColorOffset += 1

        categories.forEachIndexed { idx, category ->
            val categoryX = paddingHorizontal + (idx * categoryWidth) + (idx * categoryGap)
            graphics.blurredRoundedRect(
                categoryX,
                marginTop,
                categoryWidth,
                200,
                0x77000000,
                outlineColor,
                outlineWidth,
                8f,
                16f,
            )

            val hue = (rainbowColorOffset % 360) / 360f
            val color = Color.HSBtoRGB(hue, 0.5f, 1f)
            val nameX = categoryX + (categoryWidth / 2) - (font.width(category.name) / 2)
            graphics.text(font, category.name, nameX, marginTop + namePadding, color, false)

            val lineY = marginTop + namePadding + fontHeight + namePadding
            // reason for 1.5f: the roundedRect stuff does some weird stuff with outlines blah blah blah yeah
            val lineHeight = outlineWidth / guiScale * 1.5f
            graphics.rect(
                categoryX + (outlineWidth / guiScale),
                lineY.toFloat(),
                categoryWidth - (outlineWidth / guiScale * 2),
                lineHeight,
                outlineColor
            )
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
