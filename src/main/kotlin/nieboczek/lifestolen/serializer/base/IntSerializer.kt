package nieboczek.lifestolen.serializer.base

import nieboczek.lifestolen.serializer.SerializerError
import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder
import nieboczek.lifestolen.serializer.lang.TokenStream
import nieboczek.lifestolen.serializer.lang.TokenType

class IntSerializer : Serializer<Int>() {
    override fun serialize(value: Int, builder: SerializedStringBuilder) {
        builder.text(value.toString())
    }

    override fun deserialize(stream: TokenStream): Int {
        val text = stream.nextTokenText(TokenType.NUMBER)
        try {
            return text.toInt()
        } catch (e: NumberFormatException) {
            throw SerializerError("Invalid int value: \"$text\"", e)
        }
    }
}
