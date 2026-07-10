package nieboczek.lifestolen.gui

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.config.setting.*
import nieboczek.lifestolen.module.Module
import java.awt.Color

class ConfigScreen : Screen(Minecraft.getInstance(), Lifestolen.font, Component.literal(Lifestolen.CLIENT_NAME)) {
    companion object {
        private val categories = Module.Category.entries.map { category ->
            CategoryData(
                category.toString(),
                Lifestolen.modules.filter { it.category == category }.map { ModuleData(it) },
            )
        }
    }

    private val fontBig = Lifestolen.fontBig
    private val fontSmall = Lifestolen.fontSmall

    private var rainbowColorOffset = 0
    private var currentlyConfiguring: Module? = null

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        rainbowColorOffset += 1
        val hue = (rainbowColorOffset % 360) / 360f
        val rainbowColor = Color.HSBtoRGB(hue, 0.5f, 1f)
        val darkRainbowColor = Color.HSBtoRGB(hue, 0.5f, 0.75f)

        val dt = a * 0.5f
        getAllModules().forEach {
            if (it.hovered) it.hoverProgress = (it.hoverProgress + dt).coerceAtMost(1f)
            else it.hoverProgress = (it.hoverProgress - dt).coerceAtLeast(0f)

            if (it.live.enabled) it.enabledProgress = (it.enabledProgress + dt).coerceAtMost(1f)
            else it.enabledProgress = (it.enabledProgress - dt).coerceAtLeast(0f)
        }

        val outlineColor = 0x77888888

        val fontBigHeight = 12
        val fontHeight = 8
        val fontSmallHeight = 7
        val outlineWidth = 2
        val categoryGap = 8
        val marginTop = 8
        val namePadding = 2
        val moduleVPadding = 2
        val moduleHPadding = 4
        val moduleNameVPadding = 4
        val moduleNameHPadding = 4
        val moduleHeight = fontHeight + (moduleNameVPadding * 2)
        val paddingHorizontal = categoryGap * 2
        val categoryWidth = (width - (paddingHorizontal * 2) - ((categories.size - 1) * categoryGap)) / categories.size
        val moduleWidth = categoryWidth - (moduleHPadding * 2)
        val lineY = marginTop + namePadding + fontBigHeight + namePadding

        val guiScale = minecraft.window.guiScale.toFloat()
        val lineHeight = outlineWidth / guiScale * 1.5f
        val lineHeightCeil = kotlin.math.ceil(lineHeight).toInt()
        val moduleStartY = lineY + lineHeightCeil + moduleVPadding

        categories.forEachIndexed { idx, category ->
            val categoryX = paddingHorizontal + (idx * categoryWidth) + (idx * categoryGap)
            val lastModuleBottom = moduleStartY + ((moduleVPadding + moduleHeight) * category.modules.size)
            val neededHeight = lastModuleBottom - marginTop + moduleVPadding

            graphics.blurredRoundedRect(
                categoryX,
                marginTop,
                categoryWidth,
                neededHeight,
                0x77000000,
                outlineColor,
                outlineWidth,
                8f,
                16f,
            )

            val nameX = categoryX + ((categoryWidth - fontBig.width(category.name)) / 2)
            graphics.text(fontBig, category.name, nameX, marginTop + namePadding, rainbowColor, false)
            graphics.rect(
                categoryX + (outlineWidth / guiScale),
                lineY.toFloat(),
                categoryWidth - (outlineWidth / guiScale * 2f),
                lineHeight,
                outlineColor
            )

            val moduleX = categoryX + moduleHPadding
            category.modules.forEachIndexed { idx, module ->
                val moduleY = moduleStartY + ((moduleVPadding + moduleHeight) * idx)
                graphics.roundedRect(moduleX, moduleY, moduleWidth, moduleHeight, 0, outlineColor, outlineWidth, 4f)

                module.bounds = Bounds(moduleX, moduleY, moduleWidth, moduleHeight)

                val moduleNameX = moduleX + moduleNameHPadding + ((moduleWidth - font.width(module.live.id)) / 2)
                val color = blendModuleColor(module, darkRainbowColor, rainbowColor)
                graphics.text(font, module.live.id, moduleNameX, moduleY + moduleNameVPadding, color, false)
            }
        }

        // ### MODULE SETTINGS ################################################
        val module = currentlyConfiguring ?: return

        val windowX = width / 4
        val windowY = height / 4
        val windowWidth = width / 2
        val windowHeight = height / 2

        graphics.fill(0, 0, width, height, 0x33000000)
        graphics.blurredRoundedRect(
            windowX,
            windowY,
            windowWidth,
            windowHeight,
            0xDD000000.toInt(),
            outlineColor,
            outlineWidth,
            8f,
            16f
        )

        val settingHPadding = 4
        val windowNameVPadding = 4
        val nameX = windowX + settingHPadding
        val nameY = windowY + windowNameVPadding
        val windowLineY = nameY + fontBigHeight + windowNameVPadding

        graphics.text(fontBig, module.id, nameX, nameY, rainbowColor, false)
        graphics.rect(
            windowX + (outlineWidth / guiScale),
            windowLineY.toFloat(),
            windowWidth - (outlineWidth / guiScale * 2f),
            lineHeight,
            outlineColor
        )

        val settingVPadding = 4
        val settingX = windowX + settingHPadding
        var settingY = windowLineY + lineHeightCeil

        for (setting in module.settings) {
            if (setting.id == "Enabled") continue
            settingY += settingVPadding

            graphics.text(fontSmall, setting.name, settingX, settingY, -1, false)
            settingY += fontSmallHeight

            when (setting) {
                is ColorSetting -> {}
                is KeybindSetting -> {}
                is ListSetting<*> -> {}
                is NumberSetting<*> -> {}
                is RangeSetting<*, *> -> {}
                is Setting<*> -> {}
            }
        }
    }

    private fun blendModuleColor(module: ModuleData, darkRainbowColor: Int, rainbowColor: Int): Int {
        val baseColor = lerpColor(0xBBCCCCCC.toInt(), darkRainbowColor, module.enabledProgress)
        val hoverColor = lerpColor(0xBBFFFFFF.toInt(), rainbowColor, module.enabledProgress)
        return lerpColor(baseColor, hoverColor, module.hoverProgress)
    }

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

    override fun keyPressed(event: KeyEvent): Boolean {
        val guiKey = KeyMappingHelper.getBoundKeyOf(minecraft.options.keySocialInteractions).value
        val shouldClose = event.key == guiKey || event.isEscape

        if (currentlyConfiguring != null) {
            if (shouldClose) currentlyConfiguring = null
            return true
        }

        if (shouldClose) onClose()
        return true
    }

    override fun mouseMoved(x: Double, y: Double) {
        if (currentlyConfiguring != null) return

        val hoveredModules = getAllModules().filter {
            it.hovered = false
            it.bounds.inBounds(x, y)
        }
        // only one module should be hovered at once
        val module = hoveredModules.getOrNull(0) ?: return
        module.hovered = true
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        if (currentlyConfiguring != null) return true
        val module = getAllModules().find { it.bounds.inBounds(event) } ?: return true

        if (event.button() == 0) {
            module.live.toggle()
        } else if (event.button() == 1) {
            currentlyConfiguring = module.live
            resetState()
        }

        return true
    }

    override fun onClose() {
        resetState()
        super.onClose()
    }

    private fun resetState() = getAllModules().forEach {
        it.hoverProgress = 0f
        it.hovered = false
    }

    private fun getAllModules() = categories.flatMap { it.modules }

    override fun extractTransparentBackground(graphics: GuiGraphicsExtractor) = graphics.blurBeforeThisStratum()
    override fun isInGameUi() = true
    override fun isPauseScreen() = false

    class CategoryData(val name: String, val modules: List<ModuleData>)
    class ModuleData(
        val live: Module,
        var bounds: Bounds = Bounds(),
        var hovered: Boolean = false,
        var hoverProgress: Float = 0f,
        var enabledProgress: Float = if (live.enabled) 1f else 0f,
    )

    class Bounds(val x: Int, val y: Int, width: Int, height: Int) {
        val x2: Int = x + width
        val y2: Int = y + height

        constructor() : this(0, 0, 0, 0)

        fun inBounds(event: MouseButtonEvent) = inBounds(event.x, event.y)
        fun inBounds(cx: Double, cy: Double) = cx >= x && cy >= y && cx <= x2 && cy <= y2
    }
}
