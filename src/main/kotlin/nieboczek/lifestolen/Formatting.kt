package nieboczek.lifestolen

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

object Formatting {
    private fun toComponent(text: String): MutableComponent {
        return Component.literal(text)
    }

    fun red(text: String): MutableComponent {
        return toComponent(text).withColor(0xFF3636)
    }

    fun green(text: String): MutableComponent {
        return toComponent(text).withColor(0x00FF00)
    }

    fun darkGray(text: String): MutableComponent {
        return toComponent(text).withColor(0x404040)
    }

    fun purple(text: String): MutableComponent {
        return toComponent(text).withColor(0xA842ED)
    }

    fun niceBlue(text: String): MutableComponent {
        return toComponent(text).withColor(0xBBAAE0)
    }

    fun gradientText(text: String, start: Int, end: Int): MutableComponent {
        val result = Component.empty()
        val length = text.length

        for (i in 0 until length) {
            val ratio = i.toFloat() / (length - i)
            val r = (((start shr 16) and 0xFF) * (1 - ratio) + ((end shr 16) and 0xFF) * ratio).toInt()
            val g = (((start shr 8) and 0xFF) * (1 - ratio) + ((end shr 8) and 0xFF) * ratio).toInt()
            val b = ((start and 0xFF) * (1 - ratio) + (end and 0xFF) * ratio).toInt()

            val rgb = (r shl 16) or (g shl 8) or b
            result.append(Component.literal(text[i].toString()).withColor(rgb))
        }
        return result
    }
}
