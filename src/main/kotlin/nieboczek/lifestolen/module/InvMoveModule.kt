package nieboczek.lifestolen.module

object InvMoveModule : Module("Inv Move", Category.MOVEMENT) {
    val passthroughSneak by boolean("Passthrough Sneak", false)
}
