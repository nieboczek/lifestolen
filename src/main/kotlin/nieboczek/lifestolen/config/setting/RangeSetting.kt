package nieboczek.lifestolen.config.setting

import nieboczek.lifestolen.serializer.base.Serializer

class RangeSetting<T : ClosedRange<N>, N : Comparable<N>>(
    name: String,
    default: T,
    val allowed: ClosedRange<N>,
    val suffix: String = "",
    serializer: Serializer<T>,
) : Setting<T>(name, default, serializer)
