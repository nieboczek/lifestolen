package nieboczek.lifestolen.serializer.base;

public interface ISerializable<T> {
    Serializer<T> getSerializer();
}
