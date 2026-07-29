package nieboczek.lifestolen.gui

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.gui.widget.*
import org.lwjgl.glfw.GLFW
import java.awt.Color
import java.util.*

class ConfigScreen : Screen(Minecraft.getInstance(), Lifestolen.font, Component.literal(Lifestolen.CLIENT_NAME)) {
    companion object {
        private val rootWidget = RootWidget()
    }

    private var currentlyHovered = ArrayList<Hoverable>(4)
    private var currentlyCapturing: KeyCapturer? = null
    private var currentlyDragging: Draggable? = null
    private var rainbowColorOffset = 0f

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        ScreenState.guiScale = minecraft.window.guiScale.toFloat()

        rainbowColorOffset += a
        val hue = (rainbowColorOffset % 60f) / 60f
        ScreenState.rainbowColor = Color.HSBtoRGB(hue, 0.5f, 1f)
        ScreenState.darkRainbowColor = Color.HSBtoRGB(hue, 0.5f, 0.75f)

        val dt = a * 0.5f
        walkWidgets {
            it.tick(dt)
            if (it is Hoverable) {
                it.hoverProgress = if (it.hovered) (it.hoverProgress + dt).coerceAtMost(1f)
                else (it.hoverProgress - dt).coerceAtLeast(0f)
            }
            if (it is Draggable) {
                it.dragProgress = if (it.dragging) (it.dragProgress + dt).coerceAtMost(1f)
                else (it.dragProgress - dt).coerceAtLeast(0f)
            }
            return@walkWidgets false
        }

        rootWidget.render(graphics, width)

        if (ScreenState.debugMode) {
            val text = "Debug mode is active. Press F1 to deactivate."
            graphics.centeredText(font, text, width / 2, height - ScreenState.FONT_HEIGHT - 8, ScreenState.rainbowColor)
        }
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        currentlyCapturing?.let {
            val action = it.captureKey(event.key)
            if (action == KeyCapturer.Action.STOP_CAPTURING) currentlyCapturing = null
            return true
        }

        if (event.key == GLFW.GLFW_KEY_F1) {
            ScreenState.debugMode = !ScreenState.debugMode
            return true
        }

        val guiKey = KeyMappingHelper.getBoundKeyOf(minecraft.options.keySocialInteractions).value
        if (event.key == guiKey || event.isEscape) onClose()
        return true
    }

    override fun mouseMoved(x: Double, y: Double) {
        currentlyHovered.forEach { it.hovered = false }
        currentlyHovered.clear()

        walkWidgets {
            if (it is Hoverable && it.bounds.isInBounds(x, y)) {
                it.hovered = true
                currentlyHovered.add(it)
            }
            return@walkWidgets false
        }
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        if (currentlyCapturing != null) return true

        walkWidgets {
            if (it is Clickable && it.bounds.isInBounds(event)) {
                val action = it.click(event.button())

                if (action == Clickable.Action.CAPTURE_KEY) {
                    if (it !is KeyCapturer) {
                        error("${it.javaClass.simpleName} responded with Clickable.Action.CAPTURE_KEY, but is not a KeyCapturer")
                    }
                    currentlyCapturing = it
                }

                return@walkWidgets true
            }
            if (it is Draggable && it.bounds.isInBounds(event)) {
                currentlyDragging = it
                it.dragging = true
                it.drag(event.x, event.y)
                return@walkWidgets true
            }
            return@walkWidgets false
        }
        return true
    }

    override fun mouseDragged(event: MouseButtonEvent, dx: Double, dy: Double): Boolean {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return true
        currentlyDragging?.drag(event.x, event.y)
        return true
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            currentlyDragging?.let {
                it.dragging = false
                currentlyDragging = null
            }
        }
        return true
    }

    override fun onClose() {
        walkWidgets {
            if (it is Hoverable) {
                it.hoverProgress = 0f
                it.hovered = false
            }
            return@walkWidgets false
        }
        super.onClose()
    }

    private fun walkWidgets(walker: (Widget) -> Boolean) {
        val stack = Stack<Widget>()
        stack.addAll(rootWidget.getVisibleChildren())

        while (stack.isNotEmpty()) {
            val w = stack.pop()
            if (walker(w)) return
            stack.addAll(w.getVisibleChildren())
        }
    }

    override fun extractTransparentBackground(graphics: GuiGraphicsExtractor) = graphics.blurBeforeThisStratum()
    override fun isInGameUi() = true
    override fun isPauseScreen() = false
}
