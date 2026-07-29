package nieboczek.lifestolen.module

import nieboczek.lifestolen.Lifestolen

object NoPushModule : Module("No Push", Category.MOVEMENT) {
    val noPushByEntities by boolean("No Push By Entities")
    val noPushByFluids by boolean("No Push By Fluids")
    val noPushByBlocks by boolean("No Push By Blocks", false)

    fun isEnabled(): Boolean = enabled && !Lifestolen.killSwitch
}
