package nieboczek.lifestolen.config.setting

import nieboczek.lifestolen.config.serializer.base.BooleanSerializer

class BooleanSetting(name: String, default: Boolean) : Setting<Boolean>(name, default, BooleanSerializer())
