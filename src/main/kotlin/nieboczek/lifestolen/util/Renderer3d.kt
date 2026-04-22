package nieboczek.lifestolen.util

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.world.phys.AABB
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

    fun computeSmoothRelativeToCameraPos(oldPos: Vec3, pos: Vec3, cameraPos: Vec3): Vec3 {
        val newX = oldPos.x + (pos.x - oldPos.x) * tickDelta - cameraPos.x
        val newY = oldPos.y + (pos.y - oldPos.y) * tickDelta - cameraPos.y
        val newZ = oldPos.z + (pos.z - oldPos.z) * tickDelta - cameraPos.z
        return Vec3(newX, newY, newZ)
    }

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

    fun renderBoxOutline(boxDimensions: AABB, color: Int, pos: Vec3) {
        val consumer = bufferSource!!.getBuffer(RenderTypes.lines())
        val stack = poseStack!!

        stack.pushPose()
        stack.translate(pos)

        val positionMatrix = stack.last().pose()
        val normalMatrix = stack.last()

        val x0 = boxDimensions.minX.toFloat()
        val y0 = boxDimensions.minY.toFloat()
        val z0 = boxDimensions.minZ.toFloat()
        val x1 = boxDimensions.maxX.toFloat()
        val y1 = boxDimensions.maxY.toFloat()
        val z1 = boxDimensions.maxZ.toFloat()

        val edges = arrayOf(
            // Bottom face
            x0 to y0 to z0, x1 to y0 to z0,
            x1 to y0 to z0, x1 to y0 to z1,
            x1 to y0 to z1, x0 to y0 to z1,
            x0 to y0 to z1, x0 to y0 to z0,
            // Top face
            x0 to y1 to z0, x1 to y1 to z0,
            x1 to y1 to z0, x1 to y1 to z1,
            x1 to y1 to z1, x0 to y1 to z1,
            x0 to y1 to z1, x0 to y1 to z0,
            // Vertical edges
            x0 to y0 to z0, x0 to y1 to z0,
            x1 to y0 to z0, x1 to y1 to z0,
            x1 to y0 to z1, x1 to y1 to z1,
            x0 to y0 to z1, x0 to y1 to z1
        )

        for (i in edges.indices step 2) {
            val (xStart, yStart, zStart) = edges[i]
            val (xEnd, yEnd, zEnd) = edges[i + 1]

            consumer.addVertex(positionMatrix, xStart, yStart, zStart)
                .setColor(color)
                .setLineWidth(2.5f)
                .setNormal(normalMatrix, 0f, 1f, 0f)

            consumer.addVertex(positionMatrix, xEnd, yEnd, zEnd)
                .setColor(color)
                .setLineWidth(2.5f)
                .setNormal(normalMatrix, 0f, 1f, 0f)
        }

        stack.popPose()
        bufferSource!!.endBatch(RenderTypes.lines())
    }

    // Helper infix function to create Triple from nested Pairs for cleaner edge definition
    private infix fun <T> Pair<T, T>.to(value: T): Triple<T, T, T> = Triple(this.first, this.second, value)
}