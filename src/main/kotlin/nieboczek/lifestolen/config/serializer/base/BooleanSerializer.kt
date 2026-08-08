package nieboczek.lifestolen.config.serializer.base

import nieboczek.lifestolen.config.serializer.SerializerException
import nieboczek.lifestolen.config.serializer.lang.SerializedStringBuilder
import nieboczek.lifestolen.config.serializer.lang.TokenStream
import nieboczek.lifestolen.config.serializer.lang.TokenType

class BooleanSerializer : Serializer<Boolean>() {
    override fun serialize(value: Boolean, builder: SerializedStringBuilder) {
        builder.text(value.toString())
    }

    override fun deserialize(stream: TokenStream): Boolean {
        val text = stream.nextTokenText(TokenType.IDENTIFIER)
        val bool = text.toBooleanStrictOrNull()
        return bool ?: throw SerializerException("Invalid boolean value: \"$text\"")
    }
}
