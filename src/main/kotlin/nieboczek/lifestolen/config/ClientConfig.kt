package nieboczek.lifestolen.config

import nieboczek.lifestolen.serializer.base.*

/** Access instance at [nieboczek.lifestolen.Lifestolen.cfg] */
class ClientConfig {
    // Populate with default values
    var renderClientBrandText = false
    var textScale = 1f
    var friends = mutableListOf<String>()

    companion object {
        const val ID: String = "Client"
        val serializer = ClassSerializer { ClientConfig() }
            .field("RenderClientBrandText", BooleanSerializer(), { it.renderClientBrandText }, { c, v -> c.renderClientBrandText = v })
            .field("TextScale", FloatSerializer(), { it.textScale }, { c, v -> c.textScale = v })
            .field("Friends", ListSerializer(StringSerializer()), { it.friends }, { c, v -> c.friends = v })
    }
}
