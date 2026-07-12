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

        private val fontBig = Lifestolen.fontBig
        private val fontSmall = Lifestolen.fontSmall
        private val fontExtraSmall = Lifestolen.fontExtraSmall

        private val categories = Module.Category.entries.map { category ->
            CategoryState(
                category.toString(),
                Lifestolen.modules.filter { it.category == category }.map { mod ->
                    ModuleState(mod, mod.settings.map { setting ->
                        when (setting) {
                            is ColorSetting -> ColorSettingState(setting)
                            is KeybindSetting -> KeybindSettingState(setting)
                            is BlockListSetting -> BlockListSettingState(setting)
                            is DoubleSetting -> DoubleSettingState(setting)
                            is FloatSetting -> FloatSettingState(setting)
                            is IntSetting -> IntSettingState(setting)
                            is IntRangeSetting -> IntRangeSettingState(setting)
                            is BooleanSetting -> BooleanSettingState(setting)
                            else -> error("Unsupported setting type: ${setting.javaClass.name}")
                        } as SettingState<*>
                    })
                },
            )
        }

        private var currentlyConfiguring: ModuleState? = null
        private var debugMode = false

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

    private var currentlyHovered: Hoverable? = null
    private var currentlyRecording: KeybindSettingState? = null
    private var rainbowColorOffset = 0f

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        rainbowColorOffset += a
        val hue = (rainbowColorOffset % 60f) / 60f
        val rainbowColor = Color.HSBtoRGB(hue, 0.5f, 1f)
        val darkRainbowColor = Color.HSBtoRGB(hue, 0.5f, 0.75f)

        tickModules(a)

        val categoryGap = 8
        val marginTop = 8
        val namePadding = 2
        val moduleVPadding = 2
        val moduleHPadding = 4
        val moduleInsideHPadding = 4
        val moduleHeight = FONT_HEIGHT + (MODULE_INSIDE_V_PADDING * 2)
        val paddingHorizontal = categoryGap * 2
        val categoryWidth = (width - (paddingHorizontal * 2) - ((categories.size - 1) * categoryGap)) / categories.size
        val moduleWidth = categoryWidth - (moduleHPadding * 2)
        val lineY = marginTop + namePadding + FONT_BIG_HEIGHT + namePadding

        val guiScale = minecraft.window.guiScale.toFloat()
        val lineHeight = OUTLINE_WIDTH / guiScale * 1.5f
        val lineHeightCeil = ceil(lineHeight).toInt()
        val moduleStartY = lineY + lineHeightCeil + moduleVPadding

        categories.forEachIndexed { idx, category ->
            val categoryX = paddingHorizontal + (idx * categoryWidth) + (idx * categoryGap)
            val lastModuleBottom = moduleStartY + ((moduleVPadding + moduleHeight) * category.modules.size)
            val expandedHeight = category.modules.fold(0) { acc, state -> acc + state.computeExpandedHeight() }
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

            val nameX = categoryX + ((categoryWidth - fontBig.width(category.name)) / 2)
            graphics.text(fontBig, category.name, nameX, marginTop + namePadding, rainbowColor, false)
            graphics.rect(
                categoryX + (OUTLINE_WIDTH / guiScale),
                lineY.toFloat(),
                categoryWidth - (OUTLINE_WIDTH / guiScale * 2f),
                lineHeight,
                OUTLINE_COLOR
            )

            val moduleX = categoryX + moduleHPadding
            var moduleY = moduleStartY

            for (module in category.modules) {
                val height = moduleHeight + module.computeExpandedHeight()
                val outlineColor = lerpOutlineColor(module.hoverProgress)
                graphics.roundedRect(moduleX, moduleY, moduleWidth, height, 0, outlineColor, OUTLINE_WIDTH, 4f)
                module.clickableBounds = Bounds(moduleX, moduleY, moduleWidth, moduleHeight)
                module.bounds = Bounds(moduleX, moduleY, moduleWidth, height)

                val moduleNameX = moduleX + moduleInsideHPadding + ((moduleWidth - font.width(module.live.id)) / 2)
                val color = blendModuleColor(module, darkRainbowColor, rainbowColor)
                graphics.text(font, module.live.id, moduleNameX, moduleY + MODULE_INSIDE_V_PADDING, color, false)

                if (module.expandProgress > 0f) {
                    val settingX = moduleX + moduleInsideHPadding
                    val rightAlignedX = moduleX + moduleWidth - moduleInsideHPadding
                    var settingY = moduleY + MODULE_INSIDE_V_PADDING + FONT_HEIGHT + MODULE_INSIDE_V_PADDING

                    graphics.enableScissor(
                        settingX,
                        settingY - MODULE_INSIDE_V_PADDING,
                        settingX + moduleWidth,
                        settingY + module.computeExpandedHeight()
                    )

                    for (setting in module.settings) {
                        if (setting.live.id == "Enabled") continue

                        graphics.text(fontSmall, setting.live.name, settingX, settingY, -1, false)
                        setting.extractRenderState(graphics, rightAlignedX, settingY)

                        if (debugMode) {
                            val x = categoryX.toFloat()
                            val w = categoryWidth.toFloat()
                            val h = 1f / guiScale
                            graphics.rect(x, settingY.toFloat(), w, h, 0xFFFF0000.toInt())
                            graphics.rect(x, (settingY + (FONT_SMALL_HEIGHT / 2)).toFloat(), w, h, 0xFF00FF00.toInt())
                            graphics.rect(x, (settingY + FONT_SMALL_HEIGHT).toFloat(), w, h, 0xFFFF0000.toInt())
                        }

                        settingY += SETTING_GAP + setting.calculateHeight()
                    }

                    graphics.disableScissor()
                }

                moduleY += moduleVPadding + moduleHeight + module.computeExpandedHeight()
            }
        }

        if (debugMode) {
            val text = "Debug mode is active. Press F1 to deactivate."
            graphics.centeredText(font, text, width / 2, height - FONT_HEIGHT - 8, rainbowColor)
        }
    }

    private fun blendModuleColor(module: ModuleState, darkRainbowColor: Int, rainbowColor: Int): Int {
        val baseColor = lerpColor(0xBBCCCCCC.toInt(), darkRainbowColor, module.enabledProgress)
        val hoverColor = lerpColor(0xBBFFFFFF.toInt(), rainbowColor, module.enabledProgress)
        return lerpColor(baseColor, hoverColor, module.hoverProgress)
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        currentlyRecording?.let {
            it.captureKey(if (event.isEscape) 0 else event.key)
            currentlyRecording = null
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
        currentlyHovered?.hovered = false

        getAllModules().find { it.bounds.isInBounds(x, y) }?.let {
            it.hovered = true
            currentlyHovered = it
        }

        getAllModules().flatMap { it.settings }.forEach {
            if (it is Hoverable && it.bounds.isInBounds(x, y)) {
                it.hovered = true
                currentlyHovered = it
            }
        }
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        if (currentlyRecording != null) return true

        val module = getAllModules().find { it.clickableBounds.isInBounds(event) }

        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            module?.let { module ->
                if (currentlyConfiguring == module) {
                    module.expanded = !module.expanded
                } else {
                    currentlyConfiguring?.let { it.expanded = false }
                    module.expanded = true
                    currentlyConfiguring = module
                }
                return true
            }
        } else if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            currentlyConfiguring?.takeIf { it.expanded }?.let { module ->
                module.settings.forEach {
                    if (it is Clickable && it.bounds.isInBounds(event)) {
                        if (it is KeybindSettingState) currentlyRecording = it
                        it.click()
                        return true
                    }
                }
            }

            module?.let {
                it.live.toggle()
                return true
            }
        }
        return true
    }

    override fun onClose() {
        getAllModules().forEach {
            it.hoverProgress = 0f
            it.hovered = false
        }
        super.onClose()
    }

    private fun tickModules(a: Float) {
        val dt = a * 0.5f
        getAllModules().forEach { mod ->
            mod.hoverProgress = if (mod.hovered) (mod.hoverProgress + dt).coerceAtMost(1f)
            else (mod.hoverProgress - dt).coerceAtLeast(0f)

            mod.enabledProgress = if (mod.live.enabled) (mod.enabledProgress + dt).coerceAtMost(1f)
            else (mod.enabledProgress - dt).coerceAtLeast(0f)

            mod.expandProgress = if (mod.expanded) (mod.expandProgress + dt).coerceAtMost(1f)
            else (mod.expandProgress - dt).coerceAtLeast(0f)

            mod.settings.forEach {
                it.tick(dt)
                if (it is Hoverable) {
                    it.hoverProgress = if (it.hovered) (it.hoverProgress + dt).coerceAtMost(1f)
                    else (it.hoverProgress - dt).coerceAtLeast(0f)
                }
            }
        }
    }

    private fun getAllModules() = categories.flatMap { it.modules }

    override fun extractTransparentBackground(graphics: GuiGraphicsExtractor) = graphics.blurBeforeThisStratum()
    override fun isInGameUi() = true
    override fun isPauseScreen() = false

    class CategoryState(val name: String, val modules: List<ModuleState>)
    class ModuleState(
        val live: Module,
        val settings: List<SettingState<*>>,
        var clickableBounds: Bounds = Bounds(),
        override var bounds: Bounds = Bounds(),
        override var hovered: Boolean = false,
        override var hoverProgress: Float = 0f,
        var expanded: Boolean = false,
        var expandProgress: Float = 0f,
        var enabledProgress: Float = if (live.enabled) 1f else 0f,
    ) : Hoverable {
        fun computeExpandedHeight(): Int {
            if (expandProgress == 0f) return 0
            val baseHeight = settings.fold(0) { acc, state ->
                if (state.live.id == "Enabled") return@fold acc
                acc + state.calculateHeight()
            }
            val paddedHeight = baseHeight + MODULE_INSIDE_V_PADDING + (SETTING_GAP * (settings.size - 2))
            return (paddedHeight * expandProgress).toInt()
        }
    }

    abstract class SettingState<T>(val live: Setting<T>) {
        abstract fun extractRenderState(graphics: GuiGraphicsExtractor, x: Int, y: Int)
        open fun calculateHeight() = FONT_SMALL_HEIGHT
        open fun tick(dt: Float) {}
    }

    interface Hoverable {
        var hovered: Boolean
        var hoverProgress: Float
        val bounds: Bounds
    }

    interface Clickable {
        val bounds: Bounds
        fun click()
    }

    class ColorSettingState(
        setting: ColorSetting,
        v: Int = setting.value,
        arr: FloatArray = Color.RGBtoHSB(v and 0xFF, (v shr 2) and 0xFF, (v shr 4) and 0xFF, null),
        var targetHue: Float = arr[0],
        var targetSaturation: Float = arr[1],
        var targetBrightness: Float = arr[2],
        var targetAlpha: Int = (v shr 6) and 0xFF,
        var oldHue: Float = targetHue,
        var oldSaturation: Float = targetSaturation,
        var oldBrightness: Float = targetBrightness,
        var oldAlpha: Int = targetAlpha,
    ) : SettingState<Int>(setting) {
        override fun extractRenderState(graphics: GuiGraphicsExtractor, x: Int, y: Int) {}
    }

    class KeybindSettingState(
        setting: KeybindSetting,
        var recording: Boolean = false,
        override var bounds: Bounds = Bounds(),
        override var hovered: Boolean = false,
        override var hoverProgress: Float = 0f,
    ) : SettingState<Int>(setting), Hoverable, Clickable {
        override fun extractRenderState(graphics: GuiGraphicsExtractor, x: Int, y: Int) {
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

        override fun click() {
            recording = true
        }

        fun captureKey(key: Int) {
            live.value = key
            recording = false
        }
    }

    class BlockListSettingState(setting: BlockListSetting, var hoveredIdx: Int = 0, var hoverProgress: Float = 0f) :
        SettingState<MutableList<Block>>(setting) {
        override fun extractRenderState(graphics: GuiGraphicsExtractor, x: Int, y: Int) {}
    }

    class DoubleSettingState(setting: DoubleSetting, var target: Number = setting.value, var old: Number = target) :
        SettingState<Double>(setting) {
        override fun extractRenderState(graphics: GuiGraphicsExtractor, x: Int, y: Int) {}
    }

    class FloatSettingState(setting: FloatSetting, var target: Number = setting.value, var old: Number = target) :
        SettingState<Float>(setting) {
        override fun extractRenderState(graphics: GuiGraphicsExtractor, x: Int, y: Int) {}
    }

    class IntSettingState(val setting: IntSetting, var target: Number = setting.value, var old: Number = target) :
        SettingState<Int>(setting) {
        override fun extractRenderState(graphics: GuiGraphicsExtractor, x: Int, y: Int) {}
    }

    class IntRangeSettingState(
        setting: IntRangeSetting,
        var targetMin: Number = setting.value.first,
        var targetMax: Number = setting.value.last,
        var oldMin: Number = targetMin,
        var oldMax: Number = targetMax,
    ) : SettingState<IntRange>(setting) {
        override fun extractRenderState(graphics: GuiGraphicsExtractor, x: Int, y: Int) {}
    }

    class BooleanSettingState(
        setting: BooleanSetting,
        var enableProgress: Float = 0f,
        override var bounds: Bounds = Bounds(),
        override var hovered: Boolean = false,
        override var hoverProgress: Float = 0f,
    ) : SettingState<Boolean>(setting), Hoverable, Clickable {
        companion object {
            private val checkmarkHandle = FriedSvg.loadSvg(Lifestolen.identifier("svg/checkmark.svg"))
            private var checkmarkTexture: Identifier? = null

            init {
                FriedSvg.getTextureAsync(checkmarkHandle, 160, 160).thenAccept { checkmarkTexture = it }
            }
        }

        override fun extractRenderState(graphics: GuiGraphicsExtractor, x: Int, y: Int) {
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

        override fun click() {
            live.value = !live.value
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
