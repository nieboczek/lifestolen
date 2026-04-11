package nieboczek.lifestolen.serializer.lang

class SerializedStringBuilder {
    private val builder = StringBuilder()
    private var indentLevel = 0

    val string: String
        get() = builder.toString()

    fun indent() {
        indentLevel++
    }

    fun unindent() {
        check(indentLevel != 0) { "Tried to decrease the indent level to a negative value" }
        indentLevel--
    }

    fun indented(): SerializedStringBuilder {
        builder.append("  ".repeat(indentLevel))
        return this
    }

    fun text(text: String): SerializedStringBuilder {
        builder.append(text)
        return this
    }

    fun text(text: Char): SerializedStringBuilder {
        builder.append(text)
        return this
    }

    fun newLine(): SerializedStringBuilder {
        builder.append('\n')
        return this
    }
}
