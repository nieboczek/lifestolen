package nieboczek.lifestolen.config.setting

import nieboczek.lifestolen.serializer.base.Serializer
import nieboczek.lifestolen.util.StringUtils
import kotlin.reflect.KProperty

open class Setting<T>(val name: String, val default: T, val serializer: Serializer<T>) {
    val id = StringUtils.titleCaseToPascalCase(name)
    var value = default

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}
