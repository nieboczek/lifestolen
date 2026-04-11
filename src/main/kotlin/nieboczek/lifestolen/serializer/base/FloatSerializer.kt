package nieboczek.lifestolen.serializer.base

import nieboczek.lifestolen.serializer.SerializerError
import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder
import nieboczek.lifestolen.serializer.lang.TokenStream
import nieboczek.lifestolen.serializer.lang.TokenType

class FloatSerializer : Serializer<Float>() {
    override fun serialize(value: Float, builder: SerializedStringBuilder) {
        builder.text(value.toString())
    }

    override fun deserialize(stream: TokenStream): Float {
        val text = stream.nextTokenText(TokenType.NUMBER)
        try {
            return text.toFloat()
        } catch (e: NumberFormatException) {
            throw SerializerError("Invalid float value: \"$text\"", e)
        }
    }
}
