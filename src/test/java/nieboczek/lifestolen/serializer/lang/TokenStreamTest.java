package nieboczek.lifestolen.serializer.lang;

import nieboczek.lifestolen.serializer.SerializerError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenStreamTest {

    @Test
    void testLBrace() {
        TokenStream ts = new TokenStream("{");
        Token t = ts.next();
        assertEquals(TokenType.L_BRACE, t.type);
        assertNull(t.text);
    }

    @Test
    void testRBrace() {
        TokenStream ts = new TokenStream("}");
        Token t = ts.next();
        assertEquals(TokenType.R_BRACE, t.type);
    }

    @Test
    void testLBracket() {
        TokenStream ts = new TokenStream("[");
        Token t = ts.next();
        assertEquals(TokenType.L_BRACKET, t.type);
    }

    @Test
    void testRBracket() {
        TokenStream ts = new TokenStream("]");
        Token t = ts.next();
        assertEquals(TokenType.R_BRACKET, t.type);
    }

    @Test
    void testEqual() {
        TokenStream ts = new TokenStream("=");
        Token t = ts.next();
        assertEquals(TokenType.EQUAL, t.type);
    }

    @Test
    void testSemicolon() {
        TokenStream ts = new TokenStream(";");
        Token t = ts.next();
        assertEquals(TokenType.SEMICOLON, t.type);
    }

    @Test
    void testString() {
        TokenStream ts = new TokenStream("\"hello world\"");
        Token t = ts.next();
        assertEquals(TokenType.STRING, t.type);
        assertEquals("hello world", t.text);
    }

    @Test
    void testStringWithEscapes() {
        TokenStream ts = new TokenStream("\"hello\\nworld\\\\test\\\"quote\"");
        Token t = ts.next();
        assertEquals(TokenType.STRING, t.type);
        assertEquals("hello\nworld\\test\"quote", t.text);
    }

    @Test
    void testNumber() {
        TokenStream ts = new TokenStream("42");
        Token t = ts.next();
        assertEquals(TokenType.NUMBER, t.type);
        assertEquals("42", t.text);
    }

    @Test
    void testDoubleNumber() {
        TokenStream ts = new TokenStream("3.14");
        Token t = ts.next();
        assertEquals(TokenType.NUMBER, t.type);
        assertEquals("3.14", t.text);
    }

    @Test
    void testIdentifier() {
        TokenStream ts = new TokenStream("myVar");
        Token t = ts.next();
        assertEquals(TokenType.IDENTIFIER, t.type);
        assertEquals("myVar", t.text);
    }

    @Test
    void testIdentifierWithUnderscore() {
        TokenStream ts = new TokenStream("_private_field1");
        Token t = ts.next();
        assertEquals(TokenType.IDENTIFIER, t.type);
        assertEquals("_private_field1", t.text);
    }

    @Test
    void testEOF() {
        TokenStream ts = new TokenStream("");
        Token t = ts.next();
        assertEquals(TokenType.EOF, t.type);
    }

    @Test
    void testSkipWhitespace() {
        TokenStream ts = new TokenStream("  \n\t  hello");
        Token t = ts.next();
        assertEquals(TokenType.IDENTIFIER, t.type);
        assertEquals("hello", t.text);
    }

    @Test
    void testMultipleTokens() {
        TokenStream ts = new TokenStream("foo = \"bar\" ;");
        assertEquals(TokenType.IDENTIFIER, ts.next().type);
        assertEquals(TokenType.EQUAL, ts.next().type);
        assertEquals(TokenType.STRING, ts.next().type);
        assertEquals(TokenType.SEMICOLON, ts.next().type);
        assertEquals(TokenType.EOF, ts.next().type);
    }

    @Test
    void testPeek() {
        TokenStream ts = new TokenStream("foo bar");
        Token peek1 = ts.peek();
        Token peek2 = ts.peek();
        assertSame(peek1, peek2);
        assertEquals(TokenType.IDENTIFIER, peek1.type);
        assertEquals("foo", peek1.text);

        Token next = ts.next();
        assertSame(peek1, next);

        Token next2 = ts.next();
        assertEquals("bar", next2.text);
    }

    @Test
    void testExpect() {
        TokenStream ts = new TokenStream("foo");
        ts.expect(TokenType.IDENTIFIER);
    }

    @Test
    void testExpectThrows() {
        TokenStream ts = new TokenStream("foo");
        assertThrows(SerializerError.class, () -> ts.expect(TokenType.NUMBER));
    }

    @Test
    void testNextTokenText() {
        TokenStream ts = new TokenStream("\"hello\"");
        assertEquals("hello", ts.nextTokenText(TokenType.STRING));
    }

    @Test
    void testNextTokenTextWrongType() {
        TokenStream ts = new TokenStream("\"hello\"");
        assertThrows(SerializerError.class, () -> ts.nextTokenText(TokenType.NUMBER));
    }

    @Test
    void testConsumeTokenIfTypeConsumes() {
        TokenStream ts = new TokenStream("foo");
        assertFalse(ts.consumeTokenIfType(TokenType.IDENTIFIER));
    }

    @Test
    void testConsumeTokenIfTypeDoesNotConsume() {
        TokenStream ts = new TokenStream("foo");
        assertTrue(ts.consumeTokenIfType(TokenType.NUMBER));
    }

    @Test
    void testUnexpectedCharacter() {
        TokenStream ts = new TokenStream("@invalid");
        assertThrows(SerializerError.class, () -> ts.next());
    }

    @Test
    void testInvalidEscapeCode() {
        TokenStream ts = new TokenStream("\"hello\\xworld\"");
        assertThrows(SerializerError.class, () -> ts.next());
    }

    @Test
    void testComplexStructure() {
        String input = "{{\n  name = \"test\";\n  value = 42;\n}}";
        TokenStream ts = new TokenStream(input);

        assertEquals(TokenType.L_BRACE, ts.next().type);
        assertEquals(TokenType.L_BRACE, ts.next().type);
        assertEquals(TokenType.IDENTIFIER, ts.next().type);
        assertEquals(TokenType.EQUAL, ts.next().type);
        assertEquals(TokenType.STRING, ts.next().type);
        assertEquals(TokenType.SEMICOLON, ts.next().type);
        assertEquals(TokenType.IDENTIFIER, ts.next().type);
        assertEquals(TokenType.EQUAL, ts.next().type);
        assertEquals(TokenType.NUMBER, ts.next().type);
        assertEquals(TokenType.SEMICOLON, ts.next().type);
        assertEquals(TokenType.R_BRACE, ts.next().type);
        assertEquals(TokenType.R_BRACE, ts.next().type);
        assertEquals(TokenType.EOF, ts.next().type);
    }
}
