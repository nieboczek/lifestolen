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
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity
import nieboczek.lifestolen.config.ClientConfig
import nieboczek.lifestolen.config.ConfigManager
import nieboczek.lifestolen.gui.ConfigScreen
import nieboczek.lifestolen.module.*
import nieboczek.lifestolen.module.util.RotationUtil
import nieboczek.lifestolen.util.Commands
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color

object Lifestolen : ModInitializer, ClientModInitializer {
    const val MOD_ID: String = "lifestolen"
    const val CLIENT_NAME: String = "Lifestolen"

    val log: Logger = LoggerFactory.getLogger(CLIENT_NAME)
    val modules = ArrayList<Module>()

    var cfg: ClientConfig? = null
    var killSwitch = false

    private var rainbowColorOffset = 0

    fun identifier(path: String): Identifier = Identifier.fromNamespaceAndPath(MOD_ID, path)
    fun isFriend(player: Entity): Boolean = cfg!!.friends.contains(player.name.string)

    // TODO: remove this function and switch to GUI widgets completely
    fun displayStatus(msg: Component) {
        Minecraft.getInstance().player?.sendOverlayMessage(msg)
    }

    fun render2d(context: GuiGraphicsExtractor) {
        if (killSwitch) return

        if (cfg!!.renderClientBrandText) {
            rainbowColorOffset += 2
            val hue = (rainbowColorOffset.mod(360)) / 360f
            val color = Color.HSBtoRGB(hue, 1f, 1f)

            val font = Minecraft.getInstance().font
            context.text(font, "$CLIENT_NAME v${BuildInfo.MOD_VERSION}", 4, 4, color, true)
        }

        modules.forEach { if (it.enabled) it.render2d(context) }
    }

    fun render3d() {
        if (killSwitch) return
        modules.forEach { if (it.enabled) it.render3d() }
    }

    fun toggleKillSwitch() {
        killSwitch = !killSwitch
        // we never set Module#enabled, that's intended
        if (killSwitch) modules.forEach { if (it.enabled) it.disable() }
        else modules.forEach { if (it.enabled) it.enable() }
    }

    override fun onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register { this.clientStarted() }
        ClientLifecycleEvents.CLIENT_STOPPING.register { this.clientStopping() }
        ClientTickEvents.END_CLIENT_TICK.register { mc -> this.clientTick(mc) }
        ClientPlayConnectionEvents.INIT.register { listener, _ -> this.initializeConnection(listener) }
        ClientReceiveMessageEvents.CHAT.register { _, _, sender, bound, _ -> this.receiveChatMessage(sender, bound) }
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ -> Commands.register(dispatcher) }
    }

    override fun onInitialize() {}

    private fun clientStarted() {
        log.info("cool version: ${BuildInfo.MOD_VERSION}")

        modules.add(KillAuraModule)
        modules.add(AutoTotemModule)
        modules.add(AutoWebModule)
        modules.add(AutoTrapModule)

        modules.add(FakeLagModule)
        modules.add(NoPushModule)
        modules.add(FlyModule)
        modules.add(NoFallModule)
        modules.add(ScaffoldModule)

        modules.add(TracersModule)
        modules.add(ESPModule)
        modules.add(ChestESPModule)
        modules.add(FullBrightModule)
        modules.add(XRayModule)

        ConfigManager.loadConfig()

        modules.forEach { if (it.enabled) it.enable() }
    }

    private fun clientStopping() = ConfigManager.saveConfig()

    private fun clientTick(mc: Minecraft) {
        val noScreen = mc.gui.screen() == null
        while (mc.options.keySocialInteractions.consumeClick()) {
            if (noScreen && !killSwitch) mc.gui.setScreen(ConfigScreen())
            else mc.gui.setScreen(null)
        }

        if (killSwitch || mc.player == null) return
        RotationUtil.tick()

        val window = mc.window
        for (module in modules) {
            if (module.enabled) module.tick()
            if (noScreen) module.handleBindPress(window)
        }
    }

    private fun initializeConnection(listener: ClientPacketListener) {
        val pipeline = listener.getConnection().channel.pipeline()
        if (pipeline.get("lifestolen_packet_intercept") == null)
            pipeline.addBefore("packet_handler", "lifestolen_packet_intercept", FakeLagChannelHandler())
    }

    private fun receiveChatMessage(sender: GameProfile?, bound: ChatType.Bound?) {
        if (bound!!.chatType().`is`(ChatType.MSG_COMMAND_INCOMING)) {
            if (sender == null) {
                displayStatus(Component.literal("Sender was not set correctly due to being null"))
                return
            }
            Commands.lastSender = sender.name()
        }
    }
}
