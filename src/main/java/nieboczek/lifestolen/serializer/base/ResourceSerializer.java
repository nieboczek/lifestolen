package nieboczek.lifestolen.serializer.base;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder;
import nieboczek.lifestolen.serializer.lang.TokenStream;
import nieboczek.lifestolen.serializer.lang.TokenType;

public class ResourceSerializer<T> extends Serializer<T> {
    private final Registry<T> registry;

    private ResourceSerializer(Registry<T> registry) {
        this.registry = registry;
    }

    public static <T> ResourceSerializer<T> of(Registry<T> registry) {
        return new ResourceSerializer<>(registry);
    }

    @Override
    public void serialize(T value, SerializedStringBuilder builder) {
        Identifier key = registry.getKey(value);
        builder.text('"').text(key.toString()).text('"');
    }

    @Override
    public T deserialize(TokenStream stream) {
        String text = stream.nextTokenText(TokenType.STRING);
        return registry.getValue(Identifier.parse(text));
    }
}
