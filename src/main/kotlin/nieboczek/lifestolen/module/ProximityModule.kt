package nieboczek.lifestolen.module

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB
import nieboczek.lifestolen.serializer.base.ClassSerializer
import nieboczek.lifestolen.serializer.base.DoubleSerializer
import nieboczek.lifestolen.serializer.base.IntSerializer
import nieboczek.lifestolen.serializer.base.StringSerializer
import nieboczek.lifestolen.serializer.minecraft.ResourceSerializer
import nieboczek.lifestolen.util.Formatting
import java.util.function.Predicate

object ProximityModule : Module("Proximity", Category.COMBAT) {
    val playerWhitelist by list("Player Whitelist", mutableListOf(), StringSerializer())
    val entities by map(
        "Entities",
        mutableMapOf<EntityType<*>, EntityParameters>(),
        ResourceSerializer(BuiltInRegistries.ENTITY_TYPE),
        ClassSerializer { EntityParameters() }
            .field("priority", IntSerializer(), { p -> p.priority }, { p, v -> p.priority = v })
            .field("distance", DoubleSerializer(), { p -> p.distance }, { p, v -> p.distance = v })
    )

    private val BIG_AABB = AABB(-65535.0, -65535.0, -65535.0, 65535.0, 65535.0, 65535.0)

    override fun tick() {
        // TODO: multiple mobs at once
        var entityCandidate: Entity? = null
        var paramsCandidate: EntityParameters? = null
        var distanceCandidate = 0.0
        val entities = player.level().getEntities(mc.player, BIG_AABB, Predicate { _ -> true })

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
