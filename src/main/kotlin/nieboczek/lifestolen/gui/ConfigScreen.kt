package nieboczek.lifestolen.gui

import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.Block
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.config.setting.*
import nieboczek.lifestolen.friedsvg.FriedSvg
import nieboczek.lifestolen.friedsvg.blitPixel
import nieboczek.lifestolen.module.Module
import org.lwjgl.glfw.GLFW
import java.awt.Color
import java.util.*
import kotlin.math.ceil

class ConfigScreen : Screen(Minecraft.getInstance(), Lifestolen.font, Component.literal(Lifestolen.CLIENT_NAME)) {
    companion object {
        private const val OUTLINE_COLOR = 0x92888888.toInt()
        private const val HOVERED_OUTLINE_COLOR = 0x92BBBBBB.toInt()
        private const val OUTLINE_WIDTH = 2
        private const val FONT_BIG_HEIGHT = 12
        private const val FONT_HEIGHT = 8
        private const val FONT_SMALL_HEIGHT = 6
        private const val FONT_EXTRA_SMALL_HEIGHT = 4
        private const val SETTING_GAP = 6
        private const val MODULE_INSIDE_V_PADDING = 4

        private val fontN = Lifestolen.font
        private val fontBig = Lifestolen.fontBig
        private val fontSmall = Lifestolen.fontSmall
        private val fontExtraSmall = Lifestolen.fontExtraSmall

        private val rootWidget = RootWidget()

        private var currentlyConfiguring: ModuleWidget? = null
        private var debugMode = false

        private var guiScale = 1f
        private var rainbowColor = 0
        private var darkRainbowColor = 0

        private fun lerpColor(start: Int, target: Int, progress: Float): Int {
            val startA = (start shr 24) and 0xFF
            val startR = (start shr 16) and 0xFF
            val startG = (start shr 8) and 0xFF
            val startB = start and 0xFF
            val targetA = (target shr 24) and 0xFF
            val targetR = (target shr 16) and 0xFF
            val targetG = (target shr 8) and 0xFF
            val targetB = target and 0xFF
            val a = (startA + ((targetA - startA) * progress).toInt()) shl 24
            val r = (startR + ((targetR - startR) * progress).toInt()) shl 16
            val g = (startG + ((targetG - startG) * progress).toInt()) shl 8
            val b = startB + ((targetB - startB) * progress).toInt()
            return a or r or g or b
        }

        private fun lerpOutlineColor(progress: Float): Int = lerpColor(OUTLINE_COLOR, HOVERED_OUTLINE_COLOR, progress)
    }

    private var currentlyHovered = ArrayList<Hoverable>(4)
    private var currentlyCapturing: KeyCapturer? = null
    private var rainbowColorOffset = 0f

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        guiScale = minecraft.window.guiScale.toFloat()

        rainbowColorOffset += a
        val hue = (rainbowColorOffset % 60f) / 60f
        rainbowColor = Color.HSBtoRGB(hue, 0.5f, 1f)
        darkRainbowColor = Color.HSBtoRGB(hue, 0.5f, 0.75f)

        val dt = a * 0.5f
        walkWidgets {
            it.tick(dt)
            if (it is Hoverable) {
                it.hoverProgress = if (it.hovered) (it.hoverProgress + dt).coerceAtMost(1f)
                else (it.hoverProgress - dt).coerceAtLeast(0f)
            }
            return@walkWidgets false
        }

        rootWidget.render(graphics, width)

        if (debugMode) {
            val text = "Debug mode is active. Press F1 to deactivate."
            graphics.centeredText(font, text, width / 2, height - FONT_HEIGHT - 8, rainbowColor)
        }
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        currentlyCapturing?.let {
            val action = it.captureKey(if (event.isEscape) 0 else event.key)
            if (action == KeyCapturer.Action.STOP_CAPTURING) currentlyCapturing = null
            return true
        }

        if (event.key == GLFW.GLFW_KEY_F1) {
            debugMode = !debugMode
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
            return@walkWidgets false
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

    abstract class Widget {
        var bounds: Bounds = Bounds()

        open fun getVisibleChildren() = listOf<Widget>()
        open fun tick(dt: Float) {}
    }

    interface Hoverable {
        var hovered: Boolean
        var hoverProgress: Float
    }

    interface Clickable {
        fun click(button: Int): Action

        enum class Action {
            NONE, CAPTURE_KEY;
        }
    }

    interface KeyCapturer {
        fun captureKey(key: Int): Action

        enum class Action {
            NONE, STOP_CAPTURING;
        }
    }

    /** Use when you need to handle clicks with separate bounds */
    class ClickableWidget(
        private val onClick: (Int) -> Clickable.Action
    ) : Widget(), Clickable {
        override fun click(button: Int) = onClick(button)
    }

    class RootWidget : Widget() {
        val categories = Module.Category.entries.map { category ->
            CategoryWidget(
                category.toString(),
                Lifestolen.modules.filter { it.category == category }.map { mod ->
                    ModuleWidget(mod, mod.settings.map { setting ->
                        when (setting) {
                            is ColorSetting -> ColorSettingWidget(setting)
                            is KeybindSetting -> KeybindSettingWidget(setting)
                            is BlockListSetting -> BlockListSettingWidget(setting)
                            is DoubleSetting -> DoubleSettingWidget(setting)
                            is FloatSetting -> FloatSettingWidget(setting)
                            is IntSetting -> IntSettingWidget(setting)
                            is IntRangeSetting -> IntRangeSettingWidget(setting)
                            is BooleanSetting -> BooleanSettingWidget(setting)
                            else -> error("Unsupported setting type: ${setting.javaClass.name}")
                        } as SettingWidget<*>
                    })
                },
            )
        }

        fun render(graphics: GuiGraphicsExtractor, screenWidth: Int) {
            categories.forEachIndexed { idx, category ->
                category.render(graphics, idx, categories.size, screenWidth)
            }
        }

        override fun getVisibleChildren() = categories
    }

    class CategoryWidget(val name: String, val modules: List<ModuleWidget>) : Widget() {
        fun render(graphics: GuiGraphicsExtractor, idx: Int, categoriesSize: Int, screenWidth: Int) {
            val categoryGap = 8
            val marginTop = 8
            val namePadding = 2
            val moduleVPadding = 2
            val moduleHPadding = 4
            val moduleHeight = FONT_HEIGHT + (MODULE_INSIDE_V_PADDING * 2)
            val paddingHorizontal = categoryGap * 2
            val categoryWidth =
                (screenWidth - (paddingHorizontal * 2) - ((categoriesSize - 1) * categoryGap)) / categoriesSize
            val moduleWidth = categoryWidth - (moduleHPadding * 2)
            val lineY = marginTop + namePadding + FONT_BIG_HEIGHT + namePadding

            val lineHeight = OUTLINE_WIDTH / guiScale * 1.5f
            val lineHeightCeil = ceil(lineHeight).toInt()
            val moduleStartY = lineY + lineHeightCeil + moduleVPadding

            val categoryX = paddingHorizontal + (idx * categoryWidth) + (idx * categoryGap)
            val lastModuleBottom = moduleStartY + ((moduleVPadding + moduleHeight) * modules.size)
            val expandedHeight = modules.fold(0) { acc, state -> acc + state.computeExpandedHeight() }
            val neededHeight = lastModuleBottom - marginTop + moduleVPadding + expandedHeight

            graphics.blurredRoundedRect(
                categoryX,
                marginTop,
                categoryWidth,
                neededHeight,
                0x92000000.toInt(),
                OUTLINE_COLOR,
                OUTLINE_WIDTH,
                8f,
                16f,
            )

            val nameX = categoryX + ((categoryWidth - fontBig.width(name)) / 2)
            graphics.text(fontBig, name, nameX, marginTop + namePadding, rainbowColor, false)
            graphics.rect(
                categoryX + (OUTLINE_WIDTH / guiScale),
                lineY.toFloat(),
                categoryWidth - (OUTLINE_WIDTH / guiScale * 2f),
                lineHeight,
                OUTLINE_COLOR
            )

            val moduleX = categoryX + moduleHPadding
            var moduleY = moduleStartY

            for (module in modules) {
                module.render(graphics, moduleX, moduleY, moduleWidth, moduleHeight)
                moduleY += moduleVPadding + moduleHeight + module.computeExpandedHeight()
            }
        }

        override fun getVisibleChildren() = modules
    }

    class ModuleWidget(val live: Module, val settings: List<SettingWidget<*>>) : Widget(), Hoverable {
        val clickHandler = ClickableWidget { button ->
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                if (currentlyConfiguring == this) {
                    expanded = !expanded
                } else {
                    currentlyConfiguring?.let { it.expanded = false }
                    expanded = true
                    currentlyConfiguring = this
                }
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                live.toggle()
            }
            Clickable.Action.NONE
        }

        override var hovered = false
        override var hoverProgress = 0f
        var expanded = false
        var expandProgress = 0f
        var enabledProgress = if (live.enabled) 1f else 0f

        fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int, moduleWidth: Int, moduleHeight: Int) {
            val moduleInsideHPadding = 4

            val height = moduleHeight + computeExpandedHeight()
            val outlineColor = lerpOutlineColor(hoverProgress)
            graphics.roundedRect(x, y, moduleWidth, height, 0, outlineColor, OUTLINE_WIDTH, 4f)
            clickHandler.bounds = Bounds(x, y, moduleWidth, moduleHeight)
            bounds = Bounds(x, y, moduleWidth, height)

            val moduleNameX = x + moduleInsideHPadding + ((moduleWidth - fontN.width(live.id)) / 2)
            val color = blendModuleColor(this, darkRainbowColor, rainbowColor)
            graphics.text(fontN, live.id, moduleNameX, y + MODULE_INSIDE_V_PADDING, color, false)

            if (expandProgress > 0f) {
                val settingX = x + moduleInsideHPadding
                val rightAlignedX = x + moduleWidth - moduleInsideHPadding
                var settingY = y + MODULE_INSIDE_V_PADDING + FONT_HEIGHT + MODULE_INSIDE_V_PADDING

                graphics.enableScissor(
                    settingX,
                    settingY - MODULE_INSIDE_V_PADDING,
                    settingX + moduleWidth,
                    settingY + computeExpandedHeight()
                )

                for (setting in settings) {
                    if (setting.live.id == "Enabled") continue

                    graphics.text(fontSmall, setting.live.name, settingX, settingY, -1, false)
                    setting.render(graphics, rightAlignedX, settingY)

                    if (debugMode) {
                        val x = x.toFloat()
                        val w = moduleWidth.toFloat()
                        val h = 1f / guiScale
                        graphics.rect(x, settingY.toFloat(), w, h, 0xFFFF0000.toInt())
                        graphics.rect(x, (settingY + (FONT_SMALL_HEIGHT / 2)).toFloat(), w, h, 0xFF00FF00.toInt())
                        graphics.rect(x, (settingY + FONT_SMALL_HEIGHT).toFloat(), w, h, 0xFFFF0000.toInt())
                    }

                    settingY += SETTING_GAP + setting.calculateHeight()
                }

                graphics.disableScissor()
            }
        }

        override fun tick(dt: Float) {
            enabledProgress = if (live.enabled) (enabledProgress + dt).coerceAtMost(1f)
            else (enabledProgress - dt).coerceAtLeast(0f)

            expandProgress = if (expanded) (expandProgress + dt).coerceAtMost(1f)
            else (expandProgress - dt).coerceAtLeast(0f)
        }

        override fun getVisibleChildren(): List<Widget> {
            if (expandProgress == 0f) return listOf(clickHandler)
            if (expandProgress == 1f) return listOf(clickHandler) + settings

            var availableHeight = computeExpandedHeight() - MODULE_INSIDE_V_PADDING
            if (availableHeight <= 0) return listOf(clickHandler)

            val children = mutableListOf<Widget>(clickHandler)
            for (setting in settings) {
                availableHeight -= setting.calculateHeight()
                if (availableHeight < 0) return children

                children.add(setting)
                availableHeight -= SETTING_GAP
            }
            return children
        }

        fun computeExpandedHeight(): Int {
            if (expandProgress == 0f) return 0
            val baseHeight = settings.fold(0) { acc, state ->
                if (state.live.id == "Enabled") return@fold acc
                acc + state.calculateHeight()
            }
            val paddedHeight = baseHeight + MODULE_INSIDE_V_PADDING + (SETTING_GAP * (settings.size - 2))
            return (paddedHeight * expandProgress).toInt()
        }

        private fun blendModuleColor(module: ModuleWidget, darkRainbowColor: Int, rainbowColor: Int): Int {
            val baseColor = lerpColor(0xBBCCCCCC.toInt(), darkRainbowColor, module.enabledProgress)
            val hoverColor = lerpColor(0xBBFFFFFF.toInt(), rainbowColor, module.enabledProgress)
            return lerpColor(baseColor, hoverColor, module.hoverProgress)
        }
    }

    abstract class SettingWidget<T>(val live: Setting<T>) : Widget() {
        abstract fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int)
        open fun calculateHeight() = FONT_SMALL_HEIGHT
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

    class KeybindSettingWidget(setting: KeybindSetting) : SettingWidget<Int>(setting), Hoverable, Clickable,
        KeyCapturer {
        var recording = false
        override var hovered = false
        override var hoverProgress = 0f

        override fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int) {
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

        override fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int) {}
    }

    class DoubleSettingWidget(val setting: DoubleSetting) : SettingWidget<Double>(setting) {
        var old = setting.value

        override fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int) {}
    }

    class FloatSettingWidget(val setting: FloatSetting) : SettingWidget<Float>(setting) {
        var old = setting.value

        override fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int) {}
    }

    class IntSettingWidget(val setting: IntSetting) : SettingWidget<Int>(setting) {
        var old = setting.value

        override fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int) {}
    }

    class IntRangeSettingWidget(val setting: IntRangeSetting) : SettingWidget<IntRange>(setting) {
        var oldMin = setting.value.first
        var oldMax = setting.value.last

        override fun render(graphics: GuiGraphicsExtractor, x: Int, y: Int) {}
    }

    class BooleanSettingWidget(setting: BooleanSetting) : SettingWidget<Boolean>(setting), Hoverable, Clickable {
        var enableProgress = 0f
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

    class Bounds(val x: Int, val y: Int, width: Int, height: Int) {
        val x2: Int = x + width
        val y2: Int = y + height

        constructor() : this(0, 0, 0, 0)

        fun isInBounds(event: MouseButtonEvent) = isInBounds(event.x, event.y)
        fun isInBounds(cx: Double, cy: Double) = cx >= x && cy >= y && cx <= x2 && cy <= y2
    }
}
