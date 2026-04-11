package nieboczek.lifestolen.module

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.arguments.ResourceArgument
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB
import nieboczek.lifestolen.Commands
import nieboczek.lifestolen.Commands.stringListManipulator
import nieboczek.lifestolen.Formatting
import java.util.function.Predicate

object ProximityModule : Module("Proximity", Category.COMBAT) {
    val playerWhitelist = mutableListOf<String>()
    val entities = mutableMapOf<EntityType<*>, EntityParameters>()

    private val BIG_AABB = AABB(-65535.0, -65535.0, -65535.0, 65535.0, 65535.0, 65535.0)

    override fun tick() {
        // TODO: multiple mobs at once
        var entityCandidate: Entity? = null
        var paramsCandidate: EntityParameters? = null
        var distanceCandidate = 0.0
        val entities = player.level().getEntities(mc.player, BIG_AABB, Predicate { _: Entity? -> true })

        for (entity in entities) {
            val params = this.entities[entity.type] ?: continue
            if (playerWhitelist.contains(entity.name.string)) continue

            val priority = params.priority
            val distance = player.distanceTo(entity).toDouble()

            if (distance < params.distance && (entityCandidate == null || priority > paramsCandidate!!.priority || (priority == paramsCandidate.priority && distance < distanceCandidate))) {
                paramsCandidate = params
                entityCandidate = entity
                distanceCandidate = distance
            }
        }

        if (paramsCandidate == null) return
        // TODO: Remove sendStatus and use custom widgets instead
        sendStatus(
            paramsCandidate.getName(entityCandidate!!).append(Formatting.green(" ⇔ "))
                .append(Formatting.red(String.format("%.2fm", distanceCandidate))), mc
        )
    }

    override fun registerCommands(
        dispatcher: CommandDispatcher<FabricClientCommandSource>,
        context: CommandBuildContext
    ) {
        val distanceCommand = ClientCommandManager.literal("distance")
        val priorityCommand = ClientCommandManager.literal("priority")

        distanceCommand.then(
            ClientCommandManager.argument<Holder.Reference<EntityType<*>>>(
                "entity",
                ResourceArgument.resource(context, Registries.ENTITY_TYPE)
            )
                .executes { ctx: CommandContext<FabricClientCommandSource> -> this.printDistanceCommand(ctx) }
                .then(
                    ClientCommandManager.argument<Double>("distance", DoubleArgumentType.doubleArg(0.0, 1000.0))
                        .executes { ctx: CommandContext<FabricClientCommandSource> ->
                            this.setDistanceCommand(ctx)
                        }
                )
        )

        priorityCommand.then(
            ClientCommandManager.argument<Holder.Reference<EntityType<*>>>(
                "entity",
                ResourceArgument.resource(context, Registries.ENTITY_TYPE)
            )
                .executes { ctx: CommandContext<FabricClientCommandSource> -> this.printPriorityCommand(ctx) }
                .then(
                    ClientCommandManager.argument<Int>("priority", IntegerArgumentType.integer())
                        .executes { ctx: CommandContext<FabricClientCommandSource> ->
                            this.setPriorityCommand(
                                ctx
                            )
                        }
                )
        )

        dispatcher.register(
            ClientCommandManager.literal("proximity")
                .then(stringListManipulator(playerWhitelist, "player_whitelist", "player", "player whitelist"))
                .then(distanceCommand)
                .then(priorityCommand)
        )
    }

    private fun printDistanceCommand(ctx: CommandContext<FabricClientCommandSource>): Int {
        val type = Commands.getResource<EntityType<*>>("entity", ctx)
        val params = entities[type]
        if (params == null) {
            sendChat(ctx, Formatting.red("No distance option found for ").append(type.description))
            return 0
        }

        sendChat(ctx, Component.literal("Distance for ").append(params.getName(type)).append(" is ${params.distance}"))
        return 1
    }

    private fun setDistanceCommand(ctx: CommandContext<FabricClientCommandSource>): Int {
        val type = Commands.getResource<EntityType<*>>("entity", ctx)
        val params = entities.computeIfAbsent(type) { EntityParameters() }
        val name = params.getName(type)

        val distance = DoubleArgumentType.getDouble(ctx, "distance")
        params.distance = distance

        sendChat(ctx, Component.literal("Set distance for ").append(name).append(" to $distance"))
        return 1
    }

    private fun printPriorityCommand(ctx: CommandContext<FabricClientCommandSource>): Int {
        val type = Commands.getResource<EntityType<*>>("entity", ctx)
        val params = entities[type]
        if (params == null) {
            sendChat(ctx, Formatting.red("No priority option found for ").append(type.description))
            return 0
        }

        sendChat(ctx, Component.literal("Priority for ").append(params.getName(type)).append(" is ${params.distance}"))
        return 1
    }

    private fun setPriorityCommand(ctx: CommandContext<FabricClientCommandSource>): Int {
        val type = Commands.getResource<EntityType<*>>("entity", ctx)
        val params = entities.computeIfAbsent(type) { EntityParameters() }
        val name = params.getName(type)

        val priority = IntegerArgumentType.getInteger(ctx, "priority")
        params.priority = priority

        sendChat(ctx, Component.literal("Set priority for ").append(name).append(" to $priority"))
        return 1
    }

    class EntityParameters {
        var distance: Double = 0.0
        var priority: Int = 0

        fun getName(entity: Entity): MutableComponent {
            if (entity is Player) {
                return Formatting.purple(entity.plainTextName)
            }
            return getName(entity.type)
        }

        fun getName(type: EntityType<*>): MutableComponent {
            return Formatting.red(type.description.string)
        }
    }
}
