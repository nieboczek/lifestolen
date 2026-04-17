package nieboczek.lifestolen.gui

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.config.setting.KeybindSetting
import nieboczek.lifestolen.config.setting.NumberSetting
import nieboczek.lifestolen.config.setting.RangeSetting
import nieboczek.lifestolen.config.setting.Setting
import nieboczek.lifestolen.module.Module
import tytoo.grapheneui.api.GrapheneCore
import tytoo.grapheneui.api.bridge.GrapheneBridge
import tytoo.grapheneui.api.bridge.GrapheneBridgeSubscription
import tytoo.grapheneui.api.widget.GrapheneWebViewWidget
import java.util.concurrent.CompletableFuture

object WebViewManager {
    // We don't initialize it every time ConfigScreen is constructed to make WebView feel more responsive
    var webView: GrapheneWebViewWidget? = null
    val bridge: GrapheneBridge
        get() = webView!!.bridge()
    private val subs: ArrayList<GrapheneBridgeSubscription> = ArrayList()

    fun addSubscriptions(bridge: GrapheneBridge) {
        subs.add(bridge.onRequestJson("ready", Integer.TYPE) { _, _ ->
            Lifestolen.log.info("[WebViewManager::addSubscriptions] Received ready payload from WebView")

            CompletableFuture.completedFuture(object {
                val modules = Lifestolen.modules.map { module ->
                    ModuleInfo(
                        module.id,
                        module.category.toString(),
                        module.enabled,
                        module.settings.filter {
                            it.name != "Enabled"
                        }.map { setting ->
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

                                is KeybindSetting -> SettingInfo(
                                    setting.name,
                                    setting.value,
                                    "keybind"
                                )

                                else -> SettingInfo(
                                    setting.name,
                                    setting.value,
                                    "boolean"
                                )
                            }
                        }
                    )
                }
            })
        })

        subs.add(bridge.onEventJson("updateSetting", UpdateSettingPayload::class.java) { _, payload ->
            Lifestolen.modules.find { it.id == payload.moduleId }?.let { module ->
                module.settings.find { it.name == payload.name }?.let { setting ->
                    val convertedValue: Any = when (setting) {
                        is RangeSetting<*, *> if payload.value is List<*> -> {
                            val list = payload.value
                            val start = (list[0] as Number).toInt()
                            val end = (list[1] as Number).toInt()
                            start..end
                        }

                        else -> payload.value
                    }

                    @Suppress("UNCHECKED_CAST")
                    (setting as Setting<Any>).value = convertedValue
                }
            }
        })

        subs.add(bridge.onRequestJson("keyname", KeynamePayload::class.java) { _, payload ->
            return@onRequestJson CompletableFuture.completedFuture(
                KeynameResponse(
                    InputConstants.Type.KEYSYM.getOrCreate(payload.code).displayName.string
                )
            )
        })
    }

    fun shutdown() {
        subs.forEach { it.unsubscribe() }
        webView?.close()
    }

    fun settingUpdated(moduleId: String, name: String, value: Any) {
        bridge.emitJson("updateSetting", UpdateSettingPayload(moduleId, name, value))
    }

    fun keyPressed(code: Int, displayed: String, isReserved: Boolean) {
        bridge.emitJson("keydown", KeydownPayload(code, displayed, isReserved))
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

    private data class KeynamePayload(val code: Int)
    private data class KeynameResponse(val displayed: String)
    private data class KeydownPayload(val code: Int, val displayed: String, val isReserved: Boolean)
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
        val min: Any? = null,
        val max: Any? = null,
        val step: Any? = null,
        val unit: String? = null
    )
}
