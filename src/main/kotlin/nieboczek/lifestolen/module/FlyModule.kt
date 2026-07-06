package nieboczek.lifestolen.module

import nieboczek.lifestolen.module.util.RotationUtil

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

        player.deltaMovement = RotationUtil.getMovementDeltaFromPlayerInput(deltaY, horizontalSpeed)
    }
}