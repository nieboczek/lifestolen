package nieboczek.lifestolen.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor

fun GuiGraphicsExtractor.blurredRoundedRect(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    fillColor: Int,
    outlineColor: Int = 0,
    outlineWidth: Int = 0,
    radius: Float = 4f,
    blurRadius: Float = 0f,
) {
    val mc = Minecraft.getInstance()
    val window = mc.gameRenderer.gameRenderState().windowRenderState
    val guiScale = window.guiScale
    val physWidth = window.width.toFloat()
    val physHeight = window.height.toFloat()

    val cx = ((x + width / 2f) * guiScale) / physWidth
    val cy = 1.0f - ((y + height / 2f) * guiScale) / physHeight
    val hx = (width * guiScale / 2f) / physWidth
    val hy = (height * guiScale / 2f) / physHeight
    val cr = radius * guiScale

    BlurredRectRenderer.addRect(
        BlurRectData(
            centerX = cx,
            centerY = cy,
            halfSizeX = hx,
            halfSizeY = hy,
            cornerRadius = cr,
            feather = 0.5f,
            blurRadius = blurRadius,
        )
    )

    guiRenderState.addGuiElement(
        RoundedRectRenderState(
            x,
            y,
            x + width,
            y + height,
            fillColor,
            outlineColor,
            outlineWidth,
            radius,
            scissorStack.peek()
        )
    )
}

fun GuiGraphicsExtractor.roundedRect(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    fillColor: Int,
    outlineColor: Int = 0,
    outlineWidth: Int = 0,
    radius: Float = 4f,
) {
    guiRenderState.addGuiElement(
        RoundedRectRenderState(
            x,
            y,
            x + width,
            y + height,
            fillColor,
            outlineColor,
            outlineWidth,
            radius,
            scissorStack.peek()
        )
    )
}
