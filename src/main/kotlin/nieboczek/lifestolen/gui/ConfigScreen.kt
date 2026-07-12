package nieboczek.lifestolen.gui

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.world.level.block.Block
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.config.setting.*
import nieboczek.lifestolen.module.Module
import org.lwjgl.glfw.GLFW
import java.awt.Color
import kotlin.math.ceil

class ConfigScreen : Screen(Minecraft.getInstance(), Lifestolen.font, Component.literal(Lifestolen.CLIENT_NAME)) {
    companion object {
        private const val OUTLINE_COLOR = 0x92888888.toInt()
        private const val OUTLINE_WIDTH = 2
        private const val FONT_BIG_HEIGHT = 12
        private const val FONT_HEIGHT = 8
        private const val FONT_SMALL_HEIGHT = 6
        private const val SETTING_GAP = 6
        private const val MODULE_INSIDE_V_PADDING = 4

        private val categories = Module.Category.entries.map { category ->
            CategoryData(
                category.toString(),
                Lifestolen.modules.filter { it.category == category }.map { mod ->
                    ModuleData(mod, mod.settings.map { setting ->
                        when (setting) {
                            is ColorSetting -> ColorSettingData(setting)
                            is KeybindSetting -> KeybindSettingData(setting)
                            is BlockListSetting -> BlockListSettingData(setting)
                            is DoubleSetting -> DoubleSettingData(setting)
                            is FloatSetting -> FloatSettingData(setting)
                            is IntSetting -> IntSettingData(setting)
                            is IntRangeSetting -> IntRangeSettingData(setting)
                            is BooleanSetting -> BooleanSettingData(setting)
                            else -> error("Unsupported setting type: ${setting.javaClass.name}")
                        } as SettingData<*>
                    })
                },
            )
        }

        private var currentlyConfiguring: ModuleData? = null
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
    }

    private val fontBig = Lifestolen.fontBig
    private val fontSmall = Lifestolen.fontSmall
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
            val expandedHeight = category.modules.fold(0) { acc, data -> acc + data.computeExpandedHeight() }
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
                graphics.roundedRect(moduleX, moduleY, moduleWidth, height, 0, OUTLINE_COLOR, OUTLINE_WIDTH, 4f)
                module.bounds = Bounds(moduleX, moduleY, moduleWidth, moduleHeight)

                val moduleNameX = moduleX + moduleInsideHPadding + ((moduleWidth - font.width(module.live.id)) / 2)
                val color = blendModuleColor(module, darkRainbowColor, rainbowColor)
                graphics.text(font, module.live.id, moduleNameX, moduleY + MODULE_INSIDE_V_PADDING, color, false)

                if (module.expandProgress > 0f) {
                    val settingX = moduleX + moduleInsideHPadding
                    val rightAlignedX = moduleX + moduleWidth - moduleInsideHPadding
                    var settingY = moduleY + MODULE_INSIDE_V_PADDING + FONT_HEIGHT + MODULE_INSIDE_V_PADDING

                    graphics.enableScissor(
                        settingX,
                        settingY - 1,
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

    private fun blendModuleColor(module: ModuleData, darkRainbowColor: Int, rainbowColor: Int): Int {
        val baseColor = lerpColor(0xBBCCCCCC.toInt(), darkRainbowColor, module.enabledProgress)
        val hoverColor = lerpColor(0xBBFFFFFF.toInt(), rainbowColor, module.enabledProgress)
        return lerpColor(baseColor, hoverColor, module.hoverProgress)
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (event.key == GLFW.GLFW_KEY_F1) {
            debugMode = !debugMode
            return true
        }

        val guiKey = KeyMappingHelper.getBoundKeyOf(minecraft.options.keySocialInteractions).value
        if (event.key == guiKey || event.isEscape) onClose()
        return true
    }

    override fun mouseMoved(x: Double, y: Double) {
        val module = getAllModules().filter {
            it.hovered = false
            it.bounds.inBounds(x, y)
        }.getOrNull(0) ?: return

        module.hovered = true
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val module = getAllModules().find { it.bounds.inBounds(event) }

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
                val setting = module.settings.find {
                    when (it) {
                        is BooleanSettingData -> it.bounds.inBounds(event)
                        else -> false
                    }
                }
                setting?.let {
                    it.click()
                    return true
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

            mod.settings.forEach { it.tick(dt) }
        }
    }

    private fun getAllModules() = categories.flatMap { it.modules }

    override fun extractTransparentBackground(graphics: GuiGraphicsExtractor) = graphics.blurBeforeThisStratum()
    override fun isInGameUi() = true
    override fun isPauseScreen() = false

    class CategoryData(val name: String, val modules: List<ModuleData>)
    class ModuleData(
        val live: Module,
        val settings: List<SettingData<*>>,
        var bounds: Bounds = Bounds(),
        var hovered: Boolean = false,
        var hoverProgress: Float = 0f,
        var expanded: Boolean = false,
        var expandProgress: Float = 0f,
        var enabledProgress: Float = if (live.enabled) 1f else 0f,
    ) {
        fun computeExpandedHeight(): Int {
            if (expandProgress == 0f) return 0
            val baseHeight = settings.fold(0) { acc, data ->
                if (data.live.id == "Enabled") return@fold acc
                acc + data.calculateHeight()
            }
            val paddedHeight = baseHeight + MODULE_INSIDE_V_PADDING + (SETTING_GAP * (settings.size - 2))
            return (paddedHeight * expandProgress).toInt()
        }
    }

    abstract class SettingData<T>(val live: Setting<T>) {
        abstract fun extractRenderState(graphics: GuiGraphicsExtractor, x: Int, y: Int)
        open fun calculateHeight() = FONT_SMALL_HEIGHT
        open fun click() {}
        open fun tick(dt: Float) {}
    }

    class ColorSettingData(
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
    ) : SettingData<Int>(setting) {
        override fun extractRenderState(graphics: GuiGraphicsExtractor, x: Int, y: Int) {}
    }

    class KeybindSettingData(setting: KeybindSetting, var recording: Boolean = false) : SettingData<Int>(setting) {
        override fun extractRenderState(graphics: GuiGraphicsExtractor, x: Int, y: Int) {}
    }

    class BlockListSettingData(setting: BlockListSetting, var hoveredIdx: Int = 0, var hoverProgress: Float = 0f) :
        SettingData<MutableList<Block>>(setting) {
        override fun extractRenderState(graphics: GuiGraphicsExtractor, x: Int, y: Int) {}
    }

    class DoubleSettingData(setting: DoubleSetting, var target: Number = setting.value, var old: Number = target) :
        SettingData<Double>(setting) {
        override fun extractRenderState(graphics: GuiGraphicsExtractor, x: Int, y: Int) {}
    }

    class FloatSettingData(setting: FloatSetting, var target: Number = setting.value, var old: Number = target) :
        SettingData<Float>(setting) {
        override fun extractRenderState(graphics: GuiGraphicsExtractor, x: Int, y: Int) {}
    }

    class IntSettingData(val setting: IntSetting, var target: Number = setting.value, var old: Number = target) :
        SettingData<Int>(setting) {
        override fun extractRenderState(graphics: GuiGraphicsExtractor, x: Int, y: Int) {}
    }

    class IntRangeSettingData(
        setting: IntRangeSetting,
        var targetMin: Number = setting.value.first,
        var targetMax: Number = setting.value.last,
        var oldMin: Number = targetMin,
        var oldMax: Number = targetMax,
    ) : SettingData<IntRange>(setting) {
        override fun extractRenderState(graphics: GuiGraphicsExtractor, x: Int, y: Int) {}
    }

    class BooleanSettingData(setting: BooleanSetting, var progress: Float = 0f, var bounds: Bounds = Bounds()) :
        SettingData<Boolean>(setting) {
        override fun extractRenderState(graphics: GuiGraphicsExtractor, x: Int, y: Int) {
            val size = 10
            // TODO: replace with checkmark symbol later
            val f = lerpColor(0, -1, progress)
            val ax = x - size
            val ay = y - ((size - FONT_SMALL_HEIGHT) / 2)
            graphics.roundedRect(ax, ay, size, size, f, OUTLINE_COLOR, OUTLINE_WIDTH, 3f)
            bounds = Bounds(ax, ay, size, size)
        }

        override fun click() {
            live.value = !live.value
        }

        override fun tick(dt: Float) {
            progress = if (live.value) (progress + dt).coerceAtMost(1f) else (progress - dt).coerceAtLeast(0f)
        }
    }

    class Bounds(val x: Int, val y: Int, width: Int, height: Int) {
        val x2: Int = x + width
        val y2: Int = y + height

        constructor() : this(0, 0, 0, 0)

        fun inBounds(event: MouseButtonEvent) = inBounds(event.x, event.y)
        fun inBounds(cx: Double, cy: Double) = cx >= x && cy >= y && cx <= x2 && cy <= y2
    }
}
