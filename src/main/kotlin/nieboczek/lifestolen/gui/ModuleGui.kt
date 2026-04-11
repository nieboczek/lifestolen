package nieboczek.lifestolen.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.locale.Language
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.config.ConfigManager
import nieboczek.lifestolen.module.Module
import org.joml.Matrix3x2f
import org.lwjgl.glfw.GLFW
import kotlin.math.min

class ModuleGui : OneToOneScreen(Component.literal(Lifestolen.CLIENT_NAME), Lifestolen.uiFont!!) {
    var isAwaitingBind: Boolean = false
        private set

    private val modules: List<Module> = Lifestolen.modules
    private val categories: MutableMap<Module.Category, MutableList<Module>> =
        LinkedHashMap<Module.Category, MutableList<Module>>()

    private var ignoreNextOpenGuiToggle = false
    private var selectedModuleForBind: Module? = null
    private var draggingSlider = false
    private var textScale: Float

    companion object {
        private const val SLIDER_WIDTH = 160
        private const val SLIDER_HEIGHT = 32
        private const val SLIDER_MIN = 1f
        private const val SLIDER_MAX = 4f
    }

    init {
        textScale = Lifestolen.cfg!!.textScale
    }

    public override fun renderOneToOne(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        graphics.fill(0, 0, width, height, -0x3ff6f5f1)

        // render here

        renderTextScaleSlider(graphics, mouseX, mouseY)
    }

    override fun rebuildWidgets() {
        clearWidgets()
    }

    override fun keyPressed(keyEvent: KeyEvent): Boolean {
        if (isAwaitingBind && selectedModuleForBind != null) {
            val keycode = keyEvent.key()

            if (keycode == GLFW.GLFW_KEY_ESCAPE) {
                isAwaitingBind = false
                selectedModuleForBind = null
                return true
            }

            if (keycode == GLFW.GLFW_KEY_BACKSPACE || keycode == GLFW.GLFW_KEY_DELETE) {
                selectedModuleForBind!!.keybind = 0
            } else {
                selectedModuleForBind!!.keybind = keycode
            }

            ignoreNextOpenGuiToggle = true
            isAwaitingBind = false
            selectedModuleForBind = null
            ConfigManager.saveConfig()
            return true
        }
        return super.keyPressed(keyEvent)
    }

    public override fun mouseClickedOneToOne(mouse: MouseButtonEvent, isDoubleClick: Boolean): Boolean {
        val sliderX = width - SLIDER_WIDTH - 8
        val sliderY = height - SLIDER_HEIGHT - 8

        if (mouse.button() == 0 && mouse.x() >= sliderX && mouse.x() < sliderX + SLIDER_WIDTH && mouse.y() >= sliderY && mouse.y() < sliderY + SLIDER_HEIGHT) {
            var ratio = (mouse.x() - sliderX).toFloat() / SLIDER_WIDTH
            ratio = Math.clamp(ratio, 0f, 1f)

            textScale = SLIDER_MIN + ratio * (SLIDER_MAX - SLIDER_MIN)
            textScale = (textScale / 25) * 25
            draggingSlider = true

            Lifestolen.cfg!!.textScale = textScale
            ConfigManager.saveConfig()
            return true
        }

        return super.mouseClickedOneToOne(mouse, isDoubleClick)
    }

    public override fun mouseDraggedOneToOne(
        mouse: MouseButtonEvent,
        draggedDistanceX: Double,
        draggedDistanceY: Double
    ): Boolean {
        if (draggingSlider) {
            val sliderX = width - SLIDER_WIDTH - 8
            var ratio = (mouse.x() - sliderX).toFloat() / SLIDER_WIDTH
            ratio = Math.clamp(ratio, 0f, 1f)
            textScale = SLIDER_MIN + ratio * (SLIDER_MAX - SLIDER_MIN)
            textScale = (textScale / 25) * 25

            Lifestolen.cfg!!.textScale = textScale
            return true
        }

        return super.mouseDraggedOneToOne(mouse, draggedDistanceX, draggedDistanceY)
    }

    public override fun mouseReleasedOneToOne(mouse: MouseButtonEvent): Boolean {
        if (draggingSlider && mouse.button() == 0) {
            draggingSlider = false
            ConfigManager.saveConfig()
            return true
        }
        return super.mouseReleasedOneToOne(mouse)
    }

    private fun renderTextScaleSlider(graphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val sliderX = width - SLIDER_WIDTH - 8
        val sliderY = height - SLIDER_HEIGHT - 8

        val label = "Scale: " + (textScale * 100f).toInt() + "%"
        val labelWidth = (font.width(label) * textScale).toInt()
        val labelY = sliderY - (6.5f * textScale).toInt() + SLIDER_HEIGHT
        drawScaledString(graphics, label, sliderX - labelWidth - 4, labelY, -0x555556)

        renderRoundedBox(graphics, sliderX, sliderY, SLIDER_WIDTH, SLIDER_HEIGHT, 4f, -0xd5d5d6, -0xb5b5b6)

        val ratio = (textScale - SLIDER_MIN) / (SLIDER_MAX - SLIDER_MIN)
        val knobWidth = 8
        val knobX = sliderX + ((SLIDER_WIDTH - knobWidth) * ratio).toInt()
        graphics.fill(knobX, sliderY + 2, knobX + knobWidth, sliderY + SLIDER_HEIGHT - 2, -0xaa5501)

        if (mouseX >= sliderX && mouseX < sliderX + SLIDER_WIDTH && mouseY >= sliderY && mouseY < sliderY + SLIDER_HEIGHT) {
            graphics.fill(sliderX, sliderY, sliderX + SLIDER_WIDTH, sliderY + SLIDER_HEIGHT, 0x20FFFFFF)
        }
    }

    fun consumeOpenGuiToggleGuard(): Boolean {
        val guarded = ignoreNextOpenGuiToggle
        ignoreNextOpenGuiToggle = false
        return guarded
    }

    private fun drawWrappedString(graphics: GuiGraphics, text: String, x: Int, y: Int, maxWidth: Int, color: Int) {
        val lineHeight = (15f * textScale).toInt()
        var offset = 0

        for (line in font.split(Component.literal(text), maxWidth)) {
            drawScaledString(graphics, line, x, y + offset, color)
            offset += lineHeight
        }
    }

    fun drawScaledString(graphics: GuiGraphics, text: String, x: Int, y: Int, color: Int) {
        drawScaledString(graphics, Language.getInstance().getVisualOrder(FormattedText.of(text)), x, y, color)
    }

    fun drawScaledString(graphics: GuiGraphics, text: FormattedCharSequence, x: Int, y: Int, color: Int) {
        graphics.pose().pushMatrix()
        graphics.pose().scale(textScale, textScale)
        val scaledX = (x / textScale).toInt()
        val scaledY = (y / textScale).toInt()
        graphics.drawString(font, text, scaledX, scaledY, color, false)
        graphics.pose().popMatrix()
    }

    private fun brighten(color: Int): Int {
        val alpha = color and -0x1000000
        val red = min(((color shr 16) and 0xFF) + 24, 255)
        val green = min(((color shr 8) and 0xFF) + 24, 255)
        val blue = min((color and 0xFF) + 24, 255)
        return alpha or (red shl 16) or (green shl 8) or blue
    }

    private fun renderRoundedBox(
        graphics: GuiGraphics,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        radius: Float,
        backgroundColor: Int,
        borderColor: Int
    ) {
        graphics.guiRenderState.submitGuiElement(
            RoundedBoxRenderState(
                RenderPipelines.GUI,
                TextureSetup.noTexture(),
                Matrix3x2f(graphics.pose()),
                x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(),
                radius,
                backgroundColor, borderColor,
                graphics.scissorStack.peek()
            )
        )
    }

    override fun onClose() {
        this.isAwaitingBind = false
        ignoreNextOpenGuiToggle = false
        super.onClose()
    }
}
