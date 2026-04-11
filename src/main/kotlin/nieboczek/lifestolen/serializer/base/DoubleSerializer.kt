package nieboczek.lifestolen.serializer.base

import nieboczek.lifestolen.serializer.SerializerError
import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder
import nieboczek.lifestolen.serializer.lang.TokenStream
import nieboczek.lifestolen.serializer.lang.TokenType

class DoubleSerializer : Serializer<Double>() {
    override fun serialize(value: Double, builder: SerializedStringBuilder) {
        builder.text(value.toString())
    }

    override fun deserialize(stream: TokenStream): Double {
        val text = stream.nextTokenText(TokenType.NUMBER)
        try {
            return text.toDouble()
        } catch (e: NumberFormatException) {
            throw SerializerError("Invalid double value: \"$text\"", e)
        }
    }
}
