package nieboczek.lifestolen.serializer.base;

import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder;
import nieboczek.lifestolen.serializer.lang.TokenStream;
import nieboczek.lifestolen.serializer.lang.TokenType;

public class StringSerializer extends Serializer<String> {
    private StringSerializer() {
    }

    public static StringSerializer of() {
        return new StringSerializer();
    }

    @Override
    public void serialize(String value, SerializedStringBuilder builder) {
        builder.text('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\', '"' -> builder.text('\\').text(c);
                case '\n' -> builder.text('\\').text('n');
                default -> builder.text(c);
            }
        }
        builder.text('"');
    }

    @Override
    public String deserialize(TokenStream stream) {
        return stream.nextTokenText(TokenType.STRING);
    }
}
