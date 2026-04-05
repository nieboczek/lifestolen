package nieboczek.lifestolen.serializer.lang;

public final class Token {
    public final TokenType type;
    public final String text;

    public Token(TokenType type, String text) {
        this.type = type;
        this.text = text;
    }

    public Token(TokenType type) {
        this.type = type;
        this.text = null;
    }
}
