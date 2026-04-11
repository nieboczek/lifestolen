package nieboczek.lifestolen.config.setting

import nieboczek.lifestolen.serializer.base.MapSerializer
import nieboczek.lifestolen.serializer.base.Serializer

class MapSetting<K, V>(
    name: String,
    default: MutableMap<K, V>,
    keySerializer: Serializer<K>,
    valueSerializer: Serializer<V>,
) : Setting<MutableMap<K, V>>(name, default, MapSerializer(keySerializer, valueSerializer))
