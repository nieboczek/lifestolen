package nieboczek.lifestolen.gui

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import nieboczek.lifestolen.Lifestolen
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
    private var rainbowColorOffset = 0

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        rainbowColorOffset += 1
        val hue = (rainbowColorOffset % 360) / 360f
        val rainbowColor = Color.HSBtoRGB(hue, 0.5f, 1f)
        val darkRainbowColor = Color.HSBtoRGB(hue, 0.5f, 0.75f)

        val dt = a * 0.5f
        categories.flatMap { it.modules }.forEach {
            if (it.hovered) it.hoverProgress = (it.hoverProgress + dt).coerceAtMost(1f)
            else it.hoverProgress = (it.hoverProgress - dt).coerceAtLeast(0f)
        }

        val outlineColor = 0x77888888

        val fontBigHeight = 12
        val fontHeight = 8
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
        val moduleStartY = lineY + kotlin.math.ceil(lineHeight).toInt() + moduleVPadding

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
                categoryWidth - (outlineWidth / guiScale * 2),
                lineHeight,
                outlineColor
            )

            val moduleX = categoryX + moduleHPadding
            category.modules.forEachIndexed { idx, module ->
                val moduleY = moduleStartY + ((moduleVPadding + moduleHeight) * idx)
                graphics.roundedRect(moduleX, moduleY, moduleWidth, moduleHeight, 0, outlineColor, outlineWidth, 4f)

                module.bounds = Bounds(moduleX, moduleY, moduleWidth, moduleHeight)

                val moduleNameX = moduleX + moduleNameHPadding + ((moduleWidth - font.width(module.live.id)) / 2)
                val color =
                    if (module.live.enabled) lerpColor(darkRainbowColor, rainbowColor, module.hoverProgress)
                    else lerpColor(0xBBCCCCCC.toInt(), 0xBBFFFFFF.toInt(), module.hoverProgress)

                graphics.text(font, module.live.id, moduleNameX, moduleY + moduleNameVPadding, color, false)
            }
        }
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        val guiKey = KeyMappingHelper.getBoundKeyOf(minecraft.options.keySocialInteractions).value
        if (event.key == guiKey || event.isEscape) {
            onClose()
            return true
        }
        return super.keyPressed(event)
    }

    override fun mouseMoved(x: Double, y: Double) {
        val hoveredModules = categories.flatMap { it.modules }.filter {
            it.hovered = false
            it.bounds.inBounds(x, y)
        }
        // only one module should be hovered at once
        val module = hoveredModules.getOrNull(0) ?: return
        module.hovered = true
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        if (event.button() != 0) return true

        val clickableModules = categories.flatMap { it.modules }.filter { it.bounds.inBounds(event) }
        // only one module should be hovered at once
        val module = clickableModules.getOrNull(0) ?: return true
        module.live.toggle()
        return true
    }

    override fun onClose() {
        categories.flatMap { it.modules }.forEach {
            it.hoverProgress = 0f
            it.hovered = false
        }
        super.onClose()
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

    override fun extractTransparentBackground(graphics: GuiGraphicsExtractor) = graphics.blurBeforeThisStratum()
    override fun isInGameUi() = true
    override fun isPauseScreen() = false

    class CategoryData(val name: String, val modules: List<ModuleData>)
    class ModuleData(
        val live: Module,
        var bounds: Bounds = Bounds(),
        var hovered: Boolean = false,
        var hoverProgress: Float = 0f,
    )

    class Bounds(val x: Int, val y: Int, width: Int, height: Int) {
        val x2: Int = x + width
        val y2: Int = y + height

        constructor() : this(0, 0, 0, 0)

        fun inBounds(event: MouseButtonEvent) = inBounds(event.x, event.y)
        fun inBounds(cx: Double, cy: Double) = cx >= x && cy >= y && cx <= x2 && cy <= y2
    }
}
