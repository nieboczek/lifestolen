package nieboczek.lifestolen.gui.render

import com.mojang.blaze3d.GpuFormat
import com.mojang.blaze3d.PrimitiveTopology
import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.BindGroupLayouts
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.state.gui.GuiElementRenderState
import nieboczek.lifestolen.Lifestolen
import org.joml.Matrix3x2f
import kotlin.math.roundToInt

class ColorPickerRenderState(
    private val x0: Float,
    private val y0: Float,
    private val x1: Float,
    private val y1: Float,
    private val pickerType: Int,
    private val hue: Float,
    private val saturation: Float = 1f,
    private val value: Float = 1f,
    private val scissorArea: ScreenRectangle? = null
) : GuiElementRenderState {
    override fun pipeline() = colorPickerPipeline
    override fun textureSetup() = TextureSetup.noTexture()
    override fun scissorArea() = scissorArea
    override fun bounds() = ScreenRectangle(x0.toInt(), y0.toInt(), (x1 - x0).toInt(), (y1 - y0).toInt())

    override fun buildVertices(vertexConsumer: VertexConsumer) {
        val w = x1 - x0
        val h = y1 - y0
        val hw = w / 2f
        val hh = h / 2f
        val hueByte = (hue * 255f).roundToInt().coerceIn(0, 255)
        val satByte = (saturation * 255f).roundToInt().coerceIn(0, 255)
        val valByte = (value * 255f).roundToInt().coerceIn(0, 255)
        val encodedColor = (pickerType shl 24) or (hueByte shl 16) or (satByte shl 8) or valByte
        val hwBits = java.lang.Float.floatToFloat16(hw).toInt()
        val hhBits = java.lang.Float.floatToFloat16(hh).toInt()
        val pose = Matrix3x2f()

        vertexConsumer.addVertexWith2DPose(pose, x0, y0)
            .setUv(0f, 0f)
            .setColor(encodedColor)
            .setUv1(hwBits, hhBits)
            .setLineWidth(CORNER_RADIUS)

        vertexConsumer.addVertexWith2DPose(pose, x0, y1)
            .setUv(0f, h)
            .setColor(encodedColor)
            .setUv1(hwBits, hhBits)
            .setLineWidth(CORNER_RADIUS)

        vertexConsumer.addVertexWith2DPose(pose, x1, y1)
            .setUv(w, h)
            .setColor(encodedColor)
            .setUv1(hwBits, hhBits)
            .setLineWidth(CORNER_RADIUS)

        vertexConsumer.addVertexWith2DPose(pose, x1, y0)
            .setUv(w, 0f)
            .setColor(encodedColor)
            .setUv1(hwBits, hhBits)
            .setLineWidth(CORNER_RADIUS)
    }

    companion object {
        private const val CORNER_RADIUS = 2f

        val format = VertexFormat.builder(0)
            .addAttribute("Position", GpuFormat.RGB32_FLOAT)
            .addAttribute("UV0", GpuFormat.RG32_FLOAT)
            .addAttribute("Color", GpuFormat.RGBA8_UNORM)
            .addAttribute("UV1", GpuFormat.RG16_FLOAT)
            .addAttribute("LineWidth", GpuFormat.R32_FLOAT)
            .build()

        val colorPickerPipeline = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                .withLocation(Lifestolen.identifier("pipeline/color_picker"))
                .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                .withVertexShader(Lifestolen.identifier("core/color_picker"))
                .withFragmentShader(Lifestolen.identifier("core/color_picker"))
                .withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
                .withVertexBinding(0, format)
                .withPrimitiveTopology(PrimitiveTopology.QUADS).build()
        )

        const val TYPE_SV_SQUARE = 0
        const val TYPE_HUE_SLIDER = 1
        const val TYPE_ALPHA_SLIDER = 2
    }
}
