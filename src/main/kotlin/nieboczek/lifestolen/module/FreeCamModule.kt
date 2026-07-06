package nieboczek.lifestolen.module

import net.minecraft.client.CameraType
import net.minecraft.world.phys.Vec3
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.module.util.RotationUtil

object FreeCamModule : Module("FreeCam", Category.VISUALS) {
    val baseHorizontalSpeed by double("Base Horizontal Speed", 0.8, 0.01..10.0, step = 0.01)
    val baseVerticalSpeed by double("Base Vertical Speed", 0.6, 0.01..10.0, step = 0.01)
    val sprintHorizontalSpeed by double("Sprint Horizontal Speed", 1.6, 0.01..10.0, step = 0.01)
    val sprintVerticalSpeed by double("Sprint Vertical Speed", 1.2, 0.01..10.0, step = 0.01)
    val disableOnDamage by boolean("Disable On Damage")

    private var prevCamType: CameraType = CameraType.FIRST_PERSON
    private var oldCamPos = Vec3.ZERO
    private var camPos = Vec3.ZERO

    fun isEnabled(): Boolean = enabled && !Lifestolen.killSwitch

    fun computeLerpedPos(partialTicks: Float): Vec3 {
        val partial = partialTicks.toDouble()
        return oldCamPos.add(camPos.subtract(oldCamPos).multiply(partial, partial, partial))
    }

    override fun enable() {
        prevCamType = mc.options.cameraType
        mc.options.cameraType = CameraType.FIRST_PERSON

        camPos = player.eyePosition
        oldCamPos = camPos

        player.noPhysics = true
        player.isNoGravity = true
    }

    override fun disable() {
        mc.options.cameraType = prevCamType
        player.noPhysics = false
        player.isNoGravity = false
    }

    override fun tick() {
        if (disableOnDamage && player.hurtTime > 0) {
            toggle()
            return
        }

        player.deltaMovement = Vec3.ZERO

        val sprinting = mc.options.keySprint.isDown
        val horizontalSpeed = if (sprinting) sprintHorizontalSpeed else baseHorizontalSpeed
        val verticalSpeed = if (sprinting) sprintVerticalSpeed else baseVerticalSpeed

        val shifting = mc.options.keyShift.isDown
        val jumping = mc.options.keyJump.isDown
        val deltaY = when {
            jumping && !shifting -> verticalSpeed
            shifting && !jumping -> -verticalSpeed
            else -> 0.0
        }

        oldCamPos = camPos
        camPos = camPos.add(RotationUtil.getMovementDeltaFromPlayerInput(deltaY, horizontalSpeed))
    }
}
