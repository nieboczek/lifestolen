package nieboczek.lifestolen.module.util

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import kotlin.math.atan2
import kotlin.math.sqrt

object PlacementUtil {
    private val mc = Minecraft.getInstance()

    fun placeOnNeighbour(target: BlockPos): Boolean {
        val player = mc.player ?: return false
        val level = player.level()
        val itemStack = player.mainHandItem

        if (itemStack.isEmpty) return false

        for (direction in Direction.entries) {
            val neighbourPos = target.relative(direction)
            val neighbourState = level.getBlockState(neighbourPos)

            if (!neighbourState.isSolidRender) continue

            val hitVec = Vec3.atCenterOf(neighbourPos)
            val hitResult = BlockHitResult(
                hitVec,
                direction.opposite,
                neighbourPos,
                false
            )

            val context = BlockPlaceContext.at(
                BlockPlaceContext(player, InteractionHand.MAIN_HAND, itemStack, hitResult),
                target,
                direction.opposite
            )

            if (!context.canPlace()) continue

            val targetVec = Vec3.atCenterOf(neighbourPos)
            RotationUtil.targetRotation = RotationUtil.Rotation(getXRot(targetVec), getYRot(targetVec))

            return mc.gameMode!!.useItemOn(mc.player!!, InteractionHand.MAIN_HAND, hitResult) != InteractionResult.FAIL
        }

        return false
    }

    private fun getXRot(target: Vec3): Float {
        val player = mc.player ?: return 0f
        val eyePos = player.eyePosition
        val diff = target.subtract(eyePos)
        return Math.toDegrees(atan2(-diff.y, sqrt(diff.x * diff.x + diff.z * diff.z))).toFloat()
    }

    private fun getYRot(target: Vec3): Float {
        val player = mc.player ?: return 0f
        val eyePos = player.eyePosition
        val diff = target.subtract(eyePos)
        return Math.toDegrees(atan2(diff.x, diff.z)).toFloat()
    }
}