package nieboczek.lifestolen.module

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB
import nieboczek.lifestolen.util.Renderer3d

object ESPModule : Module("ESP", Category.VISUALS) {
    val range by double("Range", 128.0, 8.0..512.0, "blocks", 0.1)
    val showOnlyPlayers by boolean("Show Only Players")
    val color by color("Color", 0x55FFFFFF)
    val boxScale by float("Box Scale", 1f, 0.1f..3f, step = 0.01f)
    val lineWidth by float("Line Width", 2f, 0.5f..8f, step = 0.1f)

    override fun render3d() {
        val pos = player.position()
        val aabb = AABB(pos.subtract(range), pos.add(range))
        val entities = player.level().getEntities(mc.player, aabb) {
            (!showOnlyPlayers || it is Player) && it is LivingEntity && it.isAlive
        }

        for (entity in entities) {
            val dimensions = entity.getDimensions(Pose.STANDING).scale(boxScale)
            val width = dimensions.width.toDouble() / 2.0
            val height = dimensions.height.toDouble()
            val aabb = AABB(-width, 0.0, -width, width, height, width)

            val cameraPos = mc.entityRenderDispatcher.camera!!.position()
            val pos = Renderer3d.computeSmoothRelativeToCameraPos(entity.oldPosition(), entity.position(), cameraPos)

            Renderer3d.renderBoxOutline(aabb, color, pos, lineWidth)
        }
    }
}