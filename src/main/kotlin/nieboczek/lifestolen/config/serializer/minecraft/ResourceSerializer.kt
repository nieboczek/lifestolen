package nieboczek.lifestolen.config.serializer.minecraft

import net.minecraft.core.Registry
import net.minecraft.resources.Identifier
import nieboczek.lifestolen.config.serializer.lang.SerializedStringBuilder
import nieboczek.lifestolen.config.serializer.lang.TokenStream
import nieboczek.lifestolen.config.serializer.lang.TokenType
import nieboczek.lifestolen.config.serializer.SerializerException
import nieboczek.lifestolen.config.serializer.base.Serializer

class ResourceSerializer<T : Any>(private val registry: Registry<T>) : Serializer<T>() {
    override fun serialize(value: T, builder: SerializedStringBuilder) {
        val key =
            registry.getKey(value) ?: throw SerializerException("Tried to serialize object not found in registry: $value")
        builder.text('"').text(key.toString()).text('"')
    }

    override fun deserialize(stream: TokenStream): T {
        val text = stream.nextTokenText(TokenType.STRING)
        return registry.getValue(Identifier.parse(text))
            ?: throw SerializerException("Invalid Minecraft resource: \"$text\"")
    }
}
