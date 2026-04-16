package nieboczek.lifestolen.config.setting

import nieboczek.lifestolen.serializer.base.IntSerializer

class KeybindSetting : Setting<Int>("Keybind", 0, IntSerializer())
