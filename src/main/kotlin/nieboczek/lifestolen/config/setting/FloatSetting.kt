package nieboczek.lifestolen.config.setting

import nieboczek.lifestolen.serializer.base.FloatSerializer

class FloatSetting(
    name: String,
    default: Float,
    allowed: ClosedFloatingPointRange<Float>,
    suffix: String = "",
    step: Float = 1f
) : NumberSetting<Float>(name, default, allowed, suffix, step, FloatSerializer())
