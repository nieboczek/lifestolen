package nieboczek.lifestolen

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.core.Holder
import nieboczek.lifestolen.module.Module

object Commands {
    @Suppress("unchecked_cast")
    fun <T : Any> getResourceHolder(
        argName: String,
        ctx: CommandContext<FabricClientCommandSource>
    ): Holder.Reference<T> {
        return ctx.getArgument(argName, Holder.Reference::class.java) as Holder.Reference<T>
    }

    @JvmStatic
    @Suppress("unchecked_cast")
    fun <T> getResource(argName: String?, ctx: CommandContext<FabricClientCommandSource>): T {
        return ctx.getArgument(argName, Holder.Reference::class.java).value() as T
    }

    @JvmStatic
    fun stringListManipulator(
        list: MutableList<String>,
        commandName: String,
        elementName: String,
        objectName: String
    ): LiteralArgumentBuilder<FabricClientCommandSource?>? {
        return ClientCommandManager.literal(commandName)
            .executes { ctx: CommandContext<FabricClientCommandSource> ->
                Module.sendChat(ctx, "Contents of $objectName: $list")
                1
            }.then(
                ClientCommandManager.literal("add")
                    .then(
                        ClientCommandManager.argument<String>(elementName, StringArgumentType.word())
                            .executes { ctx: CommandContext<FabricClientCommandSource> ->
                                val string = StringArgumentType.getString(ctx, elementName)
                                list.add(string)
                                Module.sendChat(ctx, "Added $string to $objectName")
                                1
                            }
                    )
            ).then(
                ClientCommandManager.literal("remove")
                    .then(
                        ClientCommandManager.argument<String>(elementName, StringArgumentType.word())
                            .executes { ctx: CommandContext<FabricClientCommandSource> ->
                                val string = StringArgumentType.getString(ctx, elementName)
                                list.remove(string)
                                Module.sendChat(ctx, "Removed $string from $objectName")
                                1
                            }
                    )
            )
    }
}
