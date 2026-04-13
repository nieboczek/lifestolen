package nieboczek.lifestolen.gui

import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import nieboczek.lifestolen.Lifestolen
import tytoo.grapheneui.api.GrapheneCore
import tytoo.grapheneui.api.bridge.GrapheneBridge
import tytoo.grapheneui.api.bridge.GrapheneBridgeSubscription
import tytoo.grapheneui.api.widget.GrapheneWebViewWidget

object WebViewManager {
    // We don't initialize it every time ModuleGui is constructed to make WebView feel more responsive
    var webView: GrapheneWebViewWidget? = null
    private var readySub: GrapheneBridgeSubscription? = null

    fun addSubscriptions(bridge: GrapheneBridge) {
        readySub = bridge.onEvent("ready") { _, _ ->
            bridge.emitJson("init", object {
                val modules = Lifestolen.modules.map {
                    ModuleInfo(it.id, it.category.toString())
                }
            })

            Lifestolen.log.info("[WebViewManager::addSubscriptions] Sent init payload to WebView")
            readySub?.unsubscribe()
        }
    }

    fun initialize() {
        val handle = GrapheneCore.handle(Lifestolen.MOD_ID)
        val url = handle.appAssets().asset("ui/index.html")
        val widget = GrapheneWebViewWidget(
            object : Screen(Component.empty()) {},
            0,
            0,
            640,
            360,
            Component.empty(),
            url
        )

        addSubscriptions(widget.bridge())
        webView = widget
    }

    private data class ModuleInfo(val id: String, val category: String)
}
