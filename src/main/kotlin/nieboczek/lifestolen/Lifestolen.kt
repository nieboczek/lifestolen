package nieboczek.lifestolen

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.TitleScreen
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity
import nieboczek.lifestolen.Lifestolen.toggleKillSwitch
import nieboczek.lifestolen.command.Commands
import nieboczek.lifestolen.config.ClientConfig
import nieboczek.lifestolen.config.ConfigManager
import nieboczek.lifestolen.gui.ConfigScreen
import nieboczek.lifestolen.gui.Fonts
import nieboczek.lifestolen.gui.friedsvg.FriedSvg
import nieboczek.lifestolen.gui.notification.AntiCheatDetector
import nieboczek.lifestolen.gui.notification.Notifications
import nieboczek.lifestolen.module.*
import nieboczek.lifestolen.module.util.RotationUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color

object Lifestolen : ClientModInitializer {
    const val MOD_ID = "lifestolen"
    const val CLIENT_NAME = "Lifestolen"

    private const val CLIENT_BRAND_TEXT = "$CLIENT_NAME v${BuildInfo.MOD_VERSION}"

    val log: Logger = LoggerFactory.getLogger(CLIENT_NAME)
    val modules = mutableListOf<Module>()

    lateinit var cfg: ClientConfig
        private set
    var killSwitch = false
        private set

    /** Modules that have been enabled before the kill switch was activated. */
    private val killSwitchedModules = mutableListOf<Module>()
    private val mc = Minecraft.getInstance()
    private var firstTickWithPlayer = false
    private var seenTitleScreen: Byte = 0
    private var rainbowColorOffset = 0

    fun identifier(path: String) = Identifier.fromNamespaceAndPath(MOD_ID, path)
    fun isFriend(player: Entity) = cfg.friends.contains(player.name.string)

    fun reset() {
        if (FreeCamModule.isEnabled()) FreeCamModule.toggle()
        RotationUtil.reset()
    }

    fun render2d(graphics: GuiGraphicsExtractor, dt: Float) {
        if (killSwitch) return

        val screen = mc.gui.screen()

        // Stages:
        //  0: Mojang boot screen, don't show so the player can press the kill switch key without getting caught
        //  1: TitleScreen just seen, don't show so the player can press the kill switch key without getting caught
        //  2: just left TitleScreen, show the GUI as per the user preferences
        if (seenTitleScreen == 0.toByte()) {
            if (screen is TitleScreen) seenTitleScreen = 1
            return
        } else if (seenTitleScreen == 1.toByte()) {
            if (screen !is TitleScreen) seenTitleScreen = 2
            else return
        }

        if (screen is ConfigScreen) return

        if (cfg.renderClientBrandText) {
            rainbowColorOffset += 2
            val hue = (rainbowColorOffset % 360) / 360f
            val color = Color.HSBtoRGB(hue, 1f, 1f)
            graphics.text(Fonts.font, CLIENT_BRAND_TEXT, 4, 4, color, true)
        }

        if (screen == null) Notifications.render(graphics, dt)
    }

    fun render3d() {
        // killSwitch checked for in caller (GameRendererMixin#renderLevel)
        modules.forEach { if (it.enabled) it.render3d() }
    }

    /** Omits function calls that might require a player, unlike [toggleKillSwitch] */
    fun toggleKillSwitchInMenu() {
        killSwitch = !killSwitch
    }

    fun toggleKillSwitch() {
        killSwitch = !killSwitch
        toggleKillSwitchedModules()
    }

    private fun toggleKillSwitchedModules() {
        if (killSwitch) {
            reset()
            Notifications.clear()
            modules.forEach {
                if (it.enabled) {
                    it.toggle()
                    killSwitchedModules.add(it)
                }
            }
        } else {
            modules.forEach { if (killSwitchedModules.contains(it)) it.toggle() }
            killSwitchedModules.clear()
        }
    }

    override fun onInitializeClient() {
        FakeLagChannelHandler.init()
        AntiCheatDetector.init()
        FriedSvg.init()
        Commands.init()

        ClientLifecycleEvents.CLIENT_STARTED.register { this.clientStarted() }
        ClientLifecycleEvents.CLIENT_STOPPING.register { this.clientStopping() }
        ClientTickEvents.END_CLIENT_TICK.register { mc -> this.clientTick(mc) }
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
        modules.add(BoatFlyModule)
        modules.add(ScaffoldModule)

        modules.add(NoRenderModule)
        modules.add(FreeCamModule)
        modules.add(TracersModule)
        modules.add(ESPModule)
        modules.add(ChestESPModule)
        modules.add(FullBrightModule)
        modules.add(XRayModule)

        log.info("Loaded {} modules", modules.size)
        cfg = ConfigManager.loadConfig()
        Fonts.load()
    }

    private fun clientStopping() {
        ConfigManager.saveConfig()
    }

    private fun clientTick(mc: Minecraft) {
        val noScreen = mc.gui.screen() == null
        while (mc.options.keySocialInteractions.consumeClick()) {
            if (noScreen && !killSwitch) mc.gui.setScreen(ConfigScreen())
            else mc.gui.setScreen(null)
        }

        if (killSwitch || mc.player == null) return
        if (!firstTickWithPlayer) {
            firstTickWithPlayer = true
            toggleKillSwitchedModules()
        }

        val window = mc.window
        for (module in modules) {
            if (module.enabled) module.tick()
            if (noScreen) module.handleBindPress(window)
        }
    }
}
