package nieboczek.lifestolen.gui

import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import nieboczek.lifestolen.Lifestolen
import tytoo.grapheneui.api.GrapheneCore
import tytoo.grapheneui.api.bridge.GrapheneBridge
import tytoo.grapheneui.api.bridge.GrapheneBridgeSubscription
import tytoo.grapheneui.api.widget.GrapheneWebViewWidget
import java.util.concurrent.CompletableFuture

object WebViewManager {
    // We don't initialize it every time ModuleGui is constructed to make WebView feel more responsive
    var webView: GrapheneWebViewWidget? = null
    private var readySub: GrapheneBridgeSubscription? = null
    private var toggleSub: GrapheneBridgeSubscription? = null

    fun addSubscriptions(bridge: GrapheneBridge) {
        readySub = bridge.onRequestJson("ready", Integer.TYPE) { _, _ ->
            readySub?.unsubscribe()
            Lifestolen.log.info("[WebViewManager::addSubscriptions] Received ready payload from WebView")

            CompletableFuture.completedFuture(object {
                val modules = Lifestolen.modules.map {
                    ModuleInfo(it.id, it.category.toString(), it.enabled)
                }
            })
        }

        toggleSub = bridge.onEventJson("toggleModule", TogglePayload::class.java) { _, payload ->
            Lifestolen.modules.find { it.id == payload.id }?.let { module ->
                if (module.enabled != payload.enabled) {
                    module.toggle()
                }
            }
        }
    }

    fun shutdown() {
        readySub?.unsubscribe()
        toggleSub?.unsubscribe()
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

    private data class TogglePayload(val id: String, val enabled: Boolean)
    private data class ModuleInfo(val id: String, val category: String, val enabled: Boolean)
}
