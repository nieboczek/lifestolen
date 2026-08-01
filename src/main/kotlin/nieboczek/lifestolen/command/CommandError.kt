package nieboczek.lifestolen.command

import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style

class CommandError(message: String) : IllegalArgumentException(message) {
    fun toFormattedText() = FormattedText.of(message.orEmpty(), Style.EMPTY.withColor(0xFF3636))
}
