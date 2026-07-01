package nieboczek.lifestolen.module

object InvMoveModule : Module("InvMove", Category.MOVEMENT) {
    val passthroughSneak by boolean("Passthrough Sneak", false)
}
