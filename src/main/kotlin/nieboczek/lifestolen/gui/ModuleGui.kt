package nieboczek.lifestolen.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.KeyEvent
import net.minecraft.network.chat.Component
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.config.ConfigManager
import nieboczek.lifestolen.gui.widget.Anchor
import nieboczek.lifestolen.gui.widget.Slider
import nieboczek.lifestolen.module.Module
import org.lwjgl.glfw.GLFW
import kotlin.math.min

class ModuleGui : OneToOneScreen(Component.literal(Lifestolen.CLIENT_NAME), Lifestolen.uiFont!!) {
    var isAwaitingBind: Boolean = false
        private set

    private val modules: List<Module> = Lifestolen.modules
    private val categories = LinkedHashMap<Module.Category, MutableList<Module>>()

    private var ignoreNextOpenGuiToggle = false
    private var selectedModuleForBind: Module? = null

    init {
        modules.forEach { categories.computeIfAbsent(it.category) { mutableListOf() }.add(it) }
    }

    fun consumeOpenGuiToggleGuard(): Boolean {
        val guarded = ignoreNextOpenGuiToggle
        ignoreNextOpenGuiToggle = false
        return guarded
    }

    override fun renderOneToOne(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        graphics.fill(0, 0, width, height, -0x3ff6f5f1)

        val label = "Scale: ${(textScale * 100f).toInt()}%"
        val labelWidth = (font.width(label) * textScale).toInt()
        val labelY = height - (6.5f * textScale).toInt() - 4
        drawScaledString(graphics, label, width - labelWidth - 160 - 8, labelY, -0x555556)
    }

    override fun rebuildWidgets() {
        clearWidgets()

        val anchor = Anchor(-4, 0, 160, 32, Anchor.Alignment.BOTTOM_RIGHT, width, height)
        addRenderableWidget(Slider(anchor, 1f, 4f, textScale, 0.01f, { sliderValueChanged(it) }, { sliderEndAdjusting(it) }))
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

    private fun sliderValueChanged(value: Float) {
        textScale = value
    }

    private fun sliderEndAdjusting(value: Float) {
        textScale = value
        Lifestolen.cfg!!.textScale = value
        ConfigManager.saveConfig()
    }

    private fun drawWrappedString(graphics: GuiGraphics, text: String, x: Int, y: Int, maxWidth: Int, color: Int) {
        val lineHeight = (15f * textScale).toInt()
        var offset = 0

        for (line in font.split(Component.literal(text), maxWidth)) {
            drawScaledString(graphics, line, x, y + offset, color)
            offset += lineHeight
        }
    }

    private fun brighten(color: Int): Int {
        val alpha = color and -0x1000000
        val red = min(((color shr 16) and 0xFF) + 24, 255)
        val green = min(((color shr 8) and 0xFF) + 24, 255)
        val blue = min((color and 0xFF) + 24, 255)
        return alpha or (red shl 16) or (green shl 8) or blue
    }

    override fun onClose() {
        isAwaitingBind = false
        ignoreNextOpenGuiToggle = false
        super.onClose()
    }
}
