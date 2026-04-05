package nieboczek.lifestolen.serializer.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SerializedStringBuilderTest {

    @Test
    void testEmptyBuilder() {
        SerializedStringBuilder builder = new SerializedStringBuilder();
        assertEquals("", builder.getString());
    }

    @Test
    void testText() {
        SerializedStringBuilder builder = new SerializedStringBuilder();
        builder.text("hello");
        assertEquals("hello", builder.getString());
    }

    @Test
    void testChar() {
        SerializedStringBuilder builder = new SerializedStringBuilder();
        builder.text('{');
        assertEquals("{", builder.getString());
    }

    @Test
    void testNewLine() {
        SerializedStringBuilder builder = new SerializedStringBuilder();
        builder.text("line1").newLine().text("line2");
        assertEquals("line1\nline2", builder.getString());
    }

    @Test
    void testIndent() {
        SerializedStringBuilder builder = new SerializedStringBuilder();
        builder.indent();
        builder.indented().text("content");
        assertEquals("  content", builder.getString());
    }

    @Test
    void testDoubleIndent() {
        SerializedStringBuilder builder = new SerializedStringBuilder();
        builder.indent();
        builder.indent();
        builder.indented().text("content");
        assertEquals("    content", builder.getString());
    }

    @Test
    void testUnindent() {
        SerializedStringBuilder builder = new SerializedStringBuilder();
        builder.indent();
        builder.indent();
        builder.unindent();
        builder.indented().text("content");
        assertEquals("  content", builder.getString());
    }

    @Test
    void testUnindentToZero() {
        SerializedStringBuilder builder = new SerializedStringBuilder();
        builder.indent();
        builder.unindent();
        builder.indented().text("content");
        assertEquals("content", builder.getString());
    }

    @Test
    void testUnindentBelowZero() {
        SerializedStringBuilder builder = new SerializedStringBuilder();
        assertThrows(IllegalStateException.class, builder::unindent);
    }

    @Test
    void testUnindentBelowZeroAfterMultiple() {
        SerializedStringBuilder builder = new SerializedStringBuilder();
        builder.indent();
        builder.unindent();
        assertThrows(IllegalStateException.class, builder::unindent);
    }

    @Test
    void testChaining() {
        SerializedStringBuilder builder = new SerializedStringBuilder();
        builder.indent();
        builder.indented().text("foo").newLine();
        builder.unindent();
        builder.indented().text("bar");
        assertEquals("  foo\nbar", builder.getString());
    }

    @Test
    void testComplexStructure() {
        SerializedStringBuilder builder = new SerializedStringBuilder();
        builder.text("{{").newLine();
        builder.indent();
        builder.indented().text("field = value;").newLine();
        builder.unindent();
        builder.indented().text("}}");
        
        assertEquals("{{\n  field = value;\n}}", builder.getString());
    }
}
