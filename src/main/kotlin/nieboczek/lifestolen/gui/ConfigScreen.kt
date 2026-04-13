package nieboczek.lifestolen.gui

import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import nieboczek.lifestolen.CursedWebView
import nieboczek.lifestolen.Lifestolen
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

    override fun isPauseScreen(): Boolean {
        return false
    }
}
