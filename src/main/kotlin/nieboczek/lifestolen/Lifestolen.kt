package nieboczek.lifestolen

import com.mojang.authlib.GameProfile
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.network.chat.ChatType
import net.minecraft.network.chat.Component
import nieboczek.lifestolen.config.ClientConfig
import nieboczek.lifestolen.config.ConfigManager
import nieboczek.lifestolen.gui.ConfigScreen
import nieboczek.lifestolen.gui.WebViewManager
import nieboczek.lifestolen.module.*
import nieboczek.lifestolen.module.util.RotationUtil
import nieboczek.lifestolen.util.Commands
import nieboczek.lifestolen.util.Formatting
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tytoo.grapheneui.api.GrapheneCore
import java.awt.Color

class Lifestolen : ModInitializer, ClientModInitializer {
    companion object {
        const val MOD_ID: String = "lifestolen"
        const val CLIENT_NAME: String = "Lifestolen"

        val log: Logger = LoggerFactory.getLogger(CLIENT_NAME)
        val msgPrefix: Component = Formatting.red("LS ").append(Formatting.darkGray("» "))
        val modules: ArrayList<Module> = ArrayList()

        var cfg: ClientConfig? = null
        var killSwitch = false

        private var rainbowColorOffset = 0

        @JvmStatic
        fun render2d(context: GuiGraphicsExtractor) {
            if (killSwitch) return

            if (cfg!!.renderClientBrandText) {
                rainbowColorOffset += 2
                val hue = (rainbowColorOffset.mod(360)) / 360f
                val color = Color.HSBtoRGB(hue, 1f, 1f)

                val font = Minecraft.getInstance().font
                context.text(font, "KupaDupa v2.1.3.7", 4, 4, color, true)
            }

            modules.forEach { if (it.enabled) it.render2d(context) }
        }

        @JvmStatic
        fun render3d() {
            if (killSwitch) return

            modules.forEach { if (it.enabled) it.render3d() }
        }

        @JvmStatic
        fun toggleKillSwitch() {
            killSwitch = !killSwitch
            log.info("Set kill switch in title screen: $killSwitch")
        }
    }

    override fun onInitializeClient() {
        // UI DevTools config; http://127.0.0.1:21371/json
//        GrapheneCore.register(MOD_ID, tytoo.grapheneui.api.config.GrapheneConfig.builder().global(
//            tytoo.grapheneui.api.config.GrapheneGlobalConfig.builder().remoteDebugging(
//                tytoo.grapheneui.api.config.GrapheneRemoteDebugConfig.builder().port(21371)
//                    .allowedOrigins("https://chrome-devtools-frontend.appspot.com").build()
//            ).build()
//        ).build())

        GrapheneCore.register(MOD_ID)
    }

    override fun onInitialize() {
        ClientLifecycleEvents.CLIENT_STARTED.register { this.clientStarted() }
        ClientLifecycleEvents.CLIENT_STOPPING.register { this.clientStopping() }
        ClientTickEvents.END_CLIENT_TICK.register { mc -> this.clientTick(mc) }
        ClientPlayConnectionEvents.INIT.register { listener, _ -> this.initializeConnection(listener) }
        ClientReceiveMessageEvents.CHAT.register { _, _, sender, bound, _ -> this.receiveChatMessage(sender, bound) }
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ -> Commands.register(dispatcher) }
    }

    private fun clientStarted() {
        modules.add(KillAuraModule)
        modules.add(FakeLagModule)
        modules.add(ScaffoldModule)
        modules.add(TracersModule)
        modules.add(ESPModule)
        modules.add(AutoWebModule)
        modules.add(NoPushModule)
        modules.add(ChestESPModule)

        WebViewManager.initialize()
        ConfigManager.loadConfig()
    }

    private fun clientStopping() {
        WebViewManager.shutdown()
        ConfigManager.saveConfig()
    }

    private fun clientTick(mc: Minecraft) {
        val noScreen = mc.gui.screen() == null
        while (mc.options.keySocialInteractions.consumeClick()) {
            if (noScreen && !killSwitch) {
                mc.gui.setScreen(ConfigScreen())
            } else {
                mc.gui.setScreen(null)
            }
        }

        if (killSwitch) return
        mc.player ?: return
        RotationUtil.tick()

        val window = mc.window
        for (module in modules) {
            if (module.enabled) module.tick()
            if (noScreen) module.handleBindPress(window)
        }
    }

    private fun initializeConnection(listener: ClientPacketListener) {
        if (listener.getConnection().channel.pipeline().get("lifestolen_packet_intercept") == null) {
            listener.getConnection().channel.pipeline().addBefore(
                "packet_handler", "lifestolen_packet_intercept", FakeLagChannelHandler()
            )
        }
    }

    private fun receiveChatMessage(sender: GameProfile?, bound: ChatType.Bound?) {
        if (bound!!.chatType().`is`(ChatType.MSG_COMMAND_INCOMING)) {
            if (sender == null) {
                Minecraft.getInstance().player?.sendSystemMessage(
                    Component.literal("Sender was not set correctly due to being null")
                )
                return
            }
            Commands.lastSender = sender.name()
        }
    }
}
