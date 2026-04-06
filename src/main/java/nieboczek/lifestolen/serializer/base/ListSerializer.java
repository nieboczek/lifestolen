package nieboczek.lifestolen.serializer.base;

import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder;
import nieboczek.lifestolen.serializer.lang.TokenStream;
import nieboczek.lifestolen.serializer.lang.TokenType;

import java.util.ArrayList;
import java.util.List;

public class ListSerializer<T> extends Serializer<List<T>> {
    private final Serializer<T> elementSerializer;

    private ListSerializer(Serializer<T> elementSerializer) {
        this.elementSerializer = elementSerializer;
    }

    public static <T> ListSerializer<T> of(Serializer<T> elementSerializer) {
        return new ListSerializer<>(elementSerializer);
    }

    @Override
    public void serialize(List<T> value, SerializedStringBuilder builder) {
        builder.text('[').newLine();
        builder.indent();
        for (T element : value) {
            builder.indented();
            elementSerializer.serialize(element, builder);
            builder.text(';').newLine();
        }
        builder.unindent();
        builder.indented().text(']');
    }

    @Override
    public List<T> deserialize(TokenStream stream) {
        stream.expect(TokenType.L_BRACKET);
        List<T> list = new ArrayList<>();
        while (stream.peek().type() != TokenType.R_BRACKET) {
            list.add(elementSerializer.deserialize(stream));
            stream.expect(TokenType.SEMICOLON);
        }
        stream.expect(TokenType.R_BRACKET);
        return list;
    }
}
