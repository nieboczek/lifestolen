package nieboczek.lifestolen.module

object FullBrightModule : Module("Full Bright", Category.VISUALS) {
    override fun enable() = mc.levelExtractor.allChanged()
    override fun disable() = mc.levelExtractor.allChanged()
}
