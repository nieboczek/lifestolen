package nieboczek.lifestolen.gui.render

import com.mojang.blaze3d.GpuFormat
import com.mojang.blaze3d.PrimitiveTopology
import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.BindGroupLayouts
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.state.gui.GuiElementRenderState
import nieboczek.lifestolen.Lifestolen
import org.joml.Matrix3x2f

class RoundedRectRenderState(
    private val x0: Float,
    private val y0: Float,
    private val x1: Float,
    private val y1: Float,
    private val fillColor: Int,
    private val outlineColor: Int,
    private val outlineWidth: Int = 1,
    private val radius: Float = 4f,
    private val scissorArea: ScreenRectangle? = null
) : GuiElementRenderState {
    override fun pipeline() = roundedRectPipeline
    override fun textureSetup() = TextureSetup.noTexture()
    override fun scissorArea() = scissorArea
    override fun bounds() = ScreenRectangle(x0.toInt(), y0.toInt(), (x1 - x0).toInt(), (y1 - y0).toInt())

    override fun buildVertices(vertexConsumer: VertexConsumer) {
        val rw = x1 - x0
        val rh = y1 - y0
        val hw = rw / 2f
        val hh = rh / 2f
        val pose = Matrix3x2f()

        val guiScale = Minecraft.getInstance().window.guiScale.coerceAtLeast(1)
        val scaledOutlineWidth = (outlineWidth.toFloat() / guiScale.toFloat() * 256f).toInt().coerceIn(0, 32767)

        val or = (outlineColor shr 16 and 0xFF).toFloat() / 255f
        val og = (outlineColor shr 8 and 0xFF).toFloat() / 255f
        val ob = (outlineColor and 0xFF).toFloat() / 255f
        val oa = outlineColor shr 24 and 0xFF

        val uv1u = java.lang.Float.floatToFloat16(hw).toInt()
        val uv1v = java.lang.Float.floatToFloat16(hh).toInt()

        vertexConsumer.addVertexWith2DPose(pose, x0, y0).setUv(0f, 0f).setColor(fillColor)
            .setUv1(uv1u, uv1v).setUv2(scaledOutlineWidth, oa).setLineWidth(radius).setNormal(or, og, ob)

        vertexConsumer.addVertexWith2DPose(pose, x0, y1).setUv(0f, rh).setColor(fillColor)
            .setUv1(uv1u, uv1v).setUv2(scaledOutlineWidth, oa).setLineWidth(radius).setNormal(or, og, ob)

        vertexConsumer.addVertexWith2DPose(pose, x1, y1).setUv(rw, rh)
            .setColor(fillColor).setUv1(uv1u, uv1v).setUv2(scaledOutlineWidth, oa).setLineWidth(radius)
            .setNormal(or, og, ob)

        vertexConsumer.addVertexWith2DPose(pose, x1, y0).setUv(rw, 0f).setColor(fillColor)
            .setUv1(uv1u, uv1v).setUv2(scaledOutlineWidth, oa).setLineWidth(radius).setNormal(or, og, ob)
    }

    companion object {
        val format = VertexFormat.builder(0).addAttribute("Position", GpuFormat.RGB32_FLOAT)
            .addAttribute("UV0", GpuFormat.RG32_FLOAT).addAttribute("Color", GpuFormat.RGBA8_UNORM)
            .addAttribute("UV1", GpuFormat.RG16_FLOAT).addAttribute("UV2", GpuFormat.RG16_SINT)
            .addAttribute("LineWidth", GpuFormat.R32_FLOAT).addAttribute("Normal", GpuFormat.RGBA8_SNORM).build()

        val roundedRectPipeline = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                .withLocation(Lifestolen.identifier("pipeline/rounded_rect"))
                .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                .withVertexShader(Lifestolen.identifier("core/rounded_rect"))
                .withFragmentShader(Lifestolen.identifier("core/rounded_rect"))
                .withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT)).withVertexBinding(0, format)
                .withPrimitiveTopology(PrimitiveTopology.QUADS).build()
        )
    }
}
