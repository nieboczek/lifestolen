package nieboczek.lifestolen.module

import nieboczek.lifestolen.Lifestolen

object FullBrightModule : Module("FullBright", Category.VISUALS) {
    val affectedByKillSwitch by boolean("Affected By Kill Switch")

    @JvmStatic
    fun isEnabled(): Boolean = enabled && (!Lifestolen.killSwitch || !affectedByKillSwitch)
}
