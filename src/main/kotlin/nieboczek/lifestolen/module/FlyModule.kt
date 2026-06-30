package nieboczek.lifestolen.module

import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3

object FlyModule : Module("Fly", Category.MOVEMENT) {
    val bypassVanillaCheck by boolean("Bypass Vanilla Check")
    val baseHorizontalSpeed by double("Base Horizontal Speed", 0.8, 0.01..10.0, step = 0.01)
    val baseVerticalSpeed by double("Base Vertical Speed", 0.6, 0.01..10.0, step = 0.01)
    val sprintHorizontalSpeed by double("Sprint Horizontal Speed", 1.6, 0.01..10.0, step = 0.01)
    val sprintVerticalSpeed by double("Sprint Vertical Speed", 1.2, 0.01..10.0, step = 0.01)

    override fun tick() {
        val sprinting = mc.options.keySprint.isDown
        val horizontalSpeed = if (sprinting) sprintHorizontalSpeed else baseHorizontalSpeed
        val verticalSpeed = if (sprinting) sprintVerticalSpeed else baseVerticalSpeed

        val shouldBypassCheck = bypassVanillaCheck && player.tickCount % 40 == 0
        val shifting = mc.options.keyShift.isDown
        val jumping = mc.options.keyJump.isDown
        val deltaY = when {
            shouldBypassCheck && shifting -> -verticalSpeed
            shouldBypassCheck -> -0.04
            jumping && !shifting -> verticalSpeed
            shifting && !jumping -> -verticalSpeed
            else -> 0.0
        }

        val inputMoveVector = player.input.moveVector
        val newDeltaMovement = if (inputMoveVector.x == 0f && inputMoveVector.y == 0f) {
            Vec3(0.0, deltaY, 0.0)
        } else {
            val angle = Math.toRadians(getMovementYawOfInput().toDouble())
            val newX = -Mth.sin(angle) * horizontalSpeed
            val newZ = Mth.cos(angle) * horizontalSpeed
            Vec3(newX, deltaY, newZ)
        }

        player.deltaMovement = newDeltaMovement
    }

    fun getMovementYawOfInput(): Float {
        var movementYaw = player.yRot
        val input = player.input.keyPresses

        val diagonalMultiplier = when {
            input.backward && !input.forward -> {
                movementYaw += 180f
                -0.5f
            }

            input.forward && !input.backward -> 0.5f
            else -> 1f
        }

        if (input.left && !input.right) {
            movementYaw -= 90f * diagonalMultiplier
        }
        if (input.right && !input.left) {
            movementYaw += 90f * diagonalMultiplier
        }

        return movementYaw
    }
}