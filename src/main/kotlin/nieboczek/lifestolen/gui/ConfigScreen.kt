package nieboczek.lifestolen.gui

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.network.chat.Component

class ConfigScreen : Screen(Component.literal("Lifestolen")) {
    private fun GuiGraphicsExtractor.roundedRect(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        fillColor: Int,
        outlineColor: Int,
        outlineWidth: Int,
        radius: Float = 4f,
    ) {
        guiRenderState.addGuiElement(
            RoundedRectRenderState(
                x,
                y,
                x + width,
                y + height,
                fillColor,
                outlineColor,
                outlineWidth,
                radius,
                scissorStack.peek()
            )
        )
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        graphics.fill(0, 0, width, height, 0xFF000000.toInt())

        graphics.roundedRect(20, 20, 600, 320, 0xFF333333.toInt(), 0xFF555555.toInt(), 1)
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        val guiKey = KeyMappingHelper.getBoundKeyOf(minecraft.options.keySocialInteractions).value
        if (event.key == guiKey || event.isEscape) {
            onClose()
            return true
        }
        return super.keyPressed(event)
    }

    override fun isPauseScreen() = false
}
