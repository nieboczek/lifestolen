package nieboczek.lifestolen.config.setting

open class NumberSetting<T : Comparable<T>>(
    name: String,
    default: T,
    val allowed: ClosedRange<T>,
    suffix: String = ""
) : Setting<T>(name, default, suffix)
