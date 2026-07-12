package nieboczek.lifestolen.config.setting

import nieboczek.lifestolen.serializer.base.IntRangeSerializer

class IntRangeSetting(
    name: String,
    default: IntRange,
    val allowed: IntRange,
    val suffix: String = "",
    val step: Int,
) : Setting<IntRange>(name, default, IntRangeSerializer())
