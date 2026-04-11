package nieboczek.lifestolen.serializer.base

import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder
import nieboczek.lifestolen.serializer.lang.TokenStream
import nieboczek.lifestolen.serializer.lang.TokenType

class ListSerializer<T>(private val elementSerializer: Serializer<T>) : Serializer<MutableList<T>>() {
    override fun serialize(value: MutableList<T>, builder: SerializedStringBuilder) {
        builder.text('[').newLine()
        builder.indent()
        for (element in value) {
            builder.indented()
            elementSerializer.serialize(element, builder)
            builder.text(';').newLine()
        }
        builder.unindent()
        builder.indented().text(']')
    }

    override fun deserialize(stream: TokenStream): MutableList<T> {
        stream.expect(TokenType.L_BRACKET)
        val list = ArrayList<T>()

        while (stream.continueIfNot(TokenType.R_BRACKET)) {
            list.add(elementSerializer.deserialize(stream))
            stream.expect(TokenType.SEMICOLON)
        }

        return list
    }
}
