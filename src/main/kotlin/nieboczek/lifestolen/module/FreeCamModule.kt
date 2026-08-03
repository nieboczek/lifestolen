package nieboczek.lifestolen.module

import net.minecraft.client.CameraType
import net.minecraft.world.phys.Vec3
import nieboczek.lifestolen.mixininterfaces.IKeyboardInput
import nieboczek.lifestolen.module.util.RotationUtil

object FreeCamModule : Module("Free Cam", Category.VISUALS) {
    private val baseHorizontalSpeed by double("Base Horizontal Speed", 0.8, 0.01..10.0, step = 0.01)
    private val baseVerticalSpeed by double("Base Vertical Speed", 0.6, 0.01..10.0, step = 0.01)
    private val sprintHorizontalSpeed by double("Sprint Horizontal Speed", 1.6, 0.01..10.0, step = 0.01)
    private val sprintVerticalSpeed by double("Sprint Vertical Speed", 1.2, 0.01..10.0, step = 0.01)
    private val disableOnDamage by boolean("Disable On Damage")

    private var prevCamType: CameraType = CameraType.FIRST_PERSON
    private var oldCamPos = Vec3.ZERO
    private var camPos = Vec3.ZERO

    var camYRot = 0f
        private set
    var camXRot = 0f
        private set

    fun computeLerpedPos(partialTicks: Float): Vec3 {
        val partial = partialTicks.toDouble()
        return oldCamPos.add(camPos.subtract(oldCamPos).multiply(partial, partial, partial))
    }

    fun turnCamera(deltaYaw: Double, deltaPitch: Double) {
        camYRot += (deltaYaw * 0.15).toFloat()
        camXRot = (camXRot + (deltaPitch * 0.15).toFloat()).coerceIn(-90f, 90f)
    }

    override fun enable() {
        prevCamType = mc.options.cameraType
        mc.options.cameraType = CameraType.FIRST_PERSON

        camPos = player.eyePosition
        oldCamPos = camPos

        camYRot = player.yRot
        camXRot = player.xRot
    }

    override fun disable() {
        mc.options.cameraType = prevCamType
    }

    override fun tick() {
        if (disableOnDamage && player.hurtTime > 0) {
            toggle()
            return
        }

        val input = IKeyboardInput.getUnmodified(player)
        val horizontalSpeed = if (input.sprint) sprintHorizontalSpeed else baseHorizontalSpeed
        val verticalSpeed = if (input.sprint) sprintVerticalSpeed else baseVerticalSpeed
        val deltaY = when {
            input.jump && !input.shift -> verticalSpeed
            input.shift && !input.jump -> -verticalSpeed
            else -> 0.0
        }

        oldCamPos = camPos
        camPos = camPos.add(RotationUtil.getMovementDeltaFromInput(deltaY, horizontalSpeed, input, camYRot))
    }
}
