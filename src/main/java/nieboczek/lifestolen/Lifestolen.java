package nieboczek.lifestolen;

import com.mojang.blaze3d.platform.Window;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import nieboczek.lifestolen.config.ClientConfig;
import nieboczek.lifestolen.config.ConfigManager;
import nieboczek.lifestolen.gui.ModuleGui;
import nieboczek.lifestolen.module.FakeLagModule;
import nieboczek.lifestolen.module.KillAuraModule;
import nieboczek.lifestolen.module.Module;
import nieboczek.lifestolen.module.ProximityModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static nieboczek.lifestolen.Formatting.*;

public final class Lifestolen implements ModInitializer {
    public static final Component MSG_PREFIX = red("LS ").append(darkGray("» "));
    public static final Logger LOG = LoggerFactory.getLogger("Lifestolen");
    public static final ArrayList<Module<?>> modules = new ArrayList<>();
    public static ClientConfig cfg;

    private static String lastSender = null;
    private static int rainbowColorOffset = 0;

    public static void render2d(GuiGraphics context) {
        if (cfg.renderClientBrandText) {
            rainbowColorOffset += 2;
            float hue = (rainbowColorOffset % 360) / 360f;
            int color = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);

            Font font = Minecraft.getInstance().font;
            context.drawString(font, "KupaDupa v2.1.3.7", 4, 4, color, true);
        }

        modules.forEach(m -> {
            if (m.isEnabled()) m.render2d(context);
        });
    }

    public static void render3d() {
        modules.forEach(m -> {
            if (m.isEnabled()) m.render3d();
        });
    }

    @Override
    public void onInitialize() {
        modules.add(new ProximityModule());
        modules.add(new KillAuraModule());
        modules.add(new FakeLagModule());

        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, bound, timestamp) -> {
            if (bound.chatType().is(ChatType.MSG_COMMAND_INCOMING)) {
                if (sender == null) {
                    Module.sendChat(Component.literal("Sender was not set to the right player due to being null"), Minecraft.getInstance());
                    return;
                }
                lastSender = sender.name();
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            boolean canHandleBinds = true;
            while (mc.options.keySocialInteractions.consumeClick()) {
                canHandleBinds = false;
                if (mc.screen instanceof ModuleGui moduleGui) {
                    if (moduleGui.consumeOpenGuiToggleGuard()) {
                        continue;
                    }

                    if (!moduleGui.isAwaitingBind()) {
                        mc.setScreen(null);
                    }
                } else {
                    mc.setScreen(new ModuleGui());
                }
            }

            if (mc.player == null) return;

            Window window = mc.getWindow();
            for (Module<?> module : modules) {
                if (module.isEnabled())
                    module.tick();

                if (canHandleBinds)
                    module.handleBindPress(window);
            }
        });

        ClientLifecycleEvents.CLIENT_STARTED.register(mc -> {
            ConfigManager.loadConfig();
            modules.forEach(m -> m.mc = mc);
        });

        ClientPlayConnectionEvents.INIT.register((listener, mc) -> {
            if (listener.getConnection().channel.pipeline().get("lifestolen_packet_intercept") == null)
                listener.getConnection().channel.pipeline().addBefore(
                        "packet_handler",
                        "lifestolen_packet_intercept",
                        new FakeLagChannelHandler()
                );
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register($ -> ConfigManager.saveConfig());

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildCtx) -> {
            dispatcher.register(createReplyCommand());
            dispatcher.register(createToggleCommand());
            dispatcher.register(createBindCommand());

            dispatcher.register(literal("lifestolen:reload_configs").executes(ctx -> {
                ConfigManager.loadConfig();
                return 1;
            }));
            dispatcher.register(literal("lifestolen:save_configs").executes(ctx -> {
                ConfigManager.saveConfig();
                return 1;
            }));

            for (Module<?> module : modules) {
                module.registerCommands(dispatcher, buildCtx);
            }
        });
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> createBindCommand() {
        LiteralArgumentBuilder<FabricClientCommandSource> bindCommand = literal("bind");

        for (Module<?> module : modules) {
            bindCommand.then(literal(module.getId())
                    .then(argument("key", string())
                            .executes(ctx -> {
                                int keycode = BindUtils.getKeycode(ctx.getArgument("key", String.class));
                                module.keybind = keycode;

                                MutableComponent keyLabel = niceBlue(BindUtils.getKeyLabel(keycode));
                                Module.sendChat(ctx, Component.literal("Bound module " + module.getDisplayName() + " to ").append(keyLabel));
                                return 1;
                            })
                    ));
        }

        return bindCommand;
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> createReplyCommand() {
        return literal("r")
                .then(argument("message", greedyString())
                        .executes(ctx -> {
                            if (lastSender != null) {
                                String message = getString(ctx, "message");
                                ctx.getSource().getPlayer().connection.sendCommand("msg " + lastSender + " " + message);
                            } else {
                                Module.sendChat(ctx, "No one to reply to");
                            }
                            return 1;
                        }));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> createToggleCommand() {
        LiteralArgumentBuilder<FabricClientCommandSource> toggleCommand = literal("t");

        for (Module<?> module : modules) {
            toggleCommand.then(literal(module.getId()).executes(ctx -> {
                module.toggle();
                MutableComponent status = module.isEnabled() ? green("enabled") : red("disabled");
                Module.sendChat(ctx, Component.literal("Module " + module.getDisplayName() + " has been ").append(status));
                return 1;
            }));
        }

        return toggleCommand;
    }
}
