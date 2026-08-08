package nieboczek.lifestolen.config.serializer.base

import nieboczek.lifestolen.config.serializer.lang.SerializedStringBuilder
import nieboczek.lifestolen.config.serializer.lang.TokenStream

abstract class Serializer<T> {
    abstract fun serialize(value: T, builder: SerializedStringBuilder)
    abstract fun deserialize(stream: TokenStream): T
}
