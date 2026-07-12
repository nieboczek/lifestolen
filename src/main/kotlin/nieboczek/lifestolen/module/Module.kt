package nieboczek.lifestolen.module

import com.mojang.blaze3d.platform.InputConstants
import com.mojang.blaze3d.platform.Window
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.config.setting.*

abstract class Module(val id: String, val category: Category) {
    val mc = Minecraft.getInstance()
    val player get() = mc.player!!

    val settings = mutableListOf<Setting<*>>()

    var enabled by boolean("Enabled", false)
        private set
    var keybind by addSetting(KeybindSetting())

    private var bindHeld = false

    open fun tick() {}
    open fun render2d(context: GuiGraphicsExtractor) {}
    open fun render3d() {}
    open fun enable() {}
    open fun disable() {}

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
            val status =
                if (enabled) Component.literal("enabled").withColor(0x00FF00)
                else Component.literal("disabled").withColor(0xFF3636)
            Lifestolen.displayStatus(Component.literal("$id ").append(status))
        }
    }

    fun toggle() {
        enabled = !enabled
        if (enabled) enable() else disable()
    }

    fun intRange(
        name: String,
        default: IntRange,
        allowed: IntRange,
        suffix: String = "",
        step: Int = 1
    ) = addSetting(IntRangeSetting(name, default, allowed, suffix, step))

    fun int(name: String, default: Int, allowed: IntRange, suffix: String = "", step: Int = 1) =
        addSetting(IntSetting(name, default, allowed, suffix, step))

    fun double(
        name: String,
        default: Double,
        allowed: ClosedFloatingPointRange<Double>,
        suffix: String = "",
        step: Double = 1.0
    ) = addSetting(DoubleSetting(name, default, allowed, suffix, step))

    fun boolean(name: String, default: Boolean = true) = addSetting(BooleanSetting(name, default))

    fun float(
        name: String,
        default: Float,
        allowed: ClosedFloatingPointRange<Float>,
        suffix: String = "",
        step: Float = 1f
    ) = addSetting(FloatSetting(name, default, allowed, suffix, step))

    fun color(name: String, default: Int = -1) = addSetting(ColorSetting(name, default))

    fun <T> addSetting(setting: Setting<T>) = setting.also { settings.add(it) }

    enum class Category {
        COMBAT,
        MOVEMENT,
        VISUALS;

        override fun toString() = when (this) {
            COMBAT -> "Combat"
            MOVEMENT -> "Movement"
            VISUALS -> "Visuals"
        }
    }
}
