package nieboczek.lifestolen;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import nieboczek.lifestolen.module.KillAuraModule;
import nieboczek.lifestolen.module.Module;
import nieboczek.lifestolen.module.ProximityModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static nieboczek.lifestolen.Formatting.*;

public final class Lifestolen implements ModInitializer {
    public static final Component MSG_PREFIX = red("LS ").append(darkGray("» "));
    public static final Logger LOG = LoggerFactory.getLogger("Lifestolen");
    public static ClientConfig cfg;

    private static final ArrayList<Module<?>> modules = new ArrayList<>();
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
            if (m.enabled) m.render2d(context);
        });
    }

    public static void render3d() {
        modules.forEach(m -> {
            if (m.enabled) m.render3d();
        });
    }

    @Override
    public void onInitialize() {
        modules.add(new ProximityModule());
        modules.add(new KillAuraModule());

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
            if (mc.player == null)
                return;

            for (Module<?> module : modules)
                if (module.enabled)
                    module.tick();
        });

        ClientLifecycleEvents.CLIENT_STARTED.register(mc -> {
            ConfigManager.loadConfigs(modules);
            modules.forEach(m -> m.mc = mc);
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register($ -> ConfigManager.saveConfigs(modules));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildCtx) -> {
            dispatcher.register(createReplyCommand());
            dispatcher.register(createToggleCommand());
            dispatcher.register(literal("lifestolen:reload_configs").executes(ctx -> {
                ConfigManager.loadConfigs(modules);
                return 1;
            }));
            dispatcher.register(literal("lifestolen:save_configs").executes(ctx -> {
                ConfigManager.saveConfigs(modules);
                return 1;
            }));

            for (Module<?> module : modules) {
                module.registerCommands(dispatcher, buildCtx);
            }
        });
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
                module.enabled = !module.enabled;
                MutableComponent status = module.enabled ? green("enabled") : red("disabled");
                Module.sendChat(ctx, Component.literal("Module " + module.getId() + " has been ").append(status));
                return 1;
            }));
        }

        return toggleCommand;
    }
}
