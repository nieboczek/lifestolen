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
    private val x0: Int,
    private val y0: Int,
    private val x1: Int,
    private val y1: Int,
    private val fillColor: Int,
    private val outlineColor: Int,
    private val outlineWidth: Int = 1,
    private val radius: Float = 4f,
    private val scissorArea: ScreenRectangle? = null
) : GuiElementRenderState {
    override fun pipeline() = roundedRectPipeline
    override fun textureSetup() = TextureSetup.noTexture()
    override fun scissorArea() = scissorArea
    override fun bounds() = ScreenRectangle(x0, y0, x1 - x0, y1 - y0)

    override fun buildVertices(vertexConsumer: VertexConsumer) {
        val rw = x1 - x0
        val rh = y1 - y0
        val hw = rw / 2
        val hh = rh / 2
        val pose = Matrix3x2f()

        val guiScale = Minecraft.getInstance().window.guiScale.coerceAtLeast(1)
        val scaledOutlineWidth = (outlineWidth.toFloat() / guiScale.toFloat() * 256f).toInt().coerceIn(0, 32767)

        val or = (outlineColor shr 16 and 0xFF).toFloat() / 255f
        val og = (outlineColor shr 8 and 0xFF).toFloat() / 255f
        val ob = (outlineColor and 0xFF).toFloat() / 255f
        val oa = outlineColor shr 24 and 0xFF

        vertexConsumer.addVertexWith2DPose(pose, x0.toFloat(), y0.toFloat())
            .setUv(0f, 0f)
            .setColor(fillColor)
            .setUv1(hw, hh)
            .setUv2(scaledOutlineWidth, oa)
            .setLineWidth(radius)
            .setNormal(or, og, ob)

        vertexConsumer.addVertexWith2DPose(pose, x0.toFloat(), y1.toFloat())
            .setUv(0f, rh.toFloat())
            .setColor(fillColor)
            .setUv1(hw, hh)
            .setUv2(scaledOutlineWidth, oa)
            .setLineWidth(radius)
            .setNormal(or, og, ob)

        vertexConsumer.addVertexWith2DPose(pose, x1.toFloat(), y1.toFloat())
            .setUv(rw.toFloat(), rh.toFloat())
            .setColor(fillColor)
            .setUv1(hw, hh)
            .setUv2(scaledOutlineWidth, oa)
            .setLineWidth(radius)
            .setNormal(or, og, ob)

        vertexConsumer.addVertexWith2DPose(pose, x1.toFloat(), y0.toFloat())
            .setUv(rw.toFloat(), 0f)
            .setColor(fillColor)
            .setUv1(hw, hh)
            .setUv2(scaledOutlineWidth, oa)
            .setLineWidth(radius)
            .setNormal(or, og, ob)
    }

    companion object {
        val format = VertexFormat.builder(0)
            .addAttribute("Position", GpuFormat.RGB32_FLOAT)
            .addAttribute("UV0", GpuFormat.RG32_FLOAT)
            .addAttribute("Color", GpuFormat.RGBA8_UNORM)
            .addAttribute("UV1", GpuFormat.RG16_SINT)
            .addAttribute("UV2", GpuFormat.RG16_SINT)
            .addAttribute("LineWidth", GpuFormat.R32_FLOAT)
            .addAttribute("Normal", GpuFormat.RGBA8_SNORM)
            .build()

        val roundedRectPipeline = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                .withLocation(Lifestolen.identifier("pipeline/rounded_rect"))
                .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                .withVertexShader(Lifestolen.identifier("core/rounded_rect"))
                .withFragmentShader(Lifestolen.identifier("core/rounded_rect"))
                .withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
                .withVertexBinding(0, format)
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .build()
        )
    }
}
