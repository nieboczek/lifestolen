package nieboczek.lifestolen.module

import com.mojang.blaze3d.platform.InputConstants
import com.mojang.blaze3d.platform.Window
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.commands.CommandBuildContext
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import nieboczek.lifestolen.util.Formatting
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.config.setting.KeybindSetting
import nieboczek.lifestolen.config.setting.ListSetting
import nieboczek.lifestolen.config.setting.MapSetting
import nieboczek.lifestolen.config.setting.NumberSetting
import nieboczek.lifestolen.config.setting.RangeSetting
import nieboczek.lifestolen.config.setting.Setting
import nieboczek.lifestolen.gui.WebViewManager
import nieboczek.lifestolen.serializer.base.BooleanSerializer
import nieboczek.lifestolen.serializer.base.DoubleSerializer
import nieboczek.lifestolen.serializer.base.FloatSerializer
import nieboczek.lifestolen.serializer.base.IntRangeSerializer
import nieboczek.lifestolen.serializer.base.IntSerializer
import nieboczek.lifestolen.serializer.base.Serializer

abstract class Module(val id: String, val category: Category) {
    companion object {
        fun sendChat(ctx: CommandContext<FabricClientCommandSource>, msg: MutableComponent) {
            ctx.source.sendFeedback(Lifestolen.msgPrefix.copy().append(msg.withColor(0xFFFFFF)))
        }

        fun sendChat(ctx: CommandContext<FabricClientCommandSource>, msg: String) {
            ctx.source.sendFeedback(Lifestolen.msgPrefix.copy().append(Component.literal(msg).withColor(0xFFFFFF)))
        }

        fun sendStatus(msg: Component, mc: Minecraft) {
            mc.player?.displayClientMessage(msg, true)
        }

        fun sendChat(msg: Component, mc: Minecraft) {
            mc.player?.displayClientMessage(msg, false)
        }
    }

    val mc = Minecraft.getInstance()
    val player
        get() = mc.player!!

    val settings = ArrayList<Setting<*>>()

    var enabled by boolean("Enabled", false)
        private set
    var keybind by addSetting(KeybindSetting())

    private var bindHeld = false

    open fun registerCommands(dispatcher: CommandDispatcher<FabricClientCommandSource>, context: CommandBuildContext) {}
    open fun tick() {}
    open fun render2d(context: GuiGraphics) {}
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
            val status = if (enabled) Formatting.green("enabled") else Formatting.red("disabled")
            sendStatus(Component.literal(id).append(" ").append(status), mc)
        }
    }

    fun toggle() {
        enabled = !enabled
        WebViewManager.settingUpdated(id, "Enabled", enabled)
    }

    fun intRange(name: String, default: IntRange, allowed: IntRange, suffix: String = ""): Setting<IntRange> {
        return addSetting(RangeSetting(name, default, allowed, suffix, IntRangeSerializer()))
    }

    fun int(name: String, default: Int, allowed: IntRange, suffix: String = ""): Setting<Int> {
        return addSetting(NumberSetting(name, default, allowed, suffix, IntSerializer()))
    }

    fun double(name: String, default: Double, allowed: ClosedFloatingPointRange<Double>, suffix: String = ""): Setting<Double> {
        return addSetting(NumberSetting(name, default, allowed, suffix, DoubleSerializer()))
    }

    fun boolean(name: String, default: Boolean): Setting<Boolean> {
        return addSetting(Setting(name, default, BooleanSerializer()))
    }

    fun float(name: String, default: Float, allowed: ClosedFloatingPointRange<Float>, suffix: String = ""): Setting<Float> {
        return addSetting(NumberSetting(name, default, allowed, suffix, FloatSerializer()))
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
        MOVEMENT;

        override fun toString(): String = when (this) {
            COMBAT -> "Combat"
            MOVEMENT -> "Movement"
        }
    }
}
