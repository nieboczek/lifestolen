package nieboczek.lifestolen.gui.render

import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.state.gui.GuiElementRenderState
import org.joml.Matrix3x2f

class RectRenderState(
    private val x0: Float,
    private val y0: Float,
    private val x1: Float,
    private val y1: Float,
    private val fillColor: Int,
    private val scissorArea: ScreenRectangle? = null
) : GuiElementRenderState {
    override fun pipeline() = RenderPipelines.GUI
    override fun textureSetup() = TextureSetup.noTexture()
    override fun scissorArea() = scissorArea
    override fun bounds() = ScreenRectangle(x0.toInt(), y0.toInt(), (x1 - x0).toInt(), (y1 - y0).toInt())

    override fun buildVertices(vertexConsumer: VertexConsumer) {
        val pose = Matrix3x2f()
        vertexConsumer.addVertexWith2DPose(pose, x0, y0).setColor(fillColor)
        vertexConsumer.addVertexWith2DPose(pose, x0, y1).setColor(fillColor)
        vertexConsumer.addVertexWith2DPose(pose, x1, y1).setColor(fillColor)
        vertexConsumer.addVertexWith2DPose(pose, x1, y0).setColor(fillColor)
    }
}
