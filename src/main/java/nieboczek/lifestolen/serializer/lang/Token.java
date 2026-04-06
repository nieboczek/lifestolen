package nieboczek.lifestolen.serializer.lang;

public record Token(TokenType type, String text) {
    public Token(TokenType type) {
        this(type, null);
    }
}
