package nieboczek.lifestolen.serializer.base

import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder
import nieboczek.lifestolen.serializer.lang.TokenStream
import nieboczek.lifestolen.serializer.lang.TokenType

class MapSerializer<K, V>(
    private val keySerializer: Serializer<K>,
    private val valueSerializer: Serializer<V>
) : Serializer<Map<K, V>>() {
    override fun serialize(value: Map<K, V>, builder: SerializedStringBuilder) {
        builder.text('{').newLine()
        builder.indent()
        for (entry in value.entries) {
            builder.indented()
            keySerializer.serialize(entry.key, builder)
            builder.text(" = ")
            valueSerializer.serialize(entry.value, builder)
            builder.text(';').newLine()
        }
        builder.unindent()
        builder.indented().text('}')
    }

    override fun deserialize(stream: TokenStream): Map<K, V> {
        stream.expect(TokenType.L_BRACE)
        val map = HashMap<K, V>()

        while (stream.continueIfNot(TokenType.R_BRACE)) {
            val key = keySerializer.deserialize(stream)
            stream.expect(TokenType.EQUAL)
            val value = valueSerializer.deserialize(stream)
            stream.expect(TokenType.SEMICOLON)
            map[key] = value
        }

        return map
    }
}
