package nieboczek.lifestolen;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.core.Holder;
import nieboczek.lifestolen.module.Module;

import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class Commands {
    @SuppressWarnings("unchecked")
    public static <T> Holder.Reference<T> getResourceHolder(String argName, CommandContext<FabricClientCommandSource> ctx) {
        return ctx.getArgument(argName, Holder.Reference.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getResource(String argName, CommandContext<FabricClientCommandSource> ctx) {
        return (T) ctx.getArgument(argName, Holder.Reference.class).value();
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> stringListManipulator(List<String> list, String commandName, String objectName) {
        return literal(commandName).executes(ctx -> {
            Module.sendChat(ctx, "Contents of " + objectName + ": " + list);
            return 1;
        }).then(
                literal("add")
                        .then(argument("string", word())
                                .executes(ctx -> {
                                    String string = getString(ctx, "string");
                                    list.add(string);
                                    Module.sendChat(ctx, "Added " + string + " to " + objectName);
                                    return 1;
                                }))
        ).then(
                literal("remove")
                        .then(argument("string", word())
                                .executes(ctx -> {
                                    String string = getString(ctx, "string");
                                    list.remove(string);
                                    Module.sendChat(ctx, "Removed " + string + " from " + objectName);
                                    return 1;
                                }))
        );
    }

    private Commands() {
    }
}
