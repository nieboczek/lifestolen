package nieboczek.lifestolen.gui.widget

import net.minecraft.client.gui.GuiGraphicsExtractor
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.config.setting.*
import nieboczek.lifestolen.module.Module
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt

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
                        is NumberSetting<*> -> NumberSettingWidget(setting)
                        is IntRangeSetting -> IntRangeSettingWidget(setting)
                        is BooleanSetting -> BooleanSettingWidget(setting)
                        else -> error("Unsupported setting type: ${setting.javaClass.name}")
                    } as SettingWidget<*>
                })
            },
        )
    }

    private var startY = 8f
    private var targetY = 8f

    fun render(graphics: GuiGraphicsExtractor, screenWidth: Int) {
        categories.forEachIndexed { idx, category ->
            category.render(graphics, startY.roundToInt(), idx, categories.size, screenWidth)
        }
    }

    fun scroll(delta: Double) {
        targetY += delta.toFloat() * 8f
        if (targetY > 8f) targetY = 8f
    }

    override fun tick(dt: Float) {
        startY += (targetY - startY) * (1f - exp(-4f * dt))
        if (abs(targetY - startY) < 0.01f) startY = targetY
    }

    override fun getVisibleChildren() = categories
}
