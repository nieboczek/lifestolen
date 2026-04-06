package nieboczek.lifestolen.module;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import nieboczek.lifestolen.Commands;
import nieboczek.lifestolen.Lifestolen;
import nieboczek.lifestolen.serializer.base.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg;
import static com.mojang.brigadier.arguments.DoubleArgumentType.getDouble;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static nieboczek.lifestolen.Formatting.*;

public final class ProximityModule extends Module<ProximityModule.Config> {
    private static final AABB BIG_AABB = new AABB(-65_535.0, -65_535.0, -65_535.0, 65_535.0, 65_535.0, 65_535.0);

    @Override
    public void tick() {
        // TODO: multiple mobs at once
        Entity entityCandidate = null;
        EntityParameters paramsCandidate = null;
        double distanceCandidate = 0.0;

        List<? extends Entity> entities = mc.player.level().getEntities(mc.player, BIG_AABB, $ -> true);

        for (Entity entity : entities) {
            EntityType<?> type = entity.getType();
            EntityParameters params = cfg.entities.get(type);

            if (params == null || !params.canDisplay(type))
                continue;

            if (cfg.playerWhitelist.contains(entity.getName().getString()))
                continue;

            int priority = params.priority;
            double distance = mc.player.distanceTo(entity);

            if (distance < params.distance && (entityCandidate == null || priority > paramsCandidate.priority || (priority == paramsCandidate.priority && distance < distanceCandidate))) {
                paramsCandidate = params;
                entityCandidate = entity;
                distanceCandidate = distance;
            }
        }

        if (paramsCandidate == null) return;
        sendStatus(paramsCandidate.getName(entityCandidate).append(green(" ⇔ ")).append(red(String.format("%.2fm", distanceCandidate))), mc);
    }

    @Override
    public String getId() {
        return "proximity";
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext context) {
        LiteralArgumentBuilder<FabricClientCommandSource> distanceCommand = literal("distance");
        LiteralArgumentBuilder<FabricClientCommandSource> priorityCommand = literal("priority");

        distanceCommand.then(argument("entity", ResourceArgument.resource(context, Registries.ENTITY_TYPE))
                .executes(this::printDistanceCommand)
                .then(argument("distance", doubleArg(0, 1000))
                        .executes(this::setDistanceCommand)
                )
        );

        priorityCommand.then(argument("entity", ResourceArgument.resource(context, Registries.ENTITY_TYPE))
                .executes(this::printPriorityCommand)
                .then(argument("priority", integer())
                        .executes(this::setPriorityCommand)
                )
        );

        dispatcher.register(literal("proximity")
                .then(Commands.stringListManipulator(cfg.playerWhitelist, "player_whitelist", "player whitelist"))
                .then(distanceCommand)
                .then(priorityCommand)
        );
    }

    @Override
    public Serializer<Config> getSerializer() {
        return ObjectSerializer.of(Config::new)
                .field("playerWhitelist", ListSerializer.of(StringSerializer.of()), c -> c.playerWhitelist, (c, v) -> c.playerWhitelist = v)
                .field("entities", MapSerializer.of(
                        ResourceSerializer.of(BuiltInRegistries.ENTITY_TYPE),
                        ObjectSerializer.of(ProximityModule.EntityParameters::new)
                                .field("distance", DoubleSerializer.of(), p -> p.distance, (p, v) -> p.distance = v)
                                .field("priority", IntegerSerializer.of(), p -> p.priority, (p, v) -> p.priority = v)
                ), c -> c.entities, (c, v) -> c.entities = v);
    }

//.withDefault(() -> new HashMap<>() {{
//    put(EntityType.PLAYER, new EntityParameters(1000, 1000000));
//    put(EntityType.CREEPER, new EntityParameters(12, 50));
//    put(EntityType.PIGLIN, new EntityParameters(15, 80));
//    put(EntityType.PIGLIN_BRUTE, new EntityParameters(20, 120));
//    put(EntityType.HOGLIN, new EntityParameters(15, 100));
//    put(EntityType.DRAGON_FIREBALL, new EntityParameters(1000, 200));
//    put(EntityType.FIREBALL, new EntityParameters(1000, 160));
//    put(EntityType.GHAST, new EntityParameters(50, 120));
//    put(EntityType.RAVAGER, new EntityParameters(50, 165));
//    put(EntityType.EVOKER, new EntityParameters(40, 170));
//}}))

    private int printDistanceCommand(CommandContext<FabricClientCommandSource> ctx) {
        EntityType<?> type = Commands.getResource("entity", ctx);
        EntityParameters params = cfg.entities.get(type);
        if (params == null) {
            sendChat(ctx, red("No distance option found for ").append(type.getDescription()));
            return 0;
        }

        sendChat(ctx, Component.literal("Distance for ").append(params.getName(type)).append(" is " + params.distance));
        return 1;
    }

    private int setDistanceCommand(CommandContext<FabricClientCommandSource> ctx) {
        EntityType<?> type = Commands.getResource("entity", ctx);
        EntityParameters params = cfg.entities.computeIfAbsent(type, $ -> new EntityParameters());
        MutableComponent name = params.getName(type);

        double distance = getDouble(ctx, "distance");
        params.distance = distance;

        sendChat(ctx, Component.literal("Set distance for ").append(name).append(" to " + distance));
        return 1;
    }

    private int printPriorityCommand(CommandContext<FabricClientCommandSource> ctx) {
        EntityType<?> type = Commands.getResource("entity", ctx);
        EntityParameters params = cfg.entities.get(type);
        if (params == null) {
            sendChat(ctx, red("No priority option found for ").append(type.getDescription()));
            return 0;
        }

        sendChat(ctx, Component.literal("Priority for ").append(params.getName(type)).append(" is " + params.priority));
        return 1;
    }

    private int setPriorityCommand(CommandContext<FabricClientCommandSource> ctx) {
        EntityType<?> type = Commands.getResource("entity", ctx);
        EntityParameters params = cfg.entities.computeIfAbsent(type, $ -> new EntityParameters());
        MutableComponent name = params.getName(type);

        int priority = getInteger(ctx, "priority");
        params.priority = priority;

        sendChat(ctx, Component.literal("Set priority for ").append(name).append(" to " + priority));
        return 1;
    }

    public static class EntityParameters {
        double distance = 0;
        int priority = 0;

        private EntityParameters() {
        }

        private EntityParameters(double distance, int priority) {
            this.distance = distance;
            this.priority = priority;
        }

        private boolean canDisplay(EntityType<?> type) {
            return true;
        }

        private MutableComponent getName(Entity entity) {
            if (entity instanceof Player player) {
                return purple(player.getPlainTextName());
            }
            return getName(entity.getType());
        }

        private MutableComponent getName(EntityType<?> type) {
            return red(type.getDescription().getString());
        }
    }

//        private boolean canDisplay(Player player) {
//            if (this == PIGLIN) {
//                boolean head = player.getItemBySlot(EquipmentSlot.HEAD).is(Items.GOLDEN_HELMET);
//                boolean chest = player.getItemBySlot(EquipmentSlot.CHEST).is(Items.GOLDEN_CHESTPLATE);
//                boolean legs = player.getItemBySlot(EquipmentSlot.LEGS).is(Items.GOLDEN_LEGGINGS);
//                boolean feet = player.getItemBySlot(EquipmentSlot.FEET).is(Items.GOLDEN_BOOTS);
//                return !(head || chest || legs || feet);
//            }
//            return true;
//        }

    public static final class Config {
        Map<EntityType<?>, EntityParameters> entities = new HashMap<>();
        List<String> playerWhitelist = new ArrayList<>();
    }
}
