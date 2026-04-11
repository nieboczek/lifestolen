package nieboczek.lifestolen.config.setting

class RangeSetting<T : ClosedRange<N>, N : Comparable<N>>(
    name: String,
    default: T,
    val allowed: ClosedRange<N>,
    suffix: String = ""
) : Setting<T>(name, default, suffix)
