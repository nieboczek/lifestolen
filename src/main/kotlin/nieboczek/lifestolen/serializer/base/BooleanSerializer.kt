package nieboczek.lifestolen.serializer.base

import nieboczek.lifestolen.serializer.SerializerError
import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder
import nieboczek.lifestolen.serializer.lang.TokenStream
import nieboczek.lifestolen.serializer.lang.TokenType

class BooleanSerializer : Serializer<Boolean>() {
    override fun serialize(value: Boolean, builder: SerializedStringBuilder) {
        builder.text(value.toString())
    }

    override fun deserialize(stream: TokenStream): Boolean {
        val text = stream.nextTokenText(TokenType.IDENTIFIER)
        val bool = text.toBooleanStrictOrNull()
        return bool ?: throw SerializerError("Invalid boolean value: \"$text\"")
    }
}
