package nieboczek.lifestolen.gui

import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.network.chat.Component
import nieboczek.lifestolen.CursedWebView
import nieboczek.lifestolen.Lifestolen
import org.lwjgl.glfw.GLFW
import tytoo.grapheneui.api.screen.GrapheneScreens

class ConfigScreen : Screen(Component.literal(Lifestolen.CLIENT_NAME)) {
    private val webView = WebViewManager.webView!!

    init {
        GrapheneScreens.setWebViewAutoCloseEnabled(this, false)
        (webView as CursedWebView).`lifestolen$setScreen`(this)
    }

    override fun init() {
        webView.setSize(width, height)
        addRenderableWidget(webView)
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        val guiKey = KeyBindingHelper.getBoundKeyOf(minecraft.options.keySocialInteractions).value
        val displayed = InputConstants.Type.KEYSYM.getOrCreate(event.key).displayName.string
        WebViewManager.keyPressed(event.key, displayed, event.key == guiKey || event.key == GLFW.GLFW_KEY_ESCAPE)

        if (event.key == guiKey) {
            minecraft.setScreen(null)
        }
        return true
    }

    override fun isPauseScreen(): Boolean {
        return false
    }
}
