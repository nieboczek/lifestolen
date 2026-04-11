package nieboczek.lifestolen.gui

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.gui.render.state.GuiElementRenderState
import net.minecraft.util.Mth
import org.joml.Matrix3x2f
import kotlin.math.max
import kotlin.math.min

data class RoundedBoxRenderState(
    val pipeline: RenderPipeline,
    val textureSetup: TextureSetup,
    val pose: Matrix3x2f,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val radius: Float,
    val backgroundColor: Int,
    val borderColor: Int,
    val scissorArea: ScreenRectangle?
) : GuiElementRenderState {
    override fun buildVertices(consumer: VertexConsumer) {
        if (width <= 0f || height <= 0f) return

        val maxRadius = min(width, height) / 2f
        val r = max(0f, min(radius, maxRadius))
        val segments = 6 // minimum that seems to look good
        val bg = backgroundColor

        // Central + side rects
        addQuad(consumer, x + r, y + r, x + width - r, y + height - r, bg)
        addQuad(consumer, x + r, y, x + width - r, y + r, bg) // top
        addQuad(consumer, x + r, y + height - r, x + width - r, y + height, bg) // bottom
        addQuad(consumer, x, y + r, x + r, y + height - r, bg) // left
        addQuad(consumer, x + width - r, y + r, x + width, y + height - r, bg) // right

        // Corners (simplified fan with CCW winding)
        val cx = floatArrayOf(x + r, x + width - r, x + width - r, x + r)
        val cy = floatArrayOf(y + r, y + r, y + height - r, y + height - r)
        val startAng = floatArrayOf(Mth.PI, -Mth.HALF_PI, 0f, Mth.HALF_PI)
        val endAng = floatArrayOf(Mth.PI + Mth.HALF_PI, 0f, Mth.HALF_PI, Mth.PI)

        for (c in 0..3) {
            val step = calculateStep(startAng, endAng, c, segments)
            addCorner(consumer, c, step, segments, cx, cy, r, bg, startAng)
        }

        // ===== BORDER =====
        val bw = 1f // border width
        val bc = borderColor
        val bx0 = x
        val by0 = y
        val bx1 = x + width
        val by1 = y + height

        addQuad(consumer, bx0 + r, by0, bx1 - r, by0 + bw, bc) // top
        addQuad(consumer, bx0 + r, by1 - bw, bx1 - r, by1, bc) // bottom
        addQuad(consumer, bx0, by0 + r, bx0 + bw, by1 - r, bc) // left
        addQuad(consumer, bx1 - bw, by0 + r, bx1, by1 - r, bc) // right

        val bcx = floatArrayOf(bx0 + r, bx1 - r, bx1 - r, bx0 + r)
        val bcy = floatArrayOf(by0 + r, by0 + r, by1 - r, by1 - r)

        // Border patches (overlays of background-colored corners on border-colors corners to cover unnecessary circle parts)
        val bpcx = floatArrayOf(bx0 + r + bw, bx1 - r - bw, bx1 - r - bw, bx0 + r + bw)
        val bpcy = floatArrayOf(by0 + r + bw, by0 + r + bw, by1 - r - bw, by1 - r - bw)

        for (c in 0..3) {
            val step = calculateStep(startAng, endAng, c, segments)
            addCorner(consumer, c, step, segments, bcx, bcy, r, bc, startAng)
            addCorner(consumer, c, step, segments, bpcx, bpcy, r, bg, startAng)
        }
    }

    private fun calculateStep(startAng: FloatArray, endAng: FloatArray, c: Int, segments: Int): Float {
        var sweep = endAng[c] - startAng[c]
        if (sweep < 0) sweep += Mth.TWO_PI
        return sweep / segments
    }

    private fun addCorner(
        consumer: VertexConsumer,
        c: Int,
        step: Float,
        segments: Int,
        cx: FloatArray,
        cy: FloatArray,
        r: Float,
        color: Int,
        startAng: FloatArray
    ) {
        for (i in 0..<segments) {
            val a1 = startAng[c] + i * step
            val a2 = startAng[c] + (i + 1) * step
            val p1x = cx[c] + r * Mth.cos(a1.toDouble())
            val p1y = cy[c] + r * Mth.sin(a1.toDouble())
            val p2x = cx[c] + r * Mth.cos(a2.toDouble())
            val p2y = cy[c] + r * Mth.sin(a2.toDouble())

            consumer.addVertexWith2DPose(pose, cx[c], cy[c]).setColor(color)
            consumer.addVertexWith2DPose(pose, p2x, p2y).setColor(color)
            consumer.addVertexWith2DPose(pose, p1x, p1y).setColor(color)
            consumer.addVertexWith2DPose(pose, p1x, p1y).setColor(color)
        }
    }

    private fun addQuad(consumer: VertexConsumer, x0: Float, y0: Float, x1: Float, y1: Float, color: Int) {
        consumer.addVertexWith2DPose(pose, x0, y0).setColor(color)
        consumer.addVertexWith2DPose(pose, x0, y1).setColor(color)
        consumer.addVertexWith2DPose(pose, x1, y1).setColor(color)
        consumer.addVertexWith2DPose(pose, x1, y0).setColor(color)
    }

    override fun pipeline(): RenderPipeline {
        return pipeline
    }

    override fun textureSetup(): TextureSetup {
        return textureSetup
    }

    override fun scissorArea(): ScreenRectangle? {
        return scissorArea
    }

    override fun bounds(): ScreenRectangle {
        return ScreenRectangle(x.toInt(), y.toInt(), width.toInt(), height.toInt())
    }
}
