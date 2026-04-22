package nieboczek.lifestolen.module.util

import net.minecraft.client.Minecraft
import java.util.*
import kotlin.math.abs

object RotationUtil {
    private val mc = Minecraft.getInstance()

    var targetRotation: Rotation? = null
    var lerpedRotation: Rotation? = null

    fun tick() {
        val player = mc.player ?: return
        val target = targetRotation ?: Rotation(player.xRot, player.yRot)
        val current = lerpedRotation ?: Rotation(player.xRot, player.yRot)

        if (target == current) {
            return
        }

        val factor = 0.6f

        val newPitch = current.x + (target.x - current.x) * factor

        var deltaYaw = target.y - current.y
        deltaYaw = ((deltaYaw + 180f) % 360f) - 180f
        val newYaw = current.y + deltaYaw * factor

        val normalizedYaw = ((newYaw % 360f) + 360f) % 360f
        val finalYaw = if (normalizedYaw > 180f) normalizedYaw - 360f else normalizedYaw

        lerpedRotation = Rotation(newPitch, finalYaw)
        if (roughlyEqual(target, lerpedRotation!!)) {
            targetRotation = null
            lerpedRotation = null
        }
    }

    private fun roughlyEqual(a: Rotation, b: Rotation): Boolean {
        return abs(a.x - b.x) < 0.0001f && abs(a.y - b.y) < 0.0001f
    }

    data class Rotation(val x: Float, val y: Float)
}