package nieboczek.lifestolen.gui.notification

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import nieboczek.lifestolen.Lifestolen

object AntiCheatDetector {
    private var waitingForCommandResponse = false
    private var pluginsLeft = 0
    private val pluginNames = mutableListOf<String>()

    fun sendPluginsCommand() {
        val connection = Minecraft.getInstance().connection!!
        if (connection.commands.findNode(listOf("plugins")) != null) {
            Lifestolen.log.info("[AntiCheatDetector] Sending /plugins command")
            pluginNames.clear()
            pluginsLeft = 0
            connection.sendCommand("plugins")
            waitingForCommandResponse = true
        }
    }

    fun init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register { component, overlay ->
            if (overlay || !waitingForCommandResponse) return@register true

            val message = component.string
            if (message.startsWith("ℹ Server Plugins (")) {
                pluginsLeft = message.substringAfter("(").substringBefore(")").toIntOrNull() ?: 0
            } else if (message.startsWith(" ")) {
                parsePluginLine(message)
            }

            if (pluginsLeft <= 0) {
                waitingForCommandResponse = false
                Lifestolen.log.info("[AntiCheatDetector] Plugins: $pluginNames")
                if (pluginNames.contains("GrimAC")) {
                    Notifications.add(
                        FormattedText.of("GrimAC was detected (/plugins)", Style.EMPTY.withColor(0xFF6060)),
                        240f,
                    )
                }
            }
            return@register false
        }
    }

    private fun parsePluginLine(message: String) {
        for (name in message.trim().removePrefix("-").split(", ")) {
            if (pluginsLeft > 0) {
                pluginNames.add(name.trim())
                pluginsLeft--
            }
        }
    }
}
