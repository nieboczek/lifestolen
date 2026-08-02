package nieboczek.lifestolen.util

import com.mojang.blaze3d.PrimitiveTopology
import com.mojang.blaze3d.pipeline.DepthStencilState
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.platform.CompareOp
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.ByteBufferBuilder
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import net.minecraft.client.Camera
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import nieboczek.lifestolen.Lifestolen
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

object Renderer3d {
    @JvmField
    var tickDelta: Float = 0f

    @JvmField
    var camera: Camera? = null

    private var renderTarget: RenderTarget? = null

    private val byteBufferBuilder = ByteBufferBuilder(0x400000)
    private var activeBuilder: BufferBuilder? = null
    private var hasVertices = false

    private val quadByteBufferBuilder = ByteBufferBuilder(0x200000)
    private var quadBuilder: BufferBuilder? = null
    private var quadHasVertices = false

    private val linePipeline: RenderPipeline = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
        .withLocation(Lifestolen.identifier("lines"))
        .withDepthStencilState(DepthStencilState(CompareOp.ALWAYS_PASS, false))
        .build()

    private val quadPipeline: RenderPipeline = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
        .withLocation(Lifestolen.identifier("quads"))
        .withDepthStencilState(DepthStencilState(CompareOp.ALWAYS_PASS, false))
        .build()

    private val viewMatrix = Matrix4f()

    @JvmStatic
    fun setViewMatrix(matrix: Matrix4f) {
        viewMatrix.set(matrix)
    }

    @JvmStatic
    fun beginFrame(target: RenderTarget, cam: Camera) {
        renderTarget = target
        camera = cam
        byteBufferBuilder.clear()
        quadByteBufferBuilder.clear()
        activeBuilder = BufferBuilder(
            byteBufferBuilder,
            PrimitiveTopology.LINES,
            DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH
        )
        quadBuilder = BufferBuilder(
            quadByteBufferBuilder,
            PrimitiveTopology.QUADS,
            DefaultVertexFormat.POSITION_COLOR
        )
        hasVertices = false
        quadHasVertices = false
    }

    @JvmStatic
    fun endFrame() {
        val target = renderTarget ?: return
        renderTarget = null

        val device = RenderSystem.getDevice()
        val stack = RenderSystem.getModelViewStack()

        fun flush(builder: BufferBuilder?, pipeline: RenderPipeline, hasVertices: Boolean) {
            if (builder == null || !hasVertices) return

            val meshData = builder.buildOrThrow()
            val drawState = meshData.drawState()
            val vertexCount = drawState.vertexCount
            if (vertexCount == 0) {
                meshData.close()
                return
            }

            val vertexBuffer = device.createBuffer(
                { "Lifestolen Renderer3d VB" },
                32,
                meshData.vertexBuffer()
            )

            try {
                val indexCount = drawState.indexCount
                val sequentialBuffer = RenderSystem.getSequentialBuffer(drawState.primitiveTopology)
                val indexSlice = sequentialBuffer.getBuffer(indexCount)

                stack.pushMatrix()
                stack.mul(viewMatrix)

                val dynamicTransforms = RenderSystem.getDynamicUniforms()
                    .writeTransform(stack, Vector4f(1.0f), Vector3f(), Matrix4f())

                stack.popMatrix()

                val colorView = target.colorTextureView!!
                val depthView = target.depthTextureView

                device.createCommandEncoder().createRenderPass(
                    { "Lifestolen Renderer3d" },
                    colorView,
                    Optional.empty(),
                    depthView,
                    OptionalDouble.empty()
                ).use { pass ->
                    pass.setPipeline(pipeline)
                    RenderSystem.bindDefaultUniforms(pass)
                    pass.setUniform("DynamicTransforms", dynamicTransforms)
                    pass.setVertexBuffer(0, vertexBuffer.slice())
                    pass.setIndexBuffer(indexSlice, sequentialBuffer.type())
                    // signature 26.1.2: void drawIndexed(final int baseVertex, final int firstIndex, final int indexCount, final int instanceCount)
                    // signature 26.2: void drawIndexed(final int indexCount, final int instanceCount, final int firstIndex, final int vertexOffset, final int firstInstance)
                    pass.drawIndexed(indexCount, 1, 0, 0, 0)
                }
            } finally {
                vertexBuffer.close()
                meshData.close()
            }
        }

        flush(activeBuilder.also { activeBuilder = null }, linePipeline, hasVertices)
        flush(quadBuilder.also { quadBuilder = null }, quadPipeline, quadHasVertices)
    }

    fun computeSmoothRelativeToCameraPos(oldPos: Vec3, pos: Vec3, cameraPos: Vec3): Vec3 {
        val newX = oldPos.x + (pos.x - oldPos.x) * tickDelta - cameraPos.x
        val newY = oldPos.y + (pos.y - oldPos.y) * tickDelta - cameraPos.y
        val newZ = oldPos.z + (pos.z - oldPos.z) * tickDelta - cameraPos.z
        return Vec3(newX, newY, newZ)
    }

    fun drawLine(argb: Int, from: Vector3f, to: Vector3f) {
        val builder = activeBuilder ?: return
        hasVertices = true
        val normal = computeNormal(from, to)
        builder.addVertex(from.x, from.y, from.z).setColor(argb).setNormal(normal.x, normal.y, normal.z)
            .setLineWidth(1f)
        builder.addVertex(to.x, to.y, to.z).setColor(argb).setNormal(normal.x, normal.y, normal.z).setLineWidth(1f)
    }

    fun drawLineWithWidth(argb: Int, width: Float, from: Vector3f, to: Vector3f) {
        val builder = activeBuilder ?: return
        hasVertices = true
        val normal = computeNormal(from, to)
        builder.addVertex(from.x, from.y, from.z).setColor(argb).setNormal(normal.x, normal.y, normal.z)
            .setLineWidth(width)
        builder.addVertex(to.x, to.y, to.z).setColor(argb).setNormal(normal.x, normal.y, normal.z).setLineWidth(width)
    }

    private fun computeNormal(from: Vector3f, to: Vector3f): Vector3f {
        val dx = to.x - from.x
        val dy = to.y - from.y
        val dz = to.z - from.z
        val len = kotlin.math.sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
        if (len < 1e-6f) return Vector3f(0f, 1f, 0f)
        return Vector3f(dx / len, 0f, dz / len)
    }

    fun renderCircleOutline(segments: Int, color: Int, lineWidth: Float, radius: Float, pos: Vec3) {
        val builder = activeBuilder ?: return
        hasVertices = true

        val cx = pos.x.toFloat()
        val cy = pos.y.toFloat()
        val cz = pos.z.toFloat()

        for (i in 0..<segments) {
            val angle1 = (2.0 * Math.PI * i / segments).toFloat()
            val angle2 = (2.0 * Math.PI * (i + 1) / segments).toFloat()

            val x1 = cos(angle1) * radius + cx
            val z1 = sin(angle1) * radius + cz
            val x2 = cos(angle2) * radius + cx
            val z2 = sin(angle2) * radius + cz

            val normal = Vector3f(0f, 1f, 0f)
            builder.addVertex(x1, cy, z1).setColor(color).setNormal(normal.x, normal.y, normal.z)
                .setLineWidth(lineWidth)
            builder.addVertex(x2, cy, z2).setColor(color).setNormal(normal.x, normal.y, normal.z)
                .setLineWidth(lineWidth)
        }
    }

    fun renderBoxOutline(boxDimensions: AABB, color: Int, pos: Vec3, lineWidth: Float = 1f) {
        val builder = activeBuilder ?: return
        hasVertices = true

        val x0 = boxDimensions.minX.toFloat() + pos.x.toFloat()
        val y0 = boxDimensions.minY.toFloat() + pos.y.toFloat()
        val z0 = boxDimensions.minZ.toFloat() + pos.z.toFloat()
        val x1 = boxDimensions.maxX.toFloat() + pos.x.toFloat()
        val y1 = boxDimensions.maxY.toFloat() + pos.y.toFloat()
        val z1 = boxDimensions.maxZ.toFloat() + pos.z.toFloat()

        val edges = arrayOf(
            x0 to y0 to z0, x1 to y0 to z0,
            x1 to y0 to z0, x1 to y0 to z1,
            x1 to y0 to z1, x0 to y0 to z1,
            x0 to y0 to z1, x0 to y0 to z0,
            x0 to y1 to z0, x1 to y1 to z0,
            x1 to y1 to z0, x1 to y1 to z1,
            x1 to y1 to z1, x0 to y1 to z1,
            x0 to y1 to z1, x0 to y1 to z0,
            x0 to y0 to z0, x0 to y1 to z0,
            x1 to y0 to z0, x1 to y1 to z0,
            x1 to y0 to z1, x1 to y1 to z1,
            x0 to y0 to z1, x0 to y1 to z1
        )

        for (i in edges.indices step 2) {
            val (xStart, yStart, zStart) = edges[i]
            val (xEnd, yEnd, zEnd) = edges[i + 1]

            val normal = Vector3f(0f, 1f, 0f)
            builder.addVertex(xStart, yStart, zStart).setColor(color).setNormal(normal.x, normal.y, normal.z)
                .setLineWidth(lineWidth)
            builder.addVertex(xEnd, yEnd, zEnd).setColor(color).setNormal(normal.x, normal.y, normal.z)
                .setLineWidth(lineWidth)
        }
    }

    fun renderBoxFill(boxDimensions: AABB, color: Int, pos: Vec3) {
        val builder = quadBuilder ?: return
        quadHasVertices = true

        val x0 = boxDimensions.minX.toFloat() + pos.x.toFloat()
        val y0 = boxDimensions.minY.toFloat() + pos.y.toFloat()
        val z0 = boxDimensions.minZ.toFloat() + pos.z.toFloat()
        val x1 = boxDimensions.maxX.toFloat() + pos.x.toFloat()
        val y1 = boxDimensions.maxY.toFloat() + pos.y.toFloat()
        val z1 = boxDimensions.maxZ.toFloat() + pos.z.toFloat()

        val faces = arrayOf(
            floatArrayOf(x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0),
            floatArrayOf(x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1),
            floatArrayOf(x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1),
            floatArrayOf(x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1),
            floatArrayOf(x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0),
            floatArrayOf(x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0)
        )

        for (face in faces) {
            repeat(2) {
                builder.addVertex(face[0], face[1], face[2]).setColor(color)
                builder.addVertex(face[3], face[4], face[5]).setColor(color)
                builder.addVertex(face[6], face[7], face[8]).setColor(color)
                builder.addVertex(face[9], face[10], face[11]).setColor(color)
            }
        }
    }

    private infix fun <T> Pair<T, T>.to(value: T): Triple<T, T, T> = Triple(this.first, this.second, value)
}
