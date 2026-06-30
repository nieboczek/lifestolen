package nieboczek.lifestolen.module

import com.mojang.blaze3d.platform.InputConstants
import com.mojang.blaze3d.platform.Window
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.config.setting.*
import nieboczek.lifestolen.serializer.base.*

abstract class Module(val id: String, val category: Category) {
    val mc = Minecraft.getInstance()
    val player
        get() = mc.player!!

    val settings = ArrayList<Setting<*>>()

    var enabled by boolean("Enabled", false)
        private set
    var keybind by addSetting(KeybindSetting())

    private var bindHeld = false

    open fun tick() {}
    open fun render2d(context: GuiGraphicsExtractor) {}
    open fun render3d() {}

    fun handleBindPress(window: Window) {
        if (keybind <= 0) {
            bindHeld = false
            return
        }

        val pressed = InputConstants.isKeyDown(window, keybind)
        val shouldToggle = pressed && !bindHeld
        bindHeld = pressed

        if (shouldToggle) {
            toggle()
            val status = if (enabled) Component.literal("enabled").withColor(0x00FF00) else Component.literal("disabled").withColor(0xFF3636)
            Lifestolen.displayStatus(Component.literal("$id ").append(status))
        }
    }

    fun toggle() {
        enabled = !enabled
    }

    fun intRange(name: String, default: IntRange, allowed: IntRange, suffix: String = "", step: Int = 1): Setting<IntRange> {
        return addSetting(RangeSetting(name, default, allowed, suffix, step, IntRangeSerializer()))
    }

    fun int(name: String, default: Int, allowed: IntRange, suffix: String = "", step: Int = 1): Setting<Int> {
        return addSetting(NumberSetting(name, default, allowed, suffix, step, IntSerializer()))
    }

    fun double(name: String, default: Double, allowed: ClosedFloatingPointRange<Double>, suffix: String = "", step: Double = 1.0): Setting<Double> {
        return addSetting(NumberSetting(name, default, allowed, suffix, step, DoubleSerializer()))
    }

    fun boolean(name: String, default: Boolean = true): Setting<Boolean> {
        return addSetting(Setting(name, default, BooleanSerializer()))
    }

    fun float(name: String, default: Float, allowed: ClosedFloatingPointRange<Float>, suffix: String = "", step: Float = 1f): Setting<Float> {
        return addSetting(NumberSetting(name, default, allowed, suffix, step, FloatSerializer()))
    }

    fun color(name: String, default: Int = -1): Setting<Int> {
        return addSetting(ColorSetting(name, default))
    }

    fun <T> list(name: String, default: MutableList<T>, elementSerializer: Serializer<T>): Setting<MutableList<T>> {
        return addSetting(ListSetting(name, default, elementSerializer))
    }

    fun <K, V> map(name: String, default: MutableMap<K, V>, keySerializer: Serializer<K>, valueSerializer: Serializer<V>): Setting<MutableMap<K, V>> {
        return addSetting(MapSetting(name, default, keySerializer, valueSerializer))
    }

    private fun <T> addSetting(setting: Setting<T>): Setting<T> {
        return setting.also { settings.add(it) }
    }

    enum class Category {
        COMBAT,
        MOVEMENT,
        VISUALS;

        override fun toString(): String = when (this) {
            COMBAT -> "Combat"
            MOVEMENT -> "Movement"
            VISUALS -> "Visuals"
        }
    }
}
