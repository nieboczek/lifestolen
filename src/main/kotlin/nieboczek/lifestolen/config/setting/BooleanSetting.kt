package nieboczek.lifestolen.config.setting

import nieboczek.lifestolen.serializer.base.BooleanSerializer

class BooleanSetting(name: String, default: Boolean) : Setting<Boolean>(name, default, BooleanSerializer())
