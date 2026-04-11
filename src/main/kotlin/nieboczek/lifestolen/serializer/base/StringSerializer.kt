package nieboczek.lifestolen.serializer.base

import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder
import nieboczek.lifestolen.serializer.lang.TokenStream
import nieboczek.lifestolen.serializer.lang.TokenType

class StringSerializer : Serializer<String>() {
    override fun serialize(value: String, builder: SerializedStringBuilder) {
        builder.text('"')
        for (i in 0..<value.length) {
            when (val c = value[i]) {
                '\\', '"' -> builder.text('\\').text(c)
                '\n' -> builder.text('\\').text('n')
                else -> builder.text(c)
            }
        }
        builder.text('"')
    }

    override fun deserialize(stream: TokenStream): String {
        return stream.nextTokenText(TokenType.STRING)
    }
}
