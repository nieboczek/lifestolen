package nieboczek.lifestolen.config

import nieboczek.lifestolen.serializer.base.BooleanSerializer
import nieboczek.lifestolen.serializer.base.ClassSerializer
import nieboczek.lifestolen.serializer.base.ListSerializer
import nieboczek.lifestolen.serializer.base.StringSerializer

/** Access instance at [nieboczek.lifestolen.Lifestolen.cfg] */
class ClientConfig {
    // Populate with default values
    var renderClientBrandText = false
    var friends = mutableListOf<String>()
    var commandPrefix = "."

    companion object {
        const val ID: String = "Client"
        val serializer = ClassSerializer { ClientConfig() }
            .field("RenderClientBrandText", BooleanSerializer(), { it.renderClientBrandText }, { c, v -> c.renderClientBrandText = v })
            .field("Friends", ListSerializer(StringSerializer()), { it.friends }, { c, v -> c.friends = v })
            .field("CommandPrefix", StringSerializer(), { it.commandPrefix }, { c, v -> c.commandPrefix = v })
    }
}
