package nieboczek.lifestolen;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import nieboczek.lifestolen.module.Module;
import nieboczek.lifestolen.module.ProximityModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static nieboczek.lifestolen.Formatting.*;

public final class Lifestolen implements ModInitializer {
    private static final ArrayList<Module<?>> modules = new ArrayList<>();

    public static final Component MSG_PREFIX = red("LS ").append(darkGray("» "));
    public static final Logger LOG = LoggerFactory.getLogger("Lifestolen");

    public static String lastSender = null;

    @Override
    public void onInitialize() {
        modules.add(new ProximityModule());

        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, bound, timestamp) -> {
            if (bound.chatType().is(ChatType.MSG_COMMAND_INCOMING)) {
                if (lastSender == null) {
                    Module.sendChat(Component.literal("Sender was not set to the right due to being null"), Minecraft.getInstance());
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
                    module.tick(mc);
        });

        ClientLifecycleEvents.CLIENT_STARTED.register($ -> ConfigManager.loadConfigs(modules));

        ClientLifecycleEvents.CLIENT_STOPPING.register($ -> ConfigManager.saveConfigs(modules));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildCtx) -> {
            dispatcher.register(createReplyCommand());
            dispatcher.register(createToggleCommand());
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
