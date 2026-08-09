package nieboczek.lifestolen.module.util

import net.minecraft.client.Minecraft
import net.minecraft.util.Mth
import net.minecraft.world.entity.player.Input
import net.minecraft.world.phys.Vec3
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.module.Module
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

object RotationUtil {
    const val PRIORITY_COMBAT = 100
    const val PRIORITY_PLACEMENT = 50
    const val PRIORITY_DEFAULT = 0

    private const val MIN_PITCH = -90f
    private const val MAX_PITCH = 90f

    private const val YAW_SPEED_PER_TICK = 0.35f
    private const val PITCH_SPEED_PER_TICK = 0.2f

    private class RotationTarget(
        val module: Module, val rotation: Rotation, val priority: Int, val correctYaw: Boolean
    )

    class Rotation(val x: Float, val y: Float)

    private val mc = Minecraft.getInstance()
    private val targets = HashMap<Module, RotationTarget>()

    var lerpedRotation: Rotation? = null
        get() {
            if (Lifestolen.killSwitch) return null
            return field
        }

    fun request(
        module: Module,
        x: Float,
        y: Float,
        priority: Int = PRIORITY_DEFAULT,
        correctYaw: Boolean = true,
    ) {
        targets[module] = RotationTarget(module, Rotation(x, y), priority, correctYaw)
    }

    fun cancel(module: Module) {
        targets.remove(module)
    }

    fun reset() {
        targets.clear()
        lerpedRotation = null
    }

    fun computeCorrectedYaw(): Float? {
        val active = activeTarget() ?: return null
        if (!active.correctYaw) return null
        return lerpedRotation?.y
    }

    fun spoofedRotation(): Rotation? {
        val active = activeTarget() ?: return null
        val player = mc.player ?: return null
        val fake = lerpedRotation ?: return null

        val yaw = if (active.correctYaw) fake.y else player.yRot
        return Rotation(fake.x, yaw)
    }

    fun transformInput(input: Input): Input {
        val player = mc.player ?: return input
        val active = activeTarget() ?: return input
        if (!active.correctYaw) return input
        val fakeYaw = lerpedRotation?.y ?: return input

        if (!input.forward && !input.backward && !input.left && !input.right) return input

        val offset = Mth.wrapDegrees(getMovementYawOfInput(input, 0f))
        val targetOffset = Mth.wrapDegrees(offset + player.yRot - fakeYaw)
        val snapped = 45.0 * (targetOffset / 45.0).roundToInt()

        return keysForOffset(snapped, input)
    }

    private fun keysForOffset(snapped: Double, input: Input): Input {
        return when (Math.floorMod((snapped / 45.0).roundToInt(), 8)) {
            0 -> Input(true, false, false, false, input.jump(), input.shift(), input.sprint())
            1 -> Input(true, false, false, true, input.jump(), input.shift(), input.sprint())
            2 -> Input(false, false, false, true, input.jump(), input.shift(), input.sprint())
            3 -> Input(false, true, false, true, input.jump(), input.shift(), input.sprint())
            4 -> Input(false, true, false, false, input.jump(), input.shift(), input.sprint())
            5 -> Input(false, true, true, false, input.jump(), input.shift(), input.sprint())
            6 -> Input(false, false, true, false, input.jump(), input.shift(), input.sprint())
            else -> Input(true, false, true, false, input.jump(), input.shift(), input.sprint())
        }
    }

    fun shouldKeepSprinting(input: Input): Boolean {
        if (!input.forward || input.backward) return false
        if (computeCorrectedYaw() == null) return true

        val movementRelToLerped = Mth.wrapDegrees(getMovementYawOfInput(input, 0f))
        return abs(movementRelToLerped) <= 45f
    }

    fun lerpRotation(partialTick: Float) {
        val player = mc.player ?: return
        val active = activeTarget()
        val target = active?.rotation ?: Rotation(player.xRot, player.yRot)
        val current = lerpedRotation ?: Rotation(player.xRot, player.yRot)

        if (target == current) {
            if (active == null) lerpedRotation = null
            return
        }

        val yawFactor = frameFactor(YAW_SPEED_PER_TICK, partialTick)
        val pitchFactor = frameFactor(PITCH_SPEED_PER_TICK, partialTick)

        val newPitch = current.x + (target.x - current.x) * pitchFactor
        val newYaw = current.y + Mth.wrapDegrees(target.y - current.y) * yawFactor

        val rotation = Rotation(newPitch.coerceIn(MIN_PITCH, MAX_PITCH), Mth.wrapDegrees(newYaw))

        if (roughlyEqual(rotation, target)) {
            lerpedRotation = target
            if (active == null) lerpedRotation = null
            return
        }

        lerpedRotation = rotation
    }

    private fun activeTarget(): RotationTarget? {
        targets.entries.removeIf { it.value.module.enabled.not() }
        return targets.values.maxByOrNull { it.priority }
    }

    private fun frameFactor(speedPerTick: Float, partialTick: Float) =
        1f - (1f - speedPerTick).toDouble().pow(partialTick.toDouble()).toFloat()

    private fun roughlyEqual(a: Rotation, b: Rotation) = abs(a.x - b.x) < 0.01f && abs(a.y - b.y) < 0.01f

    // ==== ACTUAL UTILITY FUNCTIONS ==========================================

    fun getMovementDeltaFromInput(
        deltaY: Double, horizontalSpeed: Double, input: Input, yRot: Float = mc.player!!.yRot
    ): Vec3 {
        val isMoving = input.forward || input.backward || input.left || input.right
        return if (!isMoving) {
            Vec3(0.0, deltaY, 0.0)
        } else {
            val yaw = Math.toRadians(getMovementYawOfInput(input, yRot).toDouble())
            val x = -Mth.sin(yaw) * horizontalSpeed
            val z = Mth.cos(yaw) * horizontalSpeed
            Vec3(x, deltaY, z)
        }
    }

    fun getMovementYawOfInput(input: Input, yRot: Float): Float {
        var movementYaw = yRot

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
}