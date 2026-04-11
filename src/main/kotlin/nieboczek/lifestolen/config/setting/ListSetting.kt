package nieboczek.lifestolen.config.setting

open class ListSetting<T>(
    name: String,
    default: MutableList<T>
) : Setting<MutableList<T>>(name, default)
