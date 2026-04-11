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
import nieboczek.lifestolen.Formatting
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.config.setting.NumberSetting
import nieboczek.lifestolen.config.setting.RangeSetting
import nieboczek.lifestolen.config.setting.Setting

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

    var keybind = 0
    var enabled = false
    val settings = ArrayList<Setting<*>>()

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
    }

    fun intRange(name: String, default: IntRange, allowed: IntRange, suffix: String = ""): Setting<IntRange> {
        return addSetting(RangeSetting(name, default, allowed, suffix))
    }

    fun int(name: String, default: Int, allowed: IntRange, suffix: String = ""): Setting<Int> {
        return addSetting(NumberSetting(name, default, allowed, suffix))
    }

    fun double(name: String, default: Double, allowed: ClosedFloatingPointRange<Double>, suffix: String = ""): Setting<Double> {
        return addSetting(NumberSetting(name, default, allowed, suffix))
    }

    fun boolean(name: String, default: Boolean): Setting<Boolean> {
        return addSetting(Setting(name, default))
    }

    fun float(name: String, default: Float, allowed: ClosedFloatingPointRange<Float>, suffix: String = ""): Setting<Float> {
        return addSetting(NumberSetting(name, default, allowed, suffix))
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
