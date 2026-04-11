package nieboczek.lifestolen.config.setting

open class MapSetting<K, V>(
    name: String,
    default: MutableMap<K, V>
) : Setting<MutableMap<K, V>>(name, default)
