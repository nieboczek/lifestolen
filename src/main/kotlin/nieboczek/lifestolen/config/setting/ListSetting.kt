package nieboczek.lifestolen.config.setting

import nieboczek.lifestolen.serializer.base.ListSerializer
import nieboczek.lifestolen.serializer.base.Serializer

class ListSetting<T>(
    name: String,
    default: MutableList<T>,
    elementSerializer: Serializer<T>,
) : Setting<MutableList<T>>(name, default, ListSerializer(elementSerializer))
