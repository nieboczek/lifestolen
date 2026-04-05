package nieboczek.lifestolen.serializer.base;

import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder;
import nieboczek.lifestolen.serializer.lang.TokenStream;
import nieboczek.lifestolen.serializer.lang.TokenType;

public class IntegerSerializer extends Serializer<Integer> {
    private IntegerSerializer() {
    }

    public static IntegerSerializer of() {
        return new IntegerSerializer();
    }

    @Override
    public void serialize(Integer value, SerializedStringBuilder builder) {
        builder.text(value.toString());
    }

    @Override
    public Integer deserialize(TokenStream stream) {
        return Integer.parseInt(stream.nextTokenText(TokenType.NUMBER));
    }
}
