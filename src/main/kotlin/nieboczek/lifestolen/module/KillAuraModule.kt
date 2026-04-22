package nieboczek.lifestolen.module

import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB
import nieboczek.lifestolen.util.Renderer3d
import java.util.*
import java.util.function.Predicate

object KillAuraModule : Module("KillAura", Category.COMBAT) {
    val range by float("Range", 3f, 1f..4f, "blocks")
    val attackOnlyPlayers by boolean("Attack Only Players", true)

    private val BIG_AABB = AABB(-65535.0, -65535.0, -65535.0, 65535.0, 65535.0, 65535.0)

    override fun tick() {
        val target: Entity? = this.nearestEnemy
        if (target == null || !player.gameMode()!!.isSurvival) return
        if (player.isBlocking) return  // don't attack if shield blocking

        if (player.getAttackStrengthScale(0.5f) >= 0.95) {
            mc.gameMode!!.attack(player, target)
            player.swing(InteractionHand.MAIN_HAND)
        }
    }

    override fun render3d() {
        val cameraRelativePos = Renderer3d.computeSmoothRelativeToCameraPos(
            player.oldPosition(),
            player.position(),
            mc.entityRenderDispatcher.camera!!.position()
        ).add(0.0, 1.0, 0.0)

        Renderer3d.renderCircleOutline(64, -1, range, cameraRelativePos)
    }

    private val nearestEnemy: Entity?
        get() {
            var best: Entity? = null
            var bestDistSq = range * range
            val entities = player.level().getEntities(mc.player, BIG_AABB, Predicate { _: Entity -> true })

            for (entity in entities) {
                val attackPlayer = entity is Player
                val attackLivingEntity = !attackOnlyPlayers && entity is LivingEntity

                if ((attackPlayer || attackLivingEntity) && entity !== mc.player && entity.isAlive) {
                    val distSq: Double = player.distanceToSqr(entity)
                    if (distSq < bestDistSq) {
                        bestDistSq = distSq.toFloat()
                        best = entity
                    }
                }
            }

            return best
        }
}
