package nieboczek.lifestolen.module

object NoPushModule : Module("No Push", Category.MOVEMENT) {
    val noPushByEntities by boolean("No Push By Entities")
    val noPushByFluids by boolean("No Push By Fluids")
    val noPushByBlocks by boolean("No Push By Blocks", false)
}
