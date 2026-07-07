package nieboczek.lifestolen.module

import nieboczek.lifestolen.module.util.RotationUtil

object FlyModule : Module("Fly", Category.MOVEMENT) {
    val bypassVanillaCheck by boolean("Bypass Vanilla Check")
    val baseHorizontalSpeed by double("Base Horizontal Speed", 0.8, 0.01..10.0, step = 0.01)
    val baseVerticalSpeed by double("Base Vertical Speed", 0.6, 0.01..10.0, step = 0.01)
    val sprintHorizontalSpeed by double("Sprint Horizontal Speed", 1.6, 0.01..10.0, step = 0.01)
    val sprintVerticalSpeed by double("Sprint Vertical Speed", 1.2, 0.01..10.0, step = 0.01)

    private var targetY: Double = 0.0

    override fun enable() {
        targetY = player.y
    }

    override fun tick() {
        val input = player.input.keyPresses
        val horizontalSpeed = if (input.sprint) sprintHorizontalSpeed else baseHorizontalSpeed
        val verticalSpeed = if (input.sprint) sprintVerticalSpeed else baseVerticalSpeed

        val shouldBypassCheck = bypassVanillaCheck && player.tickCount % 40 == 0
        val deltaY = when {
            shouldBypassCheck && input.shift && !input.jump -> -verticalSpeed
            shouldBypassCheck -> -0.04
            input.jump && !input.shift -> verticalSpeed
            input.shift && !input.jump -> -verticalSpeed
            else -> targetY - player.y
        }

        player.deltaMovement = RotationUtil.getMovementDeltaFromInput(deltaY, horizontalSpeed, input)
        if ((input.shift || input.jump) && (!input.shift || !input.jump)) {
            targetY = player.y
        }
    }
}