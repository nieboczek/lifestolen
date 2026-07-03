package nieboczek.friedfabricsvg.render

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.state.gui.GuiElementRenderState
import net.minecraft.resources.Identifier

class PixelBlitRenderState(
    pipeline: RenderPipeline,
    textureSetup: TextureSetup,
    pixelX: Int,
    pixelY: Int,
    pixelWidth: Int,
    pixelHeight: Int,
    private val u0: Float,
    private val u1: Float,
    private val v0: Float,
    private val v1: Float,
    private val color: Int,
    scissorArea: ScreenRectangle?
) : GuiElementRenderState {

    private val _pipeline = pipeline
    private val _textureSetup = textureSetup
    private val _scissorArea = scissorArea

    private val gx0: Float
    private val gy0: Float
    private val gx1: Float
    private val gy1: Float
    private val _bounds: ScreenRectangle?

    init {
        val guiScale = Minecraft.getInstance().window.guiScale
        gx0 = (pixelX / guiScale).toFloat()
        gy0 = (pixelY / guiScale).toFloat()
        gx1 = ((pixelX + pixelWidth) / guiScale).toFloat()
        gy1 = ((pixelY + pixelHeight) / guiScale).toFloat()
        val b = ScreenRectangle(gx0.toInt(), gy0.toInt(), (gx1 - gx0).toInt(), (gy1 - gy0).toInt())
        _bounds = if (_scissorArea != null) _scissorArea.intersection(b) else b
    }

    override fun pipeline(): RenderPipeline = _pipeline

    override fun textureSetup(): TextureSetup = _textureSetup

    override fun scissorArea(): ScreenRectangle? = _scissorArea

    override fun buildVertices(consumer: VertexConsumer) {
        consumer.addVertex(gx0, gy0, 0.0f).setUv(u0, v0).setColor(color)
        consumer.addVertex(gx0, gy1, 0.0f).setUv(u0, v1).setColor(color)
        consumer.addVertex(gx1, gy1, 0.0f).setUv(u1, v1).setColor(color)
        consumer.addVertex(gx1, gy0, 0.0f).setUv(u1, v0).setColor(color)
    }

    override fun bounds(): ScreenRectangle? = _bounds
}

fun GuiGraphicsExtractor.blitPixel(
    pipeline: RenderPipeline,
    texture: Identifier,
    x: Int,
    y: Int,
    width: Int,
    height: Int
) {
    val texture = Minecraft.getInstance().textureManager.getTexture(texture)
    guiRenderState.addGuiElement(
        PixelBlitRenderState(
            pipeline,
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
