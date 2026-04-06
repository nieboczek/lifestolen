package nieboczek.lifestolen.serializer.base;

import nieboczek.lifestolen.serializer.SerializerError;
import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder;
import nieboczek.lifestolen.serializer.lang.TokenStream;
import nieboczek.lifestolen.serializer.lang.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ObjectSerializer<T> extends Serializer<T> {
    private final Supplier<T> constructor;
    private final List<FieldEntry<T, ?>> fields = new ArrayList<>();

    private ObjectSerializer(Supplier<T> constructor) {
        this.constructor = constructor;
    }

    public static <T> ObjectSerializer<T> of(Supplier<T> constructor) {
        return new ObjectSerializer<>(constructor);
    }

    public <F> ObjectSerializer<T> field(String name, Serializer<F> serializer, Function<T, F> getter, FieldSetter<T, F> setter) {
        fields.add(new FieldEntry<>(name, serializer, getter, setter));
        return this;
    }

    @Override
    public void serialize(T value, SerializedStringBuilder builder) {
        builder.text("{{").newLine();
        builder.indent();
        for (FieldEntry<T, ?> entry : fields) {
            builder.indented();
            builder.text(entry.name).text(" = ");
            entry.serialize(value, builder);
            builder.text(';').newLine();
        }
        builder.unindent();
        builder.indented().text("}}");
    }

    @Override
    public T deserialize(TokenStream stream) {
        stream.expect(TokenType.L_BRACE);
        stream.expect(TokenType.L_BRACE);
        T obj = constructor.get();
        while (stream.peek().type() != TokenType.R_BRACE) {
            String fieldName = stream.nextTokenText(TokenType.IDENTIFIER);
            stream.expect(TokenType.EQUAL);
            boolean found = false;
            for (FieldEntry<T, ?> entry : fields) {
                if (entry.name.equals(fieldName)) {
                    entry.deserialize(obj, stream);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new SerializerError("[ObjectSerializer::deserialize] Unknown field: " + fieldName);
            }
            stream.expect(TokenType.SEMICOLON);
        }
        stream.expect(TokenType.R_BRACE);
        stream.expect(TokenType.R_BRACE);
        return obj;
    }

    @FunctionalInterface
    public interface FieldSetter<T, F> {
        void set(T obj, F value);
    }

    private static class FieldEntry<T, F> {
        private final String name;
        private final Serializer<F> serializer;
        private final Function<T, F> getter;
        private final FieldSetter<T, F> setter;

        FieldEntry(String name, Serializer<F> serializer, Function<T, F> getter, FieldSetter<T, F> setter) {
            this.name = name;
            this.serializer = serializer;
            this.getter = getter;
            this.setter = setter;
        }

        void serialize(T obj, SerializedStringBuilder builder) {
            serializer.serialize(getter.apply(obj), builder);
        }

        void deserialize(T obj, TokenStream stream) {
            setter.set(obj, serializer.deserialize(stream));
        }
    }
}
