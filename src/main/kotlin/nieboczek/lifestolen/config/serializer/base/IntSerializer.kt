package nieboczek.lifestolen.config.serializer.base

import nieboczek.lifestolen.config.serializer.SerializerException
import nieboczek.lifestolen.config.serializer.lang.SerializedStringBuilder
import nieboczek.lifestolen.config.serializer.lang.TokenStream
import nieboczek.lifestolen.config.serializer.lang.TokenType

class IntSerializer : Serializer<Int>() {
    override fun serialize(value: Int, builder: SerializedStringBuilder) {
        builder.text(value.toString())
    }

    override fun deserialize(stream: TokenStream): Int {
        val text = stream.nextTokenText(TokenType.NUMBER)
        try {
            return text.toInt()
        } catch (e: NumberFormatException) {
            throw SerializerException("Invalid int value: \"$text\"", e)
        }
    }
}
