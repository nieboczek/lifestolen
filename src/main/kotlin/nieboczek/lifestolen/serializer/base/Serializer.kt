package nieboczek.lifestolen.serializer.base

import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder
import nieboczek.lifestolen.serializer.lang.TokenStream

abstract class Serializer<T> {
    abstract fun serialize(value: T, builder: SerializedStringBuilder)
    abstract fun deserialize(stream: TokenStream): T
}
