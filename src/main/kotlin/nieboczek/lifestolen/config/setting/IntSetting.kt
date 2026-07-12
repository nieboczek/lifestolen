package nieboczek.lifestolen.config.setting

import nieboczek.lifestolen.serializer.base.IntSerializer

class IntSetting(
    name: String,
    default: Int,
    allowed: IntRange,
    suffix: String = "",
    step: Int = 1
) : NumberSetting<Int>(name, default, allowed, suffix, step, IntSerializer())
