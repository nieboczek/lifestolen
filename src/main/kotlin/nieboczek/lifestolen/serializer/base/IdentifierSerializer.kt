package nieboczek.lifestolen.serializer.base

import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder
import nieboczek.lifestolen.serializer.lang.TokenStream
import nieboczek.lifestolen.serializer.lang.TokenType

class IdentifierSerializer : Serializer<String>() {
    override fun serialize(value: String, builder: SerializedStringBuilder) {
        builder.text(value)
    }

    override fun deserialize(stream: TokenStream): String {
        return stream.nextTokenText(TokenType.IDENTIFIER)
    }
}
