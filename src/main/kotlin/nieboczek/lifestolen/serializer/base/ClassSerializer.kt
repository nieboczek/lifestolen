package nieboczek.lifestolen.serializer.base

import nieboczek.lifestolen.serializer.SerializerError
import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder
import nieboczek.lifestolen.serializer.lang.TokenStream
import nieboczek.lifestolen.serializer.lang.TokenType
import java.util.function.Function
import java.util.function.Supplier

class ClassSerializer<T>(val constructor: Supplier<T>) : Serializer<T>() {
    private val fields: MutableList<FieldEntry<T, *>> = ArrayList<FieldEntry<T, *>>()

    fun <F> field(
        name: String,
        serializer: Serializer<F>,
        getter: Function<T, F>,
        setter: FieldSetter<T, F>
    ): ClassSerializer<T> {
        fields.add(FieldEntry(name, serializer, getter, setter))
        return this
    }

    override fun serialize(value: T, builder: SerializedStringBuilder) {
        builder.text("{").newLine()
        builder.indent()
        for (entry in fields) {
            builder.indented()
            builder.text(entry.name).text(" = ")
            entry.serialize(value, builder)
            builder.text(';').newLine()
        }
        builder.unindent()
        builder.indented().text("}")
    }

    override fun deserialize(stream: TokenStream): T {
        stream.expect(TokenType.L_BRACE)
        val obj = constructor.get()

        while (stream.peek().type !== TokenType.R_BRACE) {
            val fieldName = stream.nextTokenText(TokenType.IDENTIFIER)
            stream.expect(TokenType.EQUAL)
            var found = false

            for (entry in fields) {
                if (entry.name == fieldName) {
                    entry.deserialize(obj, stream)
                    found = true
                    break
                }
            }

            if (!found) throw SerializerError("[ObjectSerializer::deserialize] Unknown field: $fieldName")
            stream.expect(TokenType.SEMICOLON)
        }

        stream.expect(TokenType.R_BRACE)
        return obj
    }

    fun interface FieldSetter<T, F> {
        fun set(obj: T, value: F)
    }

    @JvmRecord
    private data class FieldEntry<T, F>(
        val name: String,
        val serializer: Serializer<F>,
        val getter: Function<T, F>,
        val setter: FieldSetter<T, F>
    ) {
        fun serialize(obj: T, builder: SerializedStringBuilder) {
            serializer.serialize(getter.apply(obj), builder)
        }

        fun deserialize(obj: T, stream: TokenStream) {
            setter.set(obj, serializer.deserialize(stream))
        }
    }
}
