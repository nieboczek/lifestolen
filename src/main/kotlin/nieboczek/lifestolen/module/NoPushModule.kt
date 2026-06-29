package nieboczek.lifestolen.module

object NoPushModule : Module("NoPush", Category.MOVEMENT) {
    val noPushByEntities = boolean("No Push By Entities")
    val noPushByFluids = boolean("No Push By Fluids")
    val noPushByBlocks = boolean("No Push By Blocks", false)
}
