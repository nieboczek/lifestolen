package nieboczek.lifestolen.config.setting

import kotlin.reflect.KProperty

open class Setting<T>(val name: String, default: T, val suffix: String = "") {
    var value = default

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}
