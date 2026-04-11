package nieboczek.lifestolen.config

/** Access instance at [nieboczek.lifestolen.Lifestolen.cfg] */
class ClientConfig {
    var renderClientBrandText: Boolean = false
    var textScale: Float = 1f
    var enabledModules: Map<String, Boolean> = HashMap()

    companion object {
        const val ID: String = "client"
    }
}
