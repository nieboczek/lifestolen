package nieboczek.lifestolen.module

object FullBrightModule : Module("Full Bright", Category.VISUALS) {
    private val affectedByKillSwitch by boolean("Affected By Kill Switch")

    override fun enable() = mc.levelExtractor.allChanged()
    override fun disable() {
        if (!enabled || affectedByKillSwitch) mc.levelExtractor.allChanged()
    }
}
