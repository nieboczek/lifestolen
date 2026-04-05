package nieboczek.lifestolen.serializer.base;

import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder;
import nieboczek.lifestolen.serializer.lang.TokenStream;
import nieboczek.lifestolen.serializer.lang.TokenType;

public class BooleanSerializer extends Serializer<Boolean> {
    private BooleanSerializer() {
    }

    public static BooleanSerializer of() {
        return new BooleanSerializer();
    }

    @Override
    public void serialize(Boolean value, SerializedStringBuilder builder) {
        builder.text(value.toString());
    }

    @Override
    public Boolean deserialize(TokenStream stream) {
        return Boolean.parseBoolean(stream.nextTokenText(TokenType.IDENTIFIER));
    }
}
