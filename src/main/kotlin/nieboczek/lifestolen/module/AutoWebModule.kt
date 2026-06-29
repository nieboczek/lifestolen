package nieboczek.lifestolen.module

import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Pose
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import nieboczek.lifestolen.module.util.PlacementUtil
import nieboczek.lifestolen.util.Renderer3d

object AutoWebModule : Module("AutoWeb", Category.COMBAT) {
    val range by double("Range", 3.0, 1.0..6.0, "blocks", 0.01)

    override fun tick() {
        val player = findNearestPlayer() ?: return
        val spots = findSpotsAroundPlayer(player)

        for (spot in spots) {
            if (mc.level!!.getBlockState(spot).canBeReplaced()) {
                PlacementUtil.switchHotbarToItem(Blocks.COBWEB.asItem())
                PlacementUtil.placeOnNeighbour(spot)
            }
            val spotAbove = spot.above()
            if (mc.level!!.getBlockState(spotAbove).canBeReplaced()) {
                PlacementUtil.switchHotbarToItem(Blocks.COBWEB.asItem())
                PlacementUtil.placeOnNeighbour(spotAbove)
            }
        }
    }

    override fun render3d() {
        val slayer = findNearestPlayer() ?: return
        for (spot in findSpotsAroundPlayer(slayer)) {
            val vec = Vec3(spot)
            val newX = player.oldPosition().x + (player.position().x - player.oldPosition().x) * Renderer3d.tickDelta
            val newY = player.oldPosition().y + (player.position().y - player.oldPosition().y) * Renderer3d.tickDelta
            val newZ = player.oldPosition().z + (player.position().z - player.oldPosition().z) * Renderer3d.tickDelta
            val new = vec.subtract(newX, newY, newZ)

            val aabb = AABB(new.x, new.y, new.z, new.x + 1, new.y + 1, new.z + 1)
            val pos = Renderer3d.computeSmoothRelativeToCameraPos(player.oldPosition(), player.position(), mc.entityRenderDispatcher.camera!!.position())
            Renderer3d.renderBoxOutline(aabb, 0xFF62F7B7.toInt(), pos, 4f)
        }
    }

    private fun toBlockPos(x: Double, y: Double, z: Double): BlockPos {
        return BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z))
    }

    private fun findSpotsAroundPlayer(player: Entity): List<BlockPos> {
        val dimensions = player.getDimensions(Pose.STANDING)
        val width = dimensions.width.toDouble() / 2.0

        val pos = player.position()

        val minX = pos.x - width
        val maxX = pos.x + width
        val minZ = pos.z - width
        val maxZ = pos.z + width

        val p1 = toBlockPos(minX, pos.y, minZ)
        val p2 = toBlockPos(minX, pos.y, maxZ)
        val p3 = toBlockPos(maxX, pos.y, minZ)
        val p4 = toBlockPos(maxX, pos.y, maxZ)
        return listOf(p1, p2, p3, p4)
    }

    private fun findNearestPlayer(): Entity? {
        var best: Entity? = null
        var bestDistSq = range * range
        val pos = player.position()
        val aabb = AABB(pos.subtract(range), pos.add(range))
        val entities = player.level().getEntities(mc.player, aabb) { it is LivingEntity && it.isAlive } // TODO: DONT FORGET TO SET IT BACK TO PLAYER

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