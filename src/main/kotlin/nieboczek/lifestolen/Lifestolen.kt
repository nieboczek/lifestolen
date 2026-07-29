package nieboczek.lifestolen

import com.mojang.authlib.GameProfile
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.network.chat.ChatType
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity
import nieboczek.lifestolen.config.ClientConfig
import nieboczek.lifestolen.config.ConfigManager
import nieboczek.lifestolen.gui.ConfigScreen
import nieboczek.lifestolen.gui.friedsvg.FriedSvg
import nieboczek.lifestolen.module.*
import nieboczek.lifestolen.module.util.RotationUtil
import nieboczek.lifestolen.util.Commands
import nieboczek.lifestolen.util.FontLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color

object Lifestolen : ClientModInitializer {
    const val MOD_ID = "lifestolen"
    const val CLIENT_NAME = "Lifestolen"

    val log: Logger = LoggerFactory.getLogger(CLIENT_NAME)
    val modules = mutableListOf<Module>()

    lateinit var cfg: ClientConfig
        private set
    lateinit var fontBig: Font
        private set
    lateinit var font: Font
        private set
    lateinit var fontSmall: Font
        private set
    lateinit var fontExtraSmall: Font
        private set

    var killSwitch = false
        private set

    private var firstTickWithPlayer = false
    private var rainbowColorOffset = 0

    fun identifier(path: String) = Identifier.fromNamespaceAndPath(MOD_ID, path)
    fun isFriend(player: Entity) = cfg.friends.contains(player.name.string)

    // TODO: remove this function and switch to GUI widgets completely
    fun displayStatus(msg: Component) {
        Minecraft.getInstance().player?.sendOverlayMessage(msg)
    }

    fun render2d(context: GuiGraphicsExtractor) {
        if (killSwitch) return

        if (cfg.renderClientBrandText && Minecraft.getInstance().gui.screen() !is ConfigScreen) {
            rainbowColorOffset += 2
            val hue = (rainbowColorOffset % 360) / 360f
            val color = Color.HSBtoRGB(hue, 1f, 1f)
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
        if (!firstTickWithPlayer) return

        // we never set Module#enabled, that's intended
        if (killSwitch) modules.forEach { if (it.enabled) it.disable() }
        else modules.forEach { if (it.enabled) it.enable() }
    }

    override fun onInitializeClient() {
        FriedSvg.initialize()
        ClientLifecycleEvents.CLIENT_STARTED.register { this.clientStarted() }
        ClientLifecycleEvents.CLIENT_STOPPING.register { this.clientStopping() }
        ClientTickEvents.END_CLIENT_TICK.register { mc -> this.clientTick(mc) }
        ClientPlayConnectionEvents.INIT.register { listener, _ -> this.initializeConnection(listener) }
        ClientReceiveMessageEvents.CHAT.register { _, _, sender, bound, _ -> this.receiveChatMessage(sender, bound) }
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ -> Commands.register(dispatcher) }
    }

    private fun clientStarted() {
        modules.add(KillAuraModule)
        modules.add(AutoTotemModule)
        modules.add(AutoWebModule)
        modules.add(AutoTrapModule)

        modules.add(FakeLagModule)
        modules.add(NoPushModule)
        modules.add(FlyModule)
        modules.add(NoFallModule)
        modules.add(InvMoveModule)
        modules.add(ScaffoldModule)

        modules.add(FreeCamModule)
        modules.add(TracersModule)
        modules.add(ESPModule)
        modules.add(ChestESPModule)
        modules.add(FullBrightModule)
        modules.add(XRayModule)

        log.info("Loaded {} modules", modules.size)
        cfg = ConfigManager.loadConfig()
        fontBig = FontLoader.loadUiFont(16f, 5f, "ui_font_big")
        font = FontLoader.loadUiFont(12f, 1f, "ui_font")
        fontSmall = FontLoader.loadUiFont(8f, -1f, "ui_font_small")
        fontExtraSmall = FontLoader.loadUiFont(6f, -3f, "ui_font_extra_small")
    }

    private fun clientStopping() = ConfigManager.saveConfig()

    private fun clientTick(mc: Minecraft) {
        val noScreen = mc.gui.screen() == null
        while (mc.options.keySocialInteractions.consumeClick()) {
            if (noScreen && !killSwitch) mc.gui.setScreen(ConfigScreen())
            else mc.gui.setScreen(null)
        }

        if (killSwitch || mc.player == null) return
        if (!firstTickWithPlayer) {
            firstTickWithPlayer = true
            modules.forEach { if (it.enabled) it.enable() }
        }
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
