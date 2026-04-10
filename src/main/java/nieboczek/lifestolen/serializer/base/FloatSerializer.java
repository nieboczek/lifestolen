package nieboczek.lifestolen.serializer.base;

import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder;
import nieboczek.lifestolen.serializer.lang.TokenStream;
import nieboczek.lifestolen.serializer.lang.TokenType;

public class FloatSerializer extends Serializer<Float> {
    private FloatSerializer() {
    }

    public static FloatSerializer of() {
        return new FloatSerializer();
    }

    @Override
    public void serialize(Float value, SerializedStringBuilder builder) {
        builder.text(value.toString());
    }

    @Override
    public Float deserialize(TokenStream stream) {
        return Float.parseFloat(stream.nextTokenText(TokenType.NUMBER));
    }
}
