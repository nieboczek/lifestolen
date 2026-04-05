package nieboczek.lifestolen.serializer.base;

import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder;
import nieboczek.lifestolen.serializer.lang.TokenStream;
import nieboczek.lifestolen.serializer.lang.TokenType;

public class DoubleSerializer extends Serializer<Double> {
    private DoubleSerializer() {
    }

    public static DoubleSerializer of() {
        return new DoubleSerializer();
    }

    @Override
    public void serialize(Double value, SerializedStringBuilder builder) {
        builder.text(value.toString());
    }

    @Override
    public Double deserialize(TokenStream stream) {
        return Double.parseDouble(stream.nextTokenText(TokenType.NUMBER));
    }
}
