package nieboczek.lifestolen.module

object AutoWebModule : Module("Auto Web", Category.COMBAT) {
    private val range by double("Range", 4.0, 1.0..6.0, "blocks", 0.01)

    override fun tick() {
        val player = AutoTrapModule.findNearestPlayer(range) ?: return
        val spots = AutoTrapModule.findSpotsAroundPlayer(player)
        AutoTrapModule.placeCobwebsOnSpots(spots)
    }

    override fun render3d() = AutoTrapModule.renderGeneric(range)
}
