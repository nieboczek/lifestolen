package nieboczek.lifestolen.module.util

import net.minecraft.client.Minecraft
import net.minecraft.util.Mth
import net.minecraft.world.entity.player.Input
import net.minecraft.world.phys.Vec3
import nieboczek.lifestolen.Lifestolen
import kotlin.math.abs

object RotationUtil {
    private val mc = Minecraft.getInstance()

    var lerpedRotation: Rotation? = null
        get() {
            if (Lifestolen.killSwitch) return null
            return field
        }

    private var targetRotation: Rotation? = null
    private var moduleTargeted = false

    fun target(x: Float, y: Float) {
        targetRotation = Rotation(x, y)
        moduleTargeted = true
    }

    fun tick() {
        val player = mc.player!!
        val target = targetRotation ?: Rotation(player.xRot, player.yRot)
        val current = lerpedRotation ?: Rotation(player.xRot, player.yRot)

        if (target == current) return

        val factor = 0.6f
        val newPitch = current.x + (target.x - current.x) * factor

        var deltaYaw = target.y - current.y
        deltaYaw = (deltaYaw + 180f).mod(360f) - 180f
        val newYaw = current.y + deltaYaw * factor

        val normalizedYaw = (newYaw.mod(360f) + 360f).mod(360f)
        val finalYaw = if (normalizedYaw > 180f) normalizedYaw - 360f else normalizedYaw

        lerpedRotation = Rotation(newPitch, finalYaw)
        if (roughlyEqual(target, lerpedRotation!!)) {
            targetRotation = null
            // only lerp if exiting from module-targeted state
            if (!moduleTargeted) lerpedRotation = null
            moduleTargeted = false
        }
    }

    fun getMovementDeltaFromInput(deltaY: Double, horizontalSpeed: Double, input: Input): Vec3 {
        val x = if (input.left && input.right) 0f else (if (input.left) -1f else (if (input.right) 1f else 0f))
        val y = if (input.backward && input.forward) 0f else (if (input.backward) -1f else (if (input.forward) 1f else 0f))

        return if (x == 0f && y == 0f) {
            Vec3(0.0, deltaY, 0.0)
        } else {
            val yaw = Math.toRadians(getMovementYawOfInput(input).toDouble())
            val x = -Mth.sin(yaw) * horizontalSpeed
            val z = Mth.cos(yaw) * horizontalSpeed
            Vec3(x, deltaY, z)
        }
    }

    fun getMovementYawOfInput(input: Input): Float {
        val player = mc.player!!
        var movementYaw = player.yRot

        val diagonalMultiplier = when {
            input.backward && !input.forward -> {
                movementYaw += 180f
                -0.5f
            }

            input.forward && !input.backward -> 0.5f
            else -> 1f
        }

        if (input.left && !input.right) movementYaw -= 90f * diagonalMultiplier
        if (input.right && !input.left) movementYaw += 90f * diagonalMultiplier

        return movementYaw
    }

    private fun roughlyEqual(a: Rotation, b: Rotation): Boolean {
        return abs(a.x - b.x) < 0.0001f && abs(a.y - b.y) < 0.0001f
    }

    data class Rotation(val x: Float, val y: Float)
}