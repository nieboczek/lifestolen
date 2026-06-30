package nieboczek.lifestolen.module

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB
import nieboczek.lifestolen.util.Renderer3d
import org.joml.Vector3f

object TracersModule : Module("Tracers", Category.VISUALS) {
    val range by double("Range", 128.0, 8.0..512.0, "blocks", 0.1)
    val showOnlyPlayers by boolean("Show Only Players")
    val color by color("Color", 0xFF00A0FF.toInt())
    val lineWidth by float("Line Width", 2f, 0.5f..8f, step = 0.1f)

    override fun render3d() {
        val maxDistSq = range * range
        val pos = player.position()
        val aabb = AABB(pos.subtract(range + 2.0), pos.add(range + 2.0))
        val entities = player.level().getEntities(mc.player, aabb) {
            (!showOnlyPlayers || it is Player) && it is LivingEntity && it.isAlive
        }

        val cam = Renderer3d.camera ?: return
        val camPos = cam.position()
        val partial = Renderer3d.tickDelta

        val eyeVec = computeEyeVector()

        for (entity in entities) {
            if (player.distanceToSqr(entity) > maxDistSq) continue

            val entityPos = entity.position()
            val entityOldPos = entity.oldPosition()
            val ex = (entityOldPos.x + (entityPos.x - entityOldPos.x) * partial - camPos.x).toFloat()
            val ey = (entityOldPos.y + (entityPos.y - entityOldPos.y) * partial - camPos.y).toFloat()
            val ez = (entityOldPos.z + (entityPos.z - entityOldPos.z) * partial - camPos.z).toFloat()

            val posVec = Vector3f(ex, ey, ez)
            val topVec = Vector3f(ex, ey + entity.bbHeight, ez)

            if (lineWidth <= 1.01f) {
                Renderer3d.drawLine(color, eyeVec, posVec)
                Renderer3d.drawLine(color, posVec, topVec)
            } else {
                Renderer3d.drawLineWithWidth(color, lineWidth, eyeVec, posVec)
                Renderer3d.drawLineWithWidth(color, lineWidth, posVec, topVec)
            }
        }
    }

    private fun computeEyeVector(): Vector3f {
        val look = player.lookAngle
        return Vector3f(look.x.toFloat(), look.y.toFloat(), look.z.toFloat())
    }
}
