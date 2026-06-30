package nieboczek.lifestolen.module

import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import nieboczek.lifestolen.module.util.PlacementUtil
import nieboczek.lifestolen.util.Renderer3d

object AutoTrapModule : Module("AutoTrap", Category.COMBAT) {
    val range by double("Range", 4.0, 1.0..6.0, "blocks", 0.01)

    override fun tick() {
        // TODO: destroy small annoyances like flowers or any instant break block
        val player = findNearestPlayer(range) ?: return
        val spots = findSpotsAroundPlayer(player)
        placeCobwebsOnSpots(spots)

        val wallSpots = findWallSpots(spots, spots[0].y)
        for (wallSpot in wallSpots) {
            if (mc.level!!.getBlockState(wallSpot).canBeReplaced()) {
                if (!PlacementUtil.switchHotbarToItem(Blocks.OBSIDIAN.asItem())) return
                PlacementUtil.placeOnNeighbour(wallSpot)
            }
            val wallSpotAbove = wallSpot.above()
            if (mc.level!!.getBlockState(wallSpotAbove).canBeReplaced()) {
                if (!PlacementUtil.switchHotbarToItem(Blocks.OBSIDIAN.asItem())) return
                PlacementUtil.placeOnNeighbour(wallSpotAbove)
            }
        }

        for (spot in spots) {
            val roofSpot = spot.above(2)
            if (mc.level!!.getBlockState(roofSpot).canBeReplaced()) {
                if (!PlacementUtil.switchHotbarToItem(Blocks.OBSIDIAN.asItem())) return
                PlacementUtil.placeOnNeighbour(roofSpot)
            }
        }
    }

    override fun render3d() = renderGeneric(range)

    fun renderGeneric(range: Double) {
        val enemy = findNearestPlayer(range) ?: return
        for (spot in findSpotsAroundPlayer(enemy)) {
            val vec = Vec3(spot)
            val newX = player.oldPosition().x + (player.position().x - player.oldPosition().x) * Renderer3d.tickDelta
            val newY = player.oldPosition().y + (player.position().y - player.oldPosition().y) * Renderer3d.tickDelta
            val newZ = player.oldPosition().z + (player.position().z - player.oldPosition().z) * Renderer3d.tickDelta
            val new = vec.subtract(newX, newY, newZ)

            val aabb = AABB(new.x, new.y, new.z, new.x + 1, new.y + 1, new.z + 1)
            val pos = Renderer3d.computeSmoothRelativeToCameraPos(
                player.oldPosition(),
                player.position(),
                mc.entityRenderDispatcher.camera!!.position()
            )
            Renderer3d.renderBoxOutline(aabb, 0xFF62F7B7.toInt(), pos, 4f)
        }
    }

    private fun findWallSpots(spots: List<BlockPos>, groundY: Int): List<BlockPos> {
        val minX = spots.minOf { it.x }
        val maxX = spots.maxOf { it.x }
        val minZ = spots.minOf { it.z }
        val maxZ = spots.maxOf { it.z }

        return listOf(
            BlockPos(minX - 1, groundY, minZ),
            BlockPos(minX - 1, groundY, maxZ),
            BlockPos(maxX + 1, groundY, minZ),
            BlockPos(maxX + 1, groundY, maxZ),
            BlockPos(minX, groundY, minZ - 1),
            BlockPos(maxX, groundY, minZ - 1),
            BlockPos(minX, groundY, maxZ + 1),
            BlockPos(maxX, groundY, maxZ + 1),
        ).distinct()
    }

    private fun toBlockPos(x: Double, y: Double, z: Double): BlockPos {
        return BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z))
    }

    fun findSpotsAroundPlayer(player: Entity): List<BlockPos> {
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
        return listOf(p1, p2, p3, p4).distinct()
    }

    fun findNearestPlayer(range: Double): Entity? {
        var best: Entity? = null
        var bestDistSq = range * range
        val pos = player.position()
        val aabb = AABB(pos.subtract(range), pos.add(range))
        val entities = player.level().getEntities(player, aabb) { it is Player && it.isAlive }

        for (entity in entities) {
            val distSq = player.distanceToSqr(entity)
            if (distSq < bestDistSq) {
                bestDistSq = distSq
                best = entity
            }
        }

        return best
    }

    fun placeCobwebsOnSpots(spots: List<BlockPos>) {
        for (spot in spots) {
            if (mc.level!!.getBlockState(spot).canBeReplaced()) {
                if (!PlacementUtil.switchHotbarToItem(Blocks.COBWEB.asItem())) return
                PlacementUtil.placeOnNeighbour(spot)
            }
            val spotAbove = spot.above()
            if (mc.level!!.getBlockState(spotAbove).canBeReplaced()) {
                if (!PlacementUtil.switchHotbarToItem(Blocks.COBWEB.asItem())) return
                PlacementUtil.placeOnNeighbour(spotAbove)
            }
        }
    }
}
