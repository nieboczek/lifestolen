package nieboczek.lifestolen.serializer.base;

import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder;
import nieboczek.lifestolen.serializer.lang.TokenStream;
import nieboczek.lifestolen.serializer.lang.TokenType;

import java.util.HashMap;
import java.util.Map;

public class MapSerializer<K, V> extends Serializer<Map<K, V>> {
    private final Serializer<K> keySerializer;
    private final Serializer<V> valueSerializer;

    private MapSerializer(Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
    }

    public static <K, V> MapSerializer<K, V> of(Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        return new MapSerializer<>(keySerializer, valueSerializer);
    }

    @Override
    public void serialize(Map<K, V> value, SerializedStringBuilder builder) {
        builder.text('{').newLine();
        builder.indent();
        for (Map.Entry<K, V> entry : value.entrySet()) {
            builder.indented();
            keySerializer.serialize(entry.getKey(), builder);
            builder.text(" = ");
            valueSerializer.serialize(entry.getValue(), builder);
            builder.text(';').newLine();
        }
        builder.unindent();
        builder.indented().text('}');
    }

    @Override
    public Map<K, V> deserialize(TokenStream stream) {
        stream.expect(TokenType.L_BRACE);
        Map<K, V> map = new HashMap<>();
        while (stream.peek().type() != TokenType.R_BRACE) {
            K key = keySerializer.deserialize(stream);
            stream.expect(TokenType.EQUAL);
            V value = valueSerializer.deserialize(stream);
            stream.expect(TokenType.SEMICOLON);
            map.put(key, value);
        }
        stream.expect(TokenType.R_BRACE);
        return map;
    }
}
