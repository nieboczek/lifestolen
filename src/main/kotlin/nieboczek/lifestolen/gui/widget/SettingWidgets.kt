package nieboczek.lifestolen.gui.widget

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.Block
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.Lifestolen.fontExtraSmall
import nieboczek.lifestolen.config.setting.*
import nieboczek.lifestolen.gui.friedsvg.FriedSvg
import nieboczek.lifestolen.gui.friedsvg.blitPixel
import nieboczek.lifestolen.gui.render.ColorPickerRenderState
import nieboczek.lifestolen.gui.render.colorPickerRect
import nieboczek.lifestolen.gui.render.rect
import nieboczek.lifestolen.gui.render.roundedRect
import nieboczek.lifestolen.gui.widget.ScreenState.FONT_EXTRA_SMALL_HEIGHT
import nieboczek.lifestolen.gui.widget.ScreenState.FONT_SMALL_HEIGHT
import nieboczek.lifestolen.gui.widget.ScreenState.OUTLINE_COLOR
import nieboczek.lifestolen.gui.widget.ScreenState.OUTLINE_WIDTH
import nieboczek.lifestolen.gui.widget.ScreenState.lerpColor
import nieboczek.lifestolen.gui.widget.ScreenState.lerpOutlineColor
import org.lwjgl.glfw.GLFW
import java.awt.Color
import kotlin.math.max
import kotlin.math.round

abstract class SettingWidget<T>(val live: Setting<T>) : Widget() {
    abstract fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int)
    open fun calculateHeight() = FONT_SMALL_HEIGHT
}

class ColorSettingWidget(setting: ColorSetting) : SettingWidget<Int>(setting) {
    private var hue: Float
    private var saturation: Float
    private var brightness: Float
    private var alpha: Float

    private val size = 48

    private inner class SvSquareRect : Widget(), Draggable {
        override var dragging = false
        override var dragProgress = 0f

        var rx = 0
        var ry = 0
        var rw = 0
        var rh = 0

        override fun drag(x: Double, y: Double) {
            saturation = ((x - rx) / rw).coerceIn(0.0, 1.0).toFloat()
            brightness = (1.0 - (y - ry) / rh).coerceIn(0.0, 1.0).toFloat()
            updateColor()
        }
    }

    private inner class HueSliderRect : Widget(), Draggable {
        override var dragging = false
        override var dragProgress = 0f

        var ry = 0
        var rh = 0

        override fun drag(x: Double, y: Double) {
            hue = ((y - ry) / rh).coerceIn(0.0, 1.0).toFloat()
            updateColor()
        }
    }

    private inner class AlphaSliderRect : Widget(), Draggable {
        override var dragging = false
        override var dragProgress = 0f

        var ry = 0
        var rh = 0

        override fun drag(x: Double, y: Double) {
            alpha = (1.0 - (y - ry) / rh).coerceIn(0.0, 1.0).toFloat()
            updateColor()
        }
    }

    private val svSquare = SvSquareRect()
    private val hueSlider = HueSliderRect()
    private val alphaSlider = AlphaSliderRect()

    private fun updateColor() {
        val rgb = Color.HSBtoRGB(hue, saturation, brightness)
        live.value = ((alpha * 255f).toInt() shl 24) or (rgb and 0xFFFFFF)
    }

    init {
        val v = setting.value
        val arr = Color.RGBtoHSB((v shr 16) and 0xFF, (v shr 8) and 0xFF, v and 0xFF, null)
        hue = arr[0]
        saturation = arr[1]
        brightness = arr[2]
        alpha = ((v shr 24) and 0xFF) / 255f
    }

    override fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int) {
        val sliderWidth = 8
        val gap = 4
        val ay = y - 2
        val squareX = x - size
        val barOverhang = 1f
        val barWidth = sliderWidth + (barOverhang * 2f)
        val barHeight = 2f
        val barYOffset = barHeight / -2f
        val barFill = 0xFFFFFFFF.toInt()
        val barOutline = 0xFF000000.toInt()

        graphics.colorPickerRect(
            squareX, ay, size, size, ColorPickerRenderState.TYPE_SV_SQUARE, hue
        )

        val circleSize = 4
        val cx = squareX + size * saturation
        val cy = ay + size * (1 - brightness)

        graphics.roundedRect(
            cx - circleSize / 2,
            cy - circleSize / 2,
            circleSize.toFloat(),
            circleSize.toFloat(),
            0xFFFFFFFF.toInt(),
            0xFF000000.toInt(),
            1,
            circleSize / 2f
        )

        with(svSquare) {
            rx = squareX
            ry = ay
            rw = size
            rh = size
            bounds = Bounds(squareX, ay, size, size)
        }

        val hsX = squareX - sliderWidth - gap
        graphics.colorPickerRect(hsX, ay, sliderWidth, size, ColorPickerRenderState.TYPE_HUE_SLIDER)

        val hy = (ay + size * hue) + barYOffset
        graphics.roundedRect((hsX - barOverhang), hy, barWidth, barHeight, barFill, barOutline, 1, 1f)

        with(hueSlider) {
            ry = ay
            rh = size
            bounds = Bounds(hsX, ay, sliderWidth, size)
        }

        val asX = hsX - sliderWidth - gap
        graphics.colorPickerRect(
            asX, ay, sliderWidth, size, ColorPickerRenderState.TYPE_ALPHA_SLIDER, hue, saturation, brightness
        )

        val ahy = ay + size * (1f - alpha) + barYOffset
        graphics.roundedRect((asX - barOverhang), ahy, barWidth, barHeight, barFill, barOutline, 1, 1f)

        with(alphaSlider) {
            ry = ay
            rh = size
            bounds = Bounds(asX, ay, sliderWidth, size)
        }
    }

    override fun getVisibleChildren() = listOf<Widget>(svSquare, hueSlider, alphaSlider)

    override fun calculateHeight() = size - 4
}

class KeybindSettingWidget(setting: KeybindSetting) : SettingWidget<Int>(setting), Hoverable, Clickable, KeyCapturer {
    override var hovered = false
    override var hoverProgress = 0f
    private var recording = false

    override fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int) {
        val width = 40
        val height = 10
        val ax = x - width
        val ay = y - 2

        val outlineColor = lerpOutlineColor(hoverProgress)
        graphics.roundedRect(ax, ay, width, height, 0, outlineColor, OUTLINE_WIDTH, 3f)

        val text =
            if (recording) "..." else if (live.value == 0) "None" else InputConstants.Type.KEYSYM.getOrCreate(live.value).displayName.string
        val textX = ax + ((width - fontExtraSmall.width(text)) / 2)
        val textY = y + (FONT_SMALL_HEIGHT / 2) - (FONT_EXTRA_SMALL_HEIGHT / 2)
        val color = lerpColor(0xDDCCCCCC.toInt(), 0xDDFFFFFF.toInt(), hoverProgress)
        graphics.text(fontExtraSmall, text, textX, textY, color, false)

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

    override fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int) {}
}

class SliderWidget<T : Comparable<T>>(val setting: NumberSetting<T>) : Widget(), Draggable {
    override var dragging = false
    override var dragProgress = 0f

    private var displayValue: Double = (setting.value as Number).toDouble()
    private var railX = 0
    private var railWidth = 0
    private var headSize = FONT_SMALL_HEIGHT

    private val hoverable = HoverableWidget()

    fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int) {
        val railColor = OUTLINE_COLOR
        val headColor = lerpColor(0xFF999999.toInt(), 0xFFBBBBBB.toInt(), max(hoverable.hoverProgress, dragProgress))

        val ax = x - width
        val ay = y + FONT_SMALL_HEIGHT + 4

        graphics.rect(ax.toFloat(), ay + 2.5f, width.toFloat(), 1f, railColor)

        val size = FONT_SMALL_HEIGHT
        val headX = computeHeadX(displayValue, ax, width, size)

        graphics.roundedRect(headX, ay.toFloat(), size.toFloat(), size.toFloat(), headColor, radius = 3.5f)
        hoverable.bounds = Bounds(headX.toInt(), ay, size, size)

        railX = ax
        railWidth = width
        headSize = size
        bounds = Bounds(ax, ay, width, size)
    }

    override fun drag(x: Double, y: Double) {
        val min = (setting.allowed.start as Number).toDouble()
        val max = (setting.allowed.endInclusive as Number).toDouble()
        val step = (setting.step as Number).toDouble()

        val effectiveWidth = (railWidth - headSize).toDouble()
        if (effectiveWidth <= 0) return

        val t = ((x - railX - (headSize / 2)) / effectiveWidth).coerceIn(0.0, 1.0)
        val raw = min + (max - min) * t
        val snapped = round(raw / step) * step
        val clamped = snapped.coerceIn(min, max)

        @Suppress("UNCHECKED_CAST") run {
            setting.value = when (setting.value) {
                is Double -> clamped as T
                is Float -> clamped.toFloat() as T
                is Int -> clamped.toInt() as T
                else -> clamped as T
            }
        }
    }

    override fun tick(dt: Float) {
        val target = (setting.value as Number).toDouble()
        displayValue += (target - displayValue) * dt
    }

    private fun computeHeadX(value: Double, x: Int, width: Int, headSize: Int): Float {
        val min = (setting.allowed.start as Number).toDouble()
        val max = (setting.allowed.endInclusive as Number).toDouble()
        val t = ((value - min) / (max - min)).toFloat().coerceIn(0f, 1f)
        return x + ((width - headSize) * t)
    }

    override fun getVisibleChildren() = listOf(hoverable)
}

class NumberSettingWidget<T : Comparable<T>>(val setting: NumberSetting<T>) : SettingWidget<T>(setting), Hoverable {
    override var hovered: Boolean = false
    override var hoverProgress: Float = 0f
    private val slider = SliderWidget(setting)

    override fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int) {
        val boxWidth = 20
        val height = 10
        val ax = x - boxWidth
        val ay = y - 2

        val outlineColor = lerpOutlineColor(hoverProgress)
        graphics.roundedRect(ax, ay, boxWidth, height, 0, outlineColor, OUTLINE_WIDTH, 3f)
        bounds = Bounds(ax, ay, boxWidth, height)

        val text = if (live.value is Int) {
            live.value.toString()
        } else {
            val decimalPlaces = setting.step.toString().substringAfter('.', "").length
            "%.${decimalPlaces}f".format(live.value)
        }
        val textX = ax + ((boxWidth - fontExtraSmall.width(text)) / 2)
        val textY = y + (FONT_SMALL_HEIGHT / 2) - (FONT_EXTRA_SMALL_HEIGHT / 2)
        val color = lerpColor(0xDDCCCCCC.toInt(), 0xDDFFFFFF.toInt(), hoverProgress)
        graphics.text(fontExtraSmall, text, textX, textY, color, false)

        slider.render(graphics, x, y, width)
    }

    override fun getVisibleChildren() = listOf(slider)

    override fun calculateHeight() = FONT_SMALL_HEIGHT + 2 + FONT_SMALL_HEIGHT
}

class IntRangeSettingWidget(setting: IntRangeSetting) : SettingWidget<IntRange>(setting) {
    var oldMin = setting.value.first
    var oldMax = setting.value.last

    override fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int) {}
}

class BooleanSettingWidget(setting: BooleanSetting) : SettingWidget<Boolean>(setting), Hoverable, Clickable {
    override var hovered = false
    override var hoverProgress = 0f
    private var enableProgress = if (live.value) 1f else 0f

    companion object {
        private val checkmarkHandle = FriedSvg.loadSvg(Lifestolen.identifier("svg/checkmark.svg"))
        private var checkmarkTexture: Identifier? = null

        init {
            FriedSvg.getTextureAsync(checkmarkHandle, 160, 160).thenAccept { checkmarkTexture = it }
        }
    }

    override fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int) {
        val size = 10
        val ax = x - size
        val ay = y - 2
        val color = lerpOutlineColor(hoverProgress)

        graphics.roundedRect(ax, ay, size, size, 0, color, OUTLINE_WIDTH, 3f)

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
