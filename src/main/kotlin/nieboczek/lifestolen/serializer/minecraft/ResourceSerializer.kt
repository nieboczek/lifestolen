package nieboczek.lifestolen.serializer.minecraft

import net.minecraft.core.Registry
import net.minecraft.resources.Identifier
import nieboczek.lifestolen.serializer.SerializerError
import nieboczek.lifestolen.serializer.base.Serializer
import nieboczek.lifestolen.serializer.lang.SerializedStringBuilder
import nieboczek.lifestolen.serializer.lang.TokenStream
import nieboczek.lifestolen.serializer.lang.TokenType

class ResourceSerializer<T : Any>(private val registry: Registry<T>) : Serializer<T>() {
    override fun serialize(value: T, builder: SerializedStringBuilder) {
        val key =
            registry.getKey(value) ?: throw SerializerError("Tried to serialize object not found in registry: $value")
        builder.text('"').text(key.toString()).text('"')
    }

    override fun deserialize(stream: TokenStream): T {
        val text = stream.nextTokenText(TokenType.STRING)
        return registry.getValue(Identifier.parse(text))
            ?: throw SerializerError("Invalid Minecraft resource: \"$text\"")
    }
}
