package nieboczek.lifestolen.gui

import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.config.setting.NumberSetting
import nieboczek.lifestolen.config.setting.RangeSetting
import nieboczek.lifestolen.config.setting.Setting
import tytoo.grapheneui.api.GrapheneCore
import tytoo.grapheneui.api.bridge.GrapheneBridge
import tytoo.grapheneui.api.bridge.GrapheneBridgeSubscription
import tytoo.grapheneui.api.widget.GrapheneWebViewWidget
import java.util.concurrent.CompletableFuture

object WebViewManager {
    // We don't initialize it every time ConfigScreen is constructed to make WebView feel more responsive
    var webView: GrapheneWebViewWidget? = null
    private var readySub: GrapheneBridgeSubscription? = null
    private var toggleSub: GrapheneBridgeSubscription? = null
    private var updateSettingSub: GrapheneBridgeSubscription? = null

    fun addSubscriptions(bridge: GrapheneBridge) {
        readySub = bridge.onRequestJson("ready", Integer.TYPE) { _, _ ->
            readySub?.unsubscribe()
            Lifestolen.log.info("[WebViewManager::addSubscriptions] Received ready payload from WebView")

            CompletableFuture.completedFuture(object {
                val modules = Lifestolen.modules.map { module ->
                    ModuleInfo(
                        module.id,
                        module.category.toString(),
                        module.enabled,
                        module.settings.map { setting ->
                            when (setting) {
                                is NumberSetting<*> -> SettingInfo(
                                    setting.name,
                                    setting.value,
                                    if (setting.allowed.start is Int) "int" else "float",
                                    setting.allowed.start as Any,
                                    setting.allowed.endInclusive as Any,
                                    null,
                                    setting.suffix
                                )
                                is RangeSetting<*, *> -> SettingInfo(
                                    setting.name,
                                    setting.value,
                                    "intRange",
                                    setting.allowed.start as Any,
                                    setting.allowed.endInclusive as Any,
                                    null,
                                    setting.suffix
                                )
                                else -> SettingInfo(
                                    setting.name,
                                    setting.value,
                                    "boolean",
                                    null,
                                    null,
                                    null,
                                    null
                                )
                            }
                        }
                    )
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

        updateSettingSub = bridge.onEventJson("updateSetting", UpdateSettingPayload::class.java) { _, payload ->
            Lifestolen.modules.find { it.id == payload.moduleId }?.let { module ->
                module.settings.find { it.name == payload.name }?.let { setting ->
                    @Suppress("UNCHECKED_CAST")
                    (setting as Setting<Any>).value = payload.value
                }
            }
        }
    }

    fun shutdown() {
        readySub?.unsubscribe()
        toggleSub?.unsubscribe()
        updateSettingSub?.unsubscribe()
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
    private data class UpdateSettingPayload(val moduleId: String, val name: String, val value: Any)
    private data class ModuleInfo(
        val id: String,
        val category: String,
        val enabled: Boolean,
        val settings: List<SettingInfo>
    )
    private data class SettingInfo(
        val name: String,
        val value: Any?,
        val type: String,
        val min: Any?,
        val max: Any?,
        val step: Any?,
        val unit: String?
    )
}
