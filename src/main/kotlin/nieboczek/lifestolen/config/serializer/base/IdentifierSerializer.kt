package nieboczek.lifestolen.config.serializer.base

import nieboczek.lifestolen.config.serializer.lang.SerializedStringBuilder
import nieboczek.lifestolen.config.serializer.lang.TokenStream
import nieboczek.lifestolen.config.serializer.lang.TokenType

class IdentifierSerializer : Serializer<String>() {
    override fun serialize(value: String, builder: SerializedStringBuilder) {
        builder.text(value)
    }

    override fun deserialize(stream: TokenStream): String {
        return stream.nextTokenText(TokenType.IDENTIFIER)
    }
}
