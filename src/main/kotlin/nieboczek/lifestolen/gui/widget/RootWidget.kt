package nieboczek.lifestolen.gui.widget

import net.minecraft.client.gui.GuiGraphicsExtractor
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.config.setting.*
import nieboczek.lifestolen.module.Module

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
