package nieboczek.lifestolen.config.setting

import nieboczek.lifestolen.serializer.base.Serializer

class NumberSetting<T : Comparable<T>>(
    name: String,
    default: T,
    val allowed: ClosedRange<T>,
    val suffix: String = "",
    val step: T,
    serializer: Serializer<T>,
) : Setting<T>(name, default, serializer)
