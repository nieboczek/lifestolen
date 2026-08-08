package nieboczek.lifestolen.config.setting

import nieboczek.lifestolen.config.serializer.base.IntSerializer

class KeybindSetting : Setting<Int>("Keybind", 0, IntSerializer())
