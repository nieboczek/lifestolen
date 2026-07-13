package nieboczek.lifestolen.gui.widget

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.Block
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.config.setting.*
import nieboczek.lifestolen.gui.friedsvg.FriedSvg
import nieboczek.lifestolen.gui.friedsvg.blitPixel
import nieboczek.lifestolen.gui.render.roundedRect
import org.lwjgl.glfw.GLFW
import java.awt.Color

abstract class SettingWidget<T>(val live: Setting<T>) : Widget() {
    abstract fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int)
    open fun calculateHeight() = ScreenState.FONT_SMALL_HEIGHT
}

class ColorSettingWidget(setting: ColorSetting) : SettingWidget<Int>(setting) {
    var oldHue: Float
    var oldSaturation: Float
    var oldBrightness: Float
    var oldAlpha: Int

    init {
        val v = setting.value
        val arr = Color.RGBtoHSB(v and 0xFF, (v shr 2) and 0xFF, (v shr 4) and 0xFF, null)
        oldHue = arr[0]
        oldSaturation = arr[1]
        oldBrightness = arr[2]
        oldAlpha = (v shr 6) and 0xFF
    }

    override fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int) {}
}

class KeybindSettingWidget(setting: KeybindSetting) : SettingWidget<Int>(setting), Hoverable, Clickable, KeyCapturer {
    var recording = false
    override var hovered = false
    override var hoverProgress = 0f

    override fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int) {
        val width = 40
        val height = 10
        val ax = x - width
        val ay = y - 2

        val outlineColor = ScreenState.lerpOutlineColor(hoverProgress)
        graphics.roundedRect(ax, ay, width, height, 0, outlineColor, ScreenState.OUTLINE_WIDTH, 3f)

        val text =
            if (recording) "..." else if (live.value == 0) "None" else InputConstants.Type.KEYSYM.getOrCreate(live.value).displayName.string
        val textX = ax + ((width - ScreenState.fontExtraSmall.width(text)) / 2)
        val textY = y + (ScreenState.FONT_SMALL_HEIGHT / 2) - (ScreenState.FONT_EXTRA_SMALL_HEIGHT / 2)
        val color = ScreenState.lerpColor(0xDDCCCCCC.toInt(), 0xDDFFFFFF.toInt(), hoverProgress)
        graphics.text(ScreenState.fontExtraSmall, text, textX, textY, color, false)

        bounds = Bounds(ax, ay, width, height)
    }

    override fun click(button: Int): Clickable.Action {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            recording = true
            return Clickable.Action.CAPTURE_KEY
        }
        return Clickable.Action.NONE
    }

    override fun captureKey(key: Int): KeyCapturer.Action {
        live.value = key
        recording = false
        return KeyCapturer.Action.STOP_CAPTURING
    }
}

class BlockListSettingWidget(setting: BlockListSetting) : SettingWidget<MutableList<Block>>(setting) {
    var hoveredIdx = 0
    var hoverProgress = 0f

    override fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int) {}
}

class DoubleSettingWidget(setting: DoubleSetting) : SettingWidget<Double>(setting) {
    var old = setting.value

    override fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int) {}
}

class FloatSettingWidget(setting: FloatSetting) : SettingWidget<Float>(setting) {
    var old = setting.value

    override fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int) {}
}

class IntSettingWidget(setting: IntSetting) : SettingWidget<Int>(setting) {
    var old = setting.value

    override fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int) {}
}

class IntRangeSettingWidget(setting: IntRangeSetting) : SettingWidget<IntRange>(setting) {
    var oldMin = setting.value.first
    var oldMax = setting.value.last

    override fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int) {}
}

class BooleanSettingWidget(setting: BooleanSetting) : SettingWidget<Boolean>(setting), Hoverable, Clickable {
    var enableProgress = if (live.value) 1f else 0f
    override var hovered = false
    override var hoverProgress = 0f

    companion object {
        private val checkmarkHandle = FriedSvg.loadSvg(Lifestolen.identifier("svg/checkmark.svg"))
        private var checkmarkTexture: Identifier? = null

        init {
            FriedSvg.getTextureAsync(checkmarkHandle, 160, 160).thenAccept { checkmarkTexture = it }
        }
    }

    override fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int) {
        val size = 10
        val ax = x - size
        val ay = y - 2
        val color = ScreenState.lerpOutlineColor(hoverProgress)

        graphics.roundedRect(ax, ay, size, size, 0, color, ScreenState.OUTLINE_WIDTH, 3f)

        if (enableProgress > 0f) {
            val clipWidth = (size * enableProgress).toInt()
            graphics.enableScissor(ax - 1, ay, ax + clipWidth, ay + size)
            graphics.blitPixel(checkmarkTexture!!, ax, ay, size, size)
            graphics.disableScissor()
        }

        bounds = Bounds(ax, ay, size, size)
    }

    override fun click(button: Int): Clickable.Action {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) live.value = !live.value
        return Clickable.Action.NONE
    }

    override fun tick(dt: Float) {
        enableProgress =
            if (live.value) (enableProgress + dt).coerceAtMost(1f) else (enableProgress - dt).coerceAtLeast(0f)
    }
}
