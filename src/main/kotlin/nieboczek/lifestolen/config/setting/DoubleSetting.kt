package nieboczek.lifestolen.config.setting

import nieboczek.lifestolen.serializer.base.DoubleSerializer

class DoubleSetting(
    name: String,
    default: Double,
    allowed: ClosedFloatingPointRange<Double>,
    suffix: String = "",
    step: Double = 1.0
) : NumberSetting<Double>(name, default, allowed, suffix, step, DoubleSerializer())
