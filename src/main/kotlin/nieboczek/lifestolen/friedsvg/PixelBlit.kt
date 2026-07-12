package nieboczek.lifestolen.friedsvg

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.state.gui.GuiElementRenderState
import net.minecraft.resources.Identifier
import org.joml.Matrix3x2f

class PixelBlitRenderState(
    private val pipeline: RenderPipeline,
    private val textureSetup: TextureSetup,
    pixelX: Int,
    pixelY: Int,
    pixelWidth: Int,
    pixelHeight: Int,
    private val u0: Float,
    private val u1: Float,
    private val v0: Float,
    private val v1: Float,
    private val color: Int,
    private val scissorArea: ScreenRectangle?,
) : GuiElementRenderState {
    private val bounds: ScreenRectangle?
    private val gx0 = pixelX.toFloat()
    private val gy0 = pixelY.toFloat()
    private val gx1 = (pixelX + pixelWidth).toFloat()
    private val gy1 = (pixelY + pixelHeight).toFloat()

    init {
        val b = ScreenRectangle(gx0.toInt(), gy0.toInt(), (gx1 - gx0).toInt(), (gy1 - gy0).toInt())
        bounds = if (scissorArea != null) scissorArea.intersection(b) else b
    }

    override fun pipeline(): RenderPipeline = pipeline
    override fun textureSetup(): TextureSetup = textureSetup
    override fun scissorArea(): ScreenRectangle? = scissorArea
    override fun bounds(): ScreenRectangle? = bounds

    override fun buildVertices(consumer: VertexConsumer) {
        val pose = Matrix3x2f()
        consumer.addVertexWith2DPose(pose, gx0, gy0).setUv(u0, v0).setColor(color)
        consumer.addVertexWith2DPose(pose, gx0, gy1).setUv(u0, v1).setColor(color)
        consumer.addVertexWith2DPose(pose, gx1, gy1).setUv(u1, v1).setColor(color)
        consumer.addVertexWith2DPose(pose, gx1, gy0).setUv(u1, v0).setColor(color)
    }
}

fun GuiGraphicsExtractor.blitPixel(texture: Identifier, x: Int, y: Int, width: Int, height: Int) {
    val texture = Minecraft.getInstance().textureManager.getTexture(texture)
    guiRenderState.addGuiElement(
        PixelBlitRenderState(
            RenderPipelines.GUI_TEXTURED,
            TextureSetup.singleTexture(texture.textureView, texture.sampler),
            x,
            y,
            width,
            height,
            0f,
            1f,
            0f,
            1f,
            -1,
            scissorStack.peek()
        )
    )
}
