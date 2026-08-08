package nieboczek.lifestolen.config.setting

import nieboczek.lifestolen.config.serializer.base.IntSerializer

class ColorSetting(name: String, default: Int) : Setting<Int>(name, default, IntSerializer())
