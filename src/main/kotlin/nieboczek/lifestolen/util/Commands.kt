package nieboczek.lifestolen.util

import com.mojang.blaze3d.platform.InputConstants
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.Lifestolen.modules
import nieboczek.lifestolen.module.Module
import org.lwjgl.glfw.GLFW
import java.util.concurrent.CompletableFuture


object Commands {
    private val moduleIds = modules.map { it.id }
    private val moduleSuggestionProvider = ListSuggestionProvider { moduleIds }

    // Used for /r /reply command
    var lastSender: String? = null

    fun register(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
        dispatcher.register(createFriendsCommand())
        dispatcher.register(createKillSwitchCommand())
        dispatcher.register(createBindCommand())
        dispatcher.register(createToggleCommand())
        dispatcher.register(createReplyCommand())
    }

    private fun getModule(it: CommandContext<FabricClientCommandSource>, argName: String): Module? {
        val str = it.getArgument(argName, String::class.java).lowercase()
        for (module in modules) {
            if (str == module.id.lowercase()) {
                return module
            }
        }
        return null
    }

    private fun createFriendsCommand(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return literal("friends").then(
            literal("add").then(
                argument("player", StringArgumentType.string())
                    .suggests { context, builder -> EntityArgument.player().listSuggestions(context, builder) }
                    .executes {
                        val name = it.getArgument("player", String::class.java)
                        Lifestolen.cfg!!.friends.add(name)
                        Lifestolen.displayStatus(Component.literal("Added $name to friends"))
                        1
                    })
        ).then(
            literal("remove").then(
                argument("player", StringArgumentType.string())
                    .suggests(ListSuggestionProvider { Lifestolen.cfg!!.friends })
                    .executes {
                        val name = it.getArgument("player", String::class.java)
                        if (Lifestolen.cfg!!.friends.remove(name))
                            Lifestolen.displayStatus(Component.literal("Removed $name from friends"))
                        else
                            Lifestolen.displayStatus(Component.literal("$name isn't a friend"))
                        1
                    }
            )
        ).then(
            literal("list").executes {
                Lifestolen.displayStatus(Component.literal(Lifestolen.cfg!!.friends.toString()))
                1
            }
        )
    }

    private fun createKillSwitchCommand(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return literal("kys").executes {
            Lifestolen.toggleKillSwitch()
            1
        }
    }

    private fun createReplyCommand(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return literal("r").then(
            argument<String>("message", StringArgumentType.greedyString()).executes {
                if (lastSender != null) {
                    val message = StringArgumentType.getString(it, "message")
                    val command =
                        if (it.source!!.player.connection.commands.findNode(listOf("minecraft:msg")) != null) "minecraft:msg"
                        else "msg"

                    it.source!!.player.connection.sendCommand("$command $lastSender $message")
                } else {
                    Lifestolen.displayStatus(Component.literal("No one to reply to"))
                }
                1
            })
    }

    private fun createToggleCommand(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return literal("t").then(
            argument("module", StringArgumentType.string()).suggests(moduleSuggestionProvider).executes {
                val module = getModule(it, "module")!!
                module.toggle()

                val status = if (module.enabled) Component.literal("enabled")
                    .withColor(0x00FF00) else Component.literal("disabled").withColor(0xFF3636)

                Lifestolen.displayStatus(Component.literal("Module ${module.id} has been ").append(status))
                1
            })
    }

    private fun createBindCommand(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return literal("bind").then(
            argument("module", StringArgumentType.string()).suggests(moduleSuggestionProvider).then(
                argument("key", StringArgumentType.string()).executes {
                    val module = getModule(it, "module")!!
                    val keycode = parseKeycode(it.getArgument("key", String::class.java))

                    module.keybind = keycode

                    val label = InputConstants.Type.KEYSYM.getOrCreate(keycode).displayName.string
                    val coloredLabel = Component.literal(label).withColor(0xBBAAE0)
                    Lifestolen.displayStatus(Component.literal("Bound module ${module.id} to ").append(coloredLabel))
                    1
                })
        )
    }

    private fun parseKeycode(label: String): Int {
        return when (val lowerCaseLabel = label.lowercase()) {
            "space" -> GLFW.GLFW_KEY_SPACE
            "shift", "lshift" -> GLFW.GLFW_KEY_LEFT_SHIFT
            "rshift" -> GLFW.GLFW_KEY_RIGHT_SHIFT
            "ctrl", "lctrl" -> GLFW.GLFW_KEY_LEFT_CONTROL
            "rctrl" -> GLFW.GLFW_KEY_RIGHT_CONTROL
            "alt", "lalt" -> GLFW.GLFW_KEY_LEFT_ALT
            "ralt" -> GLFW.GLFW_KEY_RIGHT_ALT
            "enter" -> GLFW.GLFW_KEY_ENTER
            "tab" -> GLFW.GLFW_KEY_TAB
            "backspace" -> GLFW.GLFW_KEY_BACKSPACE
            "del", "delete" -> GLFW.GLFW_KEY_DELETE
            "ins", "insert" -> GLFW.GLFW_KEY_INSERT
            "home" -> GLFW.GLFW_KEY_HOME
            "end" -> GLFW.GLFW_KEY_END
            "pgup", "pageup" -> GLFW.GLFW_KEY_PAGE_UP
            "pgdn", "pagedown" -> GLFW.GLFW_KEY_PAGE_DOWN
            "up" -> GLFW.GLFW_KEY_UP
            "down" -> GLFW.GLFW_KEY_DOWN
            "left" -> GLFW.GLFW_KEY_LEFT
            "right" -> GLFW.GLFW_KEY_RIGHT
            "esc", "escape" -> GLFW.GLFW_KEY_ESCAPE
            "caps", "capslock" -> GLFW.GLFW_KEY_CAPS_LOCK
            "pause" -> GLFW.GLFW_KEY_PAUSE
            "scrlk", "scrolllock" -> GLFW.GLFW_KEY_SCROLL_LOCK
            ";" -> GLFW.GLFW_KEY_SEMICOLON
            "'" -> GLFW.GLFW_KEY_APOSTROPHE
            "\\" -> GLFW.GLFW_KEY_BACKSLASH
            "/" -> GLFW.GLFW_KEY_SLASH
            "f1" -> GLFW.GLFW_KEY_F1
            "f2" -> GLFW.GLFW_KEY_F2
            "f3" -> GLFW.GLFW_KEY_F3
            "f4" -> GLFW.GLFW_KEY_F4
            "f5" -> GLFW.GLFW_KEY_F5
            "f6" -> GLFW.GLFW_KEY_F6
            "f7" -> GLFW.GLFW_KEY_F7
            "f8" -> GLFW.GLFW_KEY_F8
            "f9" -> GLFW.GLFW_KEY_F9
            "f10" -> GLFW.GLFW_KEY_F10
            "f11" -> GLFW.GLFW_KEY_F11
            "f12" -> GLFW.GLFW_KEY_F12
            "f13" -> GLFW.GLFW_KEY_F13
            "f14" -> GLFW.GLFW_KEY_F14
            "f15" -> GLFW.GLFW_KEY_F15
            "f16" -> GLFW.GLFW_KEY_F16
            "f17" -> GLFW.GLFW_KEY_F17
            "f18" -> GLFW.GLFW_KEY_F18
            "f19" -> GLFW.GLFW_KEY_F19
            "f20" -> GLFW.GLFW_KEY_F20
            "f21" -> GLFW.GLFW_KEY_F21
            "f22" -> GLFW.GLFW_KEY_F22
            "f23" -> GLFW.GLFW_KEY_F23
            "f24" -> GLFW.GLFW_KEY_F24
            "f25" -> GLFW.GLFW_KEY_F25
            "a" -> GLFW.GLFW_KEY_A
            "b" -> GLFW.GLFW_KEY_B
            "c" -> GLFW.GLFW_KEY_C
            "d" -> GLFW.GLFW_KEY_D
            "e" -> GLFW.GLFW_KEY_E
            "f" -> GLFW.GLFW_KEY_F
            "g" -> GLFW.GLFW_KEY_G
            "h" -> GLFW.GLFW_KEY_H
            "i" -> GLFW.GLFW_KEY_I
            "j" -> GLFW.GLFW_KEY_J
            "k" -> GLFW.GLFW_KEY_K
            "l" -> GLFW.GLFW_KEY_L
            "m" -> GLFW.GLFW_KEY_M
            "n" -> GLFW.GLFW_KEY_N
            "o" -> GLFW.GLFW_KEY_O
            "p" -> GLFW.GLFW_KEY_P
            "q" -> GLFW.GLFW_KEY_Q
            "r" -> GLFW.GLFW_KEY_R
            "s" -> GLFW.GLFW_KEY_S
            "t" -> GLFW.GLFW_KEY_T
            "u" -> GLFW.GLFW_KEY_U
            "v" -> GLFW.GLFW_KEY_V
            "w" -> GLFW.GLFW_KEY_W
            "x" -> GLFW.GLFW_KEY_X
            "y" -> GLFW.GLFW_KEY_Y
            "z" -> GLFW.GLFW_KEY_Z
            "0" -> GLFW.GLFW_KEY_0
            "1" -> GLFW.GLFW_KEY_1
            "2" -> GLFW.GLFW_KEY_2
            "3" -> GLFW.GLFW_KEY_3
            "4" -> GLFW.GLFW_KEY_4
            "5" -> GLFW.GLFW_KEY_5
            "6" -> GLFW.GLFW_KEY_6
            "7" -> GLFW.GLFW_KEY_7
            "8" -> GLFW.GLFW_KEY_8
            "9" -> GLFW.GLFW_KEY_9
            else -> {
                try {
                    lowerCaseLabel.replace("key ", "").toInt()
                } catch (_: NumberFormatException) {
                    0
                }
            }
        }
    }

    private class ListSuggestionProvider(val possibilities: () -> List<String>) :
        SuggestionProvider<FabricClientCommandSource> {
        override fun getSuggestions(
            context: CommandContext<FabricClientCommandSource>, builder: SuggestionsBuilder
        ): CompletableFuture<Suggestions> {
            val incomplete = builder.remainingLowerCase

            for (module in possibilities()) {
                if (module.lowercase().contains(incomplete)) {
                    builder.suggest(module)
                }
            }

            return builder.buildFuture()
        }
    }
}