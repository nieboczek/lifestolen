package nieboczek.lifestolen.serializer.base;

import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder;
import nieboczek.lifestolen.serializer.lang.TokenStream;
import nieboczek.lifestolen.serializer.lang.TokenType;

public class IdentifierSerializer extends Serializer<String> {
    private IdentifierSerializer() {}

    public static IdentifierSerializer of() {
        return new IdentifierSerializer();
    }

    @Override
    public void serialize(String value, SerializedStringBuilder builder) {
        builder.text(value);
    }

    @Override
    public String deserialize(TokenStream stream) {
        return stream.nextTokenText(TokenType.IDENTIFIER);
    }
}
