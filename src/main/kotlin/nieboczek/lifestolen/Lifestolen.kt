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
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.network.chat.ChatType
import net.minecraft.network.chat.Component
import nieboczek.lifestolen.config.ClientConfig
import nieboczek.lifestolen.config.ConfigManager
import nieboczek.lifestolen.gui.ConfigScreen
import nieboczek.lifestolen.gui.WebViewManager
import nieboczek.lifestolen.module.ScaffoldModule
import nieboczek.lifestolen.module.FakeLagModule
import nieboczek.lifestolen.module.KillAuraModule
import nieboczek.lifestolen.module.Module
import nieboczek.lifestolen.module.ProximityModule
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

        private var rainbowColorOffset: Int = 0

        @JvmStatic
        fun render2d(context: GuiGraphics) {
            if (cfg!!.renderClientBrandText) {
                rainbowColorOffset += 2
                val hue = (rainbowColorOffset % 360) / 360f
                val color = Color.HSBtoRGB(hue, 1f, 1f)

                val font = Minecraft.getInstance().font
                context.drawString(font, "KupaDupa v2.1.3.7", 4, 4, color, true)
            }

            modules.forEach { if (it.enabled) it.render2d(context) }
        }

        @JvmStatic
        fun render3d() {
            modules.forEach { if (it.enabled) it.render3d() }
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
        ClientLifecycleEvents.CLIENT_STARTED.register { _ -> this.clientStarted() }
        ClientLifecycleEvents.CLIENT_STOPPING.register { _ -> this.clientStopping() }
        ClientTickEvents.END_CLIENT_TICK.register { mc -> this.clientTick(mc) }
        ClientPlayConnectionEvents.INIT.register { listener, _ -> this.initializeConnection(listener) }
        ClientReceiveMessageEvents.CHAT.register { _, _, sender, bound, _ -> this.receiveChatMessage(sender, bound) }
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ -> Commands.register(dispatcher) }
    }

    private fun clientStarted() {
        modules.add(ProximityModule)
        modules.add(KillAuraModule)
        modules.add(FakeLagModule)
        modules.add(ScaffoldModule)

        WebViewManager.initialize()
        ConfigManager.loadConfig()
    }

    private fun clientStopping() {
        WebViewManager.shutdown()
        ConfigManager.saveConfig()
    }

    private fun clientTick(mc: Minecraft) {
        val noScreen = mc.screen == null
        while (mc.options.keySocialInteractions.consumeClick()) {
            if (noScreen) {
                mc.setScreen(ConfigScreen())
            } else {
                mc.setScreen(null)
            }
        }

        RotationUtil.tick()

        mc.player ?: return

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
                Minecraft.getInstance().player?.displayClientMessage(
                    Component.literal("Sender was not set correctly due to being null"), false
                )
                return
            }
            Commands.lastSender = sender.name()
        }
    }
}
