package nieboczek.lifestolen

import com.mojang.authlib.GameProfile
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStarted
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.commands.CommandBuildContext
import net.minecraft.network.chat.ChatType
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.PlayerChatMessage
import net.minecraft.resources.Identifier
import nieboczek.lifestolen.config.ClientConfig
import nieboczek.lifestolen.config.ConfigManager
import nieboczek.lifestolen.gui.FontLoader
import nieboczek.lifestolen.gui.ModuleGui
import nieboczek.lifestolen.module.FakeLagModule
import nieboczek.lifestolen.module.KillAuraModule
import nieboczek.lifestolen.module.Module
import nieboczek.lifestolen.module.ProximityModule
import nieboczek.lifestolen.util.BindUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.time.Instant

class Lifestolen : ModInitializer {
    companion object {
        const val MOD_ID: String = "lifestolen"
        const val CLIENT_NAME: String = "Lifestolen"

        @JvmField
        val log: Logger = LoggerFactory.getLogger(CLIENT_NAME)
        val msgPrefix: Component = Formatting.red("LS ").append(Formatting.darkGray("» "))
        val modules: ArrayList<Module> = ArrayList()

        var cfg: ClientConfig? = null
        var uiFont: Font? = null

        private var lastSender: String? = null
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

        @JvmStatic
        fun id(id: String): Identifier {
            return Identifier.fromNamespaceAndPath(MOD_ID, id)
        }
    }

    override fun onInitialize() {
        ClientLifecycleEvents.CLIENT_STARTED.register(ClientStarted { _ -> this.clientStarted() })
        ClientLifecycleEvents.CLIENT_STOPPING.register(ClientStopping { _ -> ConfigManager.saveConfig() })
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { mc -> this.clientTick(mc) })
        ClientPlayConnectionEvents.INIT.register(ClientPlayConnectionEvents.Init { listener, _ ->
            this.initializeConnection(listener)
        })
        ClientReceiveMessageEvents.CHAT.register(ClientReceiveMessageEvents.Chat { _, _, sender: GameProfile?, bound: ChatType.Bound?, _ ->
            this.receiveChatMessage(sender, bound)
        })
        ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher: CommandDispatcher<FabricClientCommandSource>, buildCtx: CommandBuildContext ->
            this.registerCommands(dispatcher, buildCtx)
        })
    }

    private fun clientStarted() {
        modules.add(ProximityModule)
        modules.add(KillAuraModule)
        modules.add(FakeLagModule)

        ConfigManager.loadConfig()
        log.info("[Lifestolen::clientStarted] Loading UI font")
        uiFont = FontLoader.loadUiFont()
        log.info("[Lifestolen::clientStarted] UI font loaded")
    }

    private fun clientTick(mc: Minecraft) {
        var canHandleBinds = true
        while (mc.options.keySocialInteractions.consumeClick()) {
            canHandleBinds = false
            if (mc.screen is ModuleGui) {
                val gui = mc.screen as ModuleGui
                if (gui.consumeOpenGuiToggleGuard()) {
                    continue
                }

                if (!gui.isAwaitingBind) {
                    mc.setScreen(null)
                }
            } else {
                mc.setScreen(ModuleGui())
            }
        }

        mc.player ?: return

        val window = mc.window
        for (module in modules) {
            if (module.enabled) module.tick()
            if (canHandleBinds) module.handleBindPress(window)
        }
    }

    private fun initializeConnection(listener: ClientPacketListener) {
        if (listener.getConnection().channel.pipeline().get("lifestolen_packet_intercept") == null) {
            listener.getConnection().channel.pipeline().addBefore(
                "packet_handler",
                "lifestolen_packet_intercept",
                FakeLagChannelHandler()
            )
        }
    }

    private fun receiveChatMessage(sender: GameProfile?, bound: ChatType.Bound?) {
        if (bound!!.chatType().`is`(ChatType.MSG_COMMAND_INCOMING)) {
            if (sender == null) {
                Module.sendChat(
                    Component.literal("Sender was not set correctly due to being null"),
                    Minecraft.getInstance()
                )
                return
            }
            lastSender = sender.name()
        }
    }

    private fun registerCommands(
        dispatcher: CommandDispatcher<FabricClientCommandSource>,
        buildCtx: CommandBuildContext
    ) {
        dispatcher.register(createReplyCommand())
        dispatcher.register(createToggleCommand())
        dispatcher.register(createBindCommand())

        dispatcher.register(
            ClientCommandManager.literal("lifestolen:reload_configs")
                .executes { _: CommandContext<FabricClientCommandSource> ->
                    ConfigManager.loadConfig()
                    1
                }
        )
        dispatcher.register(
            ClientCommandManager.literal("lifestolen:save_configs")
                .executes { _: CommandContext<FabricClientCommandSource> ->
                    ConfigManager.saveConfig()
                    1
                }
        )

        for (module in modules) {
            module.registerCommands(dispatcher, buildCtx)
        }
    }

    private fun createBindCommand(): LiteralArgumentBuilder<FabricClientCommandSource> {
        val bindCommand = ClientCommandManager.literal("bind")

        for (module in modules) {
            bindCommand.then(
                ClientCommandManager.literal(module.id)
                    .then(
                        ClientCommandManager.argument<String>("key", StringArgumentType.string())
                            .executes { ctx: CommandContext<FabricClientCommandSource> ->
                                val keycode =
                                    BindUtils.getKeycode(ctx.getArgument("key", String::class.java))
                                module.keybind = keycode

                                val keyLabel = Formatting.niceBlue(BindUtils.getKeyLabel(keycode))
                                Module.sendChat(
                                    ctx,
                                    Component.literal("Bound module ${module.id} to ")
                                        .append(keyLabel)
                                )
                                1
                            }
                    )
            )
        }

        return bindCommand
    }

    private fun createReplyCommand(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return ClientCommandManager.literal("r")
            .then(
                ClientCommandManager.argument<String>("message", StringArgumentType.greedyString())
                    .executes { ctx: CommandContext<FabricClientCommandSource> ->
                        if (lastSender != null) {
                            val message = StringArgumentType.getString(ctx, "message")
                            ctx.getSource()!!.player.connection.sendCommand("msg $lastSender $message")
                        } else {
                            Module.sendChat(ctx, "No one to reply to")
                        }
                        1
                    }
            )
    }

    private fun createToggleCommand(): LiteralArgumentBuilder<FabricClientCommandSource> {
        val toggleCommand = ClientCommandManager.literal("t")

        for (module in modules) {
            toggleCommand.then(
                ClientCommandManager.literal(module.id)
                    .executes { ctx: CommandContext<FabricClientCommandSource> ->
                        module.toggle()
                        val status = if (module.enabled) Formatting.green("enabled") else Formatting.red("disabled")
                        Module.sendChat(
                            ctx,
                            Component.literal("Module ${module.id} has been ").append(status)
                        )
                        1
                    }
            )
        }

        return toggleCommand
    }
}
