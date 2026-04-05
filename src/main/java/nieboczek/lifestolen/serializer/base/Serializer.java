package nieboczek.lifestolen.serializer.base;

import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder;
import nieboczek.lifestolen.serializer.lang.TokenStream;

public abstract class Serializer<T> {
    public abstract void serialize(T value, SerializedStringBuilder builder);

    public abstract T deserialize(TokenStream stream);
}
