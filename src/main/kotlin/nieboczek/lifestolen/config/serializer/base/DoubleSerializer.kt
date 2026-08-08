package nieboczek.lifestolen.config.serializer.base

import nieboczek.lifestolen.config.serializer.SerializerException
import nieboczek.lifestolen.config.serializer.lang.SerializedStringBuilder
import nieboczek.lifestolen.config.serializer.lang.TokenStream
import nieboczek.lifestolen.config.serializer.lang.TokenType

class DoubleSerializer : Serializer<Double>() {
    override fun serialize(value: Double, builder: SerializedStringBuilder) {
        builder.text(value.toString())
    }

    override fun deserialize(stream: TokenStream): Double {
        val text = stream.nextTokenText(TokenType.NUMBER)
        try {
            return text.toDouble()
        } catch (e: NumberFormatException) {
            throw SerializerException("Invalid double value: \"$text\"", e)
        }
    }
}
