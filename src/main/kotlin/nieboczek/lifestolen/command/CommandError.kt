package nieboczek.lifestolen.command

import net.minecraft.network.chat.Component

class CommandError(message: String) : IllegalArgumentException(message) {
    fun toComponent(): Component {
        return Component.literal(message.orEmpty()).withColor(0xFF3636)
    }
}
