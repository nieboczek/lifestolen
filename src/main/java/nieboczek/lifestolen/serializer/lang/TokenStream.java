package nieboczek.lifestolen.serializer.lang;

import nieboczek.lifestolen.serializer.SerializerError;

public final class TokenStream {
    private final String src;
    private int i = 0;
    private Token peekResult;

    public TokenStream(String src) {
        this.src = src;
    }

    public void expect(TokenType type) {
        TokenType actualType = next().type();
        if (actualType != type)
            throw new SerializerError("[TokenStream::expect] Expected token " + type + " but got " + actualType);
    }

    public String nextTokenText(TokenType type) {
        Token token = next();
        if (token.type() != type)
            throw new SerializerError("[TokenStream::nextTokenText] Expected token " + type + " but got " + token.type());

        return token.text();
    }

    /// Returns false on consumption, true otherwise. Useful for while loops.
    public boolean consumeTokenIfType(TokenType type) {
        if (peek().type() == type) {
            next();
            return false;
        }
        return true;
    }

    public Token peek() {
        if (peekResult != null)
            return peekResult;

        peekResult = next();
        return peekResult;
    }

    public Token next() {
        if (peekResult != null) {
            Token t = peekResult;
            peekResult = null;
            return t;
        }

        skipWhitespace();

        if (i >= src.length()) {
            return new Token(TokenType.EOF);
        }

        char c = src.charAt(i);

        switch (c) {
            case '{': i++; return new Token(TokenType.L_BRACE);
            case '}': i++; return new Token(TokenType.R_BRACE);
            case '[': i++; return new Token(TokenType.L_BRACKET);
            case ']': i++; return new Token(TokenType.R_BRACKET);
            case '=': i++; return new Token(TokenType.EQUAL);
            case ';': i++; return new Token(TokenType.SEMICOLON);
            case '"': return readString();
        }

        if (isNumberStart(c)) {
            return readNumber();
        }

        if (isIdentifierStart(c)) {
            return readIdentifier();
        }

        throw new SerializerError("[TokenStream::next] Unexpected character: " + c);
    }

    private Token readString() {
        StringBuilder sb = new StringBuilder();
        i++; // skip "

        while (i < src.length()) {
            char c = src.charAt(i);

            if (c == '\\') {
                i++;
                if (i < src.length()) {
                    char ch = src.charAt(i);
                    switch (ch) {
                        case '\\', '"' -> sb.append(ch);
                        case 'n' -> sb.append("\n");
                        default -> throw new SerializerError("[TokenStream::readString] Invalid escape code: " + c + ch);
                    }
                    i++;
                }
                continue;
            }

            if (c == '"') {
                i++; // consume "
                break;
            }

            sb.append(c);
            i++;
        }

        return new Token(TokenType.STRING, sb.toString());
    }

    private Token readNumber() {
        int start = i;
        while (i < src.length() && isNumberPart(src.charAt(i))) {
            i++;
        }
        return new Token(TokenType.NUMBER, src.substring(start, i));
    }

    private boolean isNumberStart(char c) {
        return Character.isDigit(c);
    }

    private boolean isNumberPart(char c) {
        return isNumberStart(c) || c == '.';
    }

    private Token readIdentifier() {
        int start = i;
        while (i < src.length() && isIdentifierPart(src.charAt(i))) {
            i++;
        }
        return new Token(TokenType.IDENTIFIER, src.substring(start, i));
    }

    private boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private boolean isIdentifierPart(char c) {
        return isIdentifierStart(c) || Character.isDigit(c);
    }

    private void skipWhitespace() {
        while (i < src.length() && Character.isWhitespace(src.charAt(i))) {
            i++;
        }
    }
}
