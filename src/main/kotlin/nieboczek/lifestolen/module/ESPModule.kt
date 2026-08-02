package nieboczek.lifestolen.module

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.util.Renderer3d

object ESPModule : Module("ESP", Category.VISUALS) {
    private val range by double("Range", 128.0, 8.0..512.0, "blocks", 0.1)
    private val showOnlyPlayers by boolean("Show Only Players")
    private val color by color("Color", 0x55FFFFFF)
    private val boxScale by float("Box Scale", 1f, 0.1f..3f, step = 0.01f)
    private val lineWidth by float("Line Width", 2f, 0.5f..8f, step = 0.1f)
    private val fillOpacity by float("Fill Opacity", 0f, 0f..1f, step = 0.01f)

    override fun render3d() {
        val pos = player.position()
        val aabb = AABB(pos.subtract(range), pos.add(range))
        val entities = player.level().getEntities(mc.player, aabb) {
            (!showOnlyPlayers || it is Player) && it is LivingEntity && it.isAlive && !Lifestolen.isFriend(it)
        }

        for (entity in entities) {
            val dimensions = entity.getDimensions(Pose.STANDING).scale(boxScale)
            val width = dimensions.width.toDouble() / 2.0
            val height = dimensions.height.toDouble()
            val aabb = AABB(-width, 0.0, -width, width, height, width)

            val cameraPos = mc.entityRenderDispatcher.camera!!.position()
            val pos = Renderer3d.computeSmoothRelativeToCameraPos(entity.oldPosition(), entity.position(), cameraPos)

            if (fillOpacity > 0f) {
                val baseAlpha = (color ushr 24) and 0xFF
                val fillAlpha = (baseAlpha * fillOpacity).toInt()
                val fillColor = (fillAlpha shl 24) or (color and 0x00FFFFFF)
                Renderer3d.renderBoxFill(aabb, fillColor, pos)
            }

            Renderer3d.renderBoxOutline(aabb, color, pos, lineWidth)
        }
    }
}