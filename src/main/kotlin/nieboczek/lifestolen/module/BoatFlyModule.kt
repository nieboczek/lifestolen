package nieboczek.lifestolen.module

import net.minecraft.world.entity.vehicle.boat.AbstractBoat
import nieboczek.lifestolen.module.util.RotationUtil

object BoatFlyModule : Module("BoatFly", Category.MOVEMENT) {
    private val baseHorizontalSpeed by double("Base Horizontal Speed", 0.8, 0.01..10.0, step = 0.01)
    private val baseVerticalSpeed by double("Base Vertical Speed", 0.6, 0.01..10.0, step = 0.01)
    private val sprintHorizontalSpeed by double("Sprint Horizontal Speed", 1.6, 0.01..10.0, step = 0.01)
    private val sprintVerticalSpeed by double("Sprint Vertical Speed", 1.2, 0.01..10.0, step = 0.01)
    private val glide by double("Glide", -0.15, -0.3..0.3, step = 0.01)

    override fun tick() {
        val boat = player.vehicle as? AbstractBoat ?: return
        val input = player.input.keyPresses

        val horizontalSpeed = if (input.sprint) sprintHorizontalSpeed else baseHorizontalSpeed
        val verticalSpeed = if (input.sprint) sprintVerticalSpeed else baseVerticalSpeed

        val deltaY = when {
            input.jump && !input.shift -> verticalSpeed
            input.shift && !input.jump -> -verticalSpeed
            // keep the boat afloat instead of letting it sink into the water when idle
            boat.isInWater -> 0.0
            else -> glide
        }

        boat.deltaMovement = RotationUtil.getMovementDeltaFromInput(deltaY, horizontalSpeed, input, boat.yRot)
    }
}
