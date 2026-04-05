package nieboczek.lifestolen.serializer.lang;

public final class SerializedStringBuilder {
    private final StringBuilder builder;
    private int indentLevel = 0;

    public SerializedStringBuilder() {
        builder = new StringBuilder();
    }

    public void indent() {
        indentLevel++;
    }

    public void unindent() {
        if (indentLevel == 0)
            throw new IllegalStateException("Tried to decrease the indent level to a negative value");

        indentLevel--;
    }

    public SerializedStringBuilder indented() {
        builder.append("  ".repeat(indentLevel));
        return this;
    }

    public SerializedStringBuilder text(String text) {
        builder.append(text);
        return this;
    }

    public SerializedStringBuilder text(char text) {
        builder.append(text);
        return this;
    }

    public SerializedStringBuilder newLine() {
        builder.append('\n');
        return this;
    }

    public String getString() {
        return builder.toString();
    }
}
