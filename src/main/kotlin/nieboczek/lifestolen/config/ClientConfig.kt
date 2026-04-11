package nieboczek.lifestolen.config

import nieboczek.lifestolen.serializer.base.BooleanSerializer
import nieboczek.lifestolen.serializer.base.ClassSerializer
import nieboczek.lifestolen.serializer.base.FloatSerializer

/** Access instance at [nieboczek.lifestolen.Lifestolen.cfg] */
class ClientConfig {
    // Populate with default values
    var renderClientBrandText: Boolean = false
    var textScale: Float = 1f

    companion object {
        const val ID: String = "Client"
        val serializer = ClassSerializer { ClientConfig() }
            .field("RenderClientBrandText", BooleanSerializer(), { it.renderClientBrandText }, { c, v -> c.renderClientBrandText = v })
            .field("TextScale", FloatSerializer(), { it.textScale }, { c, v -> c.textScale = v })
    }
}
