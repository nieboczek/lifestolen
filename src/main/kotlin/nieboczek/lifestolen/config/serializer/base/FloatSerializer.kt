package nieboczek.lifestolen.config.serializer.base

import nieboczek.lifestolen.config.serializer.SerializerException
import nieboczek.lifestolen.config.serializer.lang.SerializedStringBuilder
import nieboczek.lifestolen.config.serializer.lang.TokenStream
import nieboczek.lifestolen.config.serializer.lang.TokenType

class FloatSerializer : Serializer<Float>() {
    override fun serialize(value: Float, builder: SerializedStringBuilder) {
        builder.text(value.toString())
    }

    override fun deserialize(stream: TokenStream): Float {
        val text = stream.nextTokenText(TokenType.NUMBER)
        try {
            return text.toFloat()
        } catch (e: NumberFormatException) {
            throw SerializerException("Invalid float value: \"$text\"", e)
        }
    }
}
