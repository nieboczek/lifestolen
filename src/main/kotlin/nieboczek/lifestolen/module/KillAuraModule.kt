package nieboczek.lifestolen.module

import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.module.util.RotationUtil
import nieboczek.lifestolen.util.Renderer3d
import kotlin.math.atan2
import kotlin.math.sqrt

object KillAuraModule : Module("Kill Aura", Category.COMBAT) {
    private val range by double("Range", 3.0, 1.0..4.0, "blocks", 0.01)
    private val attackOnlyPlayers by boolean("Attack Only Players")
    private val lookAtTarget by boolean("Look At Target")
    private val renderRangeOutline by boolean("Render Range Outline")

    override fun tick() {
        val target = findNearestEntity()
        if (target == null || !lookAtTarget) {
            RotationUtil.cancel(this)
        } else {
            val targetVec = target.eyePosition
            RotationUtil.request(this, getXRot(targetVec), getYRot(targetVec), RotationUtil.PRIORITY_COMBAT)
        }

        if (target == null || player.isBlocking) return

        if (player.getAttackStrengthScale(0.5f) >= 0.95) {
            mc.gameMode!!.attack(player, target)
            player.swing(InteractionHand.MAIN_HAND)
        }
    }

    override fun render3d() {
        if (!renderRangeOutline) return

        val cameraRelativePos = Renderer3d.computeSmoothRelativeToCameraPos(
            player.oldPosition(),
            player.position(),
            mc.entityRenderDispatcher.camera!!.position()
        ).add(0.0, 1.0, 0.0)

        Renderer3d.renderCircleOutline(64, -1, 9f, range.toFloat(), cameraRelativePos)
    }

    private fun getXRot(target: Vec3): Float {
        val eyePos = player.eyePosition
        val diff = target.subtract(eyePos)
        return Math.toDegrees(atan2(-diff.y, sqrt(diff.x * diff.x + diff.z * diff.z))).toFloat()
    }

    private fun getYRot(target: Vec3): Float {
        val eyePos = player.eyePosition
        val diff = target.subtract(eyePos)
        return (-Math.toDegrees(atan2(diff.x, diff.z))).toFloat()
    }

    private fun findNearestEntity(): Entity? {
        var best: Entity? = null
        var bestDistSq = range * range
        val pos = player.position()
        val aabb = AABB(pos.subtract(5.0), pos.add(5.0))
        val entities = player.level()
            .getEntities(mc.player, aabb)
            { (!attackOnlyPlayers || it is Player) && it is LivingEntity && it.isAlive && !Lifestolen.isFriend(it) }

        for (entity in entities) {
            val distSq = player.distanceToSqr(entity)
            if (distSq < bestDistSq) {
                bestDistSq = distSq
                best = entity
            }
        }

        return best
    }
}
