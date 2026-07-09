package nieboczek.lifestolen.gui

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.network.chat.Component

class ConfigScreen : Screen(Component.literal("Lifestolen")) {
    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        graphics.blurredRoundedRect(4, 4, 100, 200, 0x33777777, radius = 8f, blurRadius = 16f)
        graphics.blurredRoundedRect(108, 4, 100, 200, 0x33777777, radius = 8f, blurRadius = 16f)
        graphics.blurredRoundedRect(212, 4, 100, 200, 0x33777777, radius = 8f, blurRadius = 16f)
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
}
