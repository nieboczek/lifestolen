package nieboczek.lifestolen.module

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.Items
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import nieboczek.lifestolen.module.util.PlacementUtil
import nieboczek.lifestolen.util.Renderer3d

object ScaffoldModule : Module("Scaffold", Category.MOVEMENT) {
    val onlyInAir by boolean("Only In Air", true)
    val showTarget by boolean("Show Target", true)

    private var targetPos: BlockPos? = null

    override fun tick() {
        if (!player.gameMode()!!.isSurvival) return
        if (player.mainHandItem.isEmpty && player.offhandItem.isEmpty) return

        targetPos = findPlaceTarget()
        val pos = targetPos ?: return

        val placed = PlacementUtil.placeOnNeighbour(pos)
        if (placed) {
            player.swing(InteractionHand.MAIN_HAND)
        }
    }

    override fun render3d() {
        if (!showTarget) return

        val feetPos = player.blockPosition().below()
        val cameraPos = mc.entityRenderDispatcher.camera!!.position()
        val predictedPos = predictNextPosition(feetPos, cameraPos)

        Renderer3d.renderBoxOutline(
            AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0),
            0xFF00DF82u.toInt(),
            predictedPos
        )
    }

    private fun predictNextPosition(basePos: BlockPos, cameraPos: Vec3): Vec3 {
        val yaw = Math.toRadians(player.yRot.toDouble())
        val predictedX = -kotlin.math.sin(yaw) * 2.0
        val predictedZ = kotlin.math.cos(yaw) * 2.0

        val predictedBlockPos = basePos.offset(
            predictedX.toInt(),
            0,
            predictedZ.toInt()
        )

        return Vec3(
            predictedBlockPos.x.toDouble() - cameraPos.x,
            predictedBlockPos.y.toDouble() - cameraPos.y,
            predictedBlockPos.z.toDouble() - cameraPos.z
        )
    }

    private fun findPlaceTarget(): BlockPos? {
        val feetPos = player.blockPosition().below()
        if (isValidPlacement(feetPos)) {
            return feetPos
        }
        return null
    }

    private fun isValidPlacement(pos: BlockPos): Boolean {
        val level = player.level()

        val state = level.getBlockState(pos)
        if (onlyInAir && !state.isAir && !state.canBeReplaced()) return false

        for (direction in Direction.entries) {
            val neighbor = pos.relative(direction)
            val neighborState = level.getBlockState(neighbor)
            if (neighborState.isSolidRender) {
                val handItem = player.mainHandItem
                if (!handItem.isEmpty && handItem.item !in listOf(Items.AIR)) {
                    return true
                }
            }
        }

        return false
    }
}