package nieboczek.lifestolen

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.world.phys.Vec3
import kotlin.math.cos
import kotlin.math.sin

object Renderer3d {
    @JvmField
    var bufferSource: MultiBufferSource.BufferSource? = null
    @JvmField
    var poseStack: PoseStack? = null
    @JvmField
    var tickDelta: Float = 0f

    @JvmStatic
    fun computePartialTickPos(oldPos: Vec3, pos: Vec3, cameraPos: Vec3): Vec3 {
        val newX = oldPos.x + (pos.x - oldPos.x) * tickDelta - cameraPos.x
        val newY = oldPos.y + (pos.y - oldPos.y) * tickDelta - cameraPos.y
        val newZ = oldPos.z + (pos.z - oldPos.z) * tickDelta - cameraPos.z
        return Vec3(newX, newY, newZ)
    }

    @JvmStatic
    fun renderCircleOutline(segments: Int, color: Int, radius: Float, pos: Vec3) {
        val consumer = bufferSource!!.getBuffer(RenderTypes.lines())
        val stack = poseStack!!

        stack.translate(pos)

        val positionMatrix = stack.last().pose()
        val normalMatrix = stack.last()

        for (i in 0..<segments) {
            val angle1 = (2.0 * Math.PI * i / segments).toFloat()
            val angle2 = (2.0 * Math.PI * (i + 1) / segments).toFloat()

            val x1 = cos(angle1.toDouble()).toFloat() * radius
            val z1 = sin(angle1.toDouble()).toFloat() * radius
            val x2 = cos(angle2.toDouble()).toFloat() * radius
            val z2 = sin(angle2.toDouble()).toFloat() * radius

            // Line segment: vertex 1
            consumer.addVertex(positionMatrix, x1, 0f, z1)
                .setColor(color)
                .setLineWidth(10.0f)
                .setNormal(normalMatrix, 0f, 1f, 0f)

            // Line segment: vertex 2
            consumer.addVertex(positionMatrix, x2, 0f, z2)
                .setColor(color)
                .setLineWidth(10.0f)
                .setNormal(normalMatrix, 0f, 1f, 0f)
        }

        stack.translate(-pos.x, -pos.y, -pos.z)

        bufferSource!!.endBatch(RenderTypes.lines())
    }
}
