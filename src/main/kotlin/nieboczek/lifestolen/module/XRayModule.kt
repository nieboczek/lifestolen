package nieboczek.lifestolen.module

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.config.setting.BlockListSetting
import nieboczek.lifestolen.serializer.minecraft.ResourceSerializer

object XRayModule : Module("XRay", Category.VISUALS) {
    val fullBright by boolean("Full Bright")

    private val defaultBlocks = listOf(
        Blocks.COAL_ORE,
        Blocks.COPPER_ORE,
        Blocks.DIAMOND_ORE,
        Blocks.EMERALD_ORE,
        Blocks.GOLD_ORE,
        Blocks.IRON_ORE,
        Blocks.LAPIS_ORE,
        Blocks.REDSTONE_ORE,
        Blocks.DEEPSLATE_COAL_ORE,
        Blocks.DEEPSLATE_COPPER_ORE,
        Blocks.DEEPSLATE_DIAMOND_ORE,
        Blocks.DEEPSLATE_EMERALD_ORE,
        Blocks.DEEPSLATE_GOLD_ORE,
        Blocks.DEEPSLATE_IRON_ORE,
        Blocks.DEEPSLATE_LAPIS_ORE,
        Blocks.DEEPSLATE_REDSTONE_ORE,
        Blocks.COAL_BLOCK,
        Blocks.DIAMOND_BLOCK,
        Blocks.EMERALD_BLOCK,
        Blocks.GOLD_BLOCK,
        Blocks.IRON_BLOCK,
        Blocks.LAPIS_BLOCK,
        Blocks.REDSTONE_BLOCK,
        Blocks.RAW_COPPER_BLOCK,
        Blocks.RAW_GOLD_BLOCK,
        Blocks.RAW_IRON_BLOCK,
        Blocks.ANCIENT_DEBRIS,
        Blocks.NETHER_GOLD_ORE,
        Blocks.NETHER_QUARTZ_ORE,
        Blocks.NETHERITE_BLOCK,
        Blocks.QUARTZ_BLOCK,
        Blocks.CHEST,
        Blocks.DISPENSER,
        Blocks.DROPPER,
        Blocks.ENDER_CHEST,
        Blocks.HOPPER,
        Blocks.TRAPPED_CHEST,
        Blocks.SHULKER_BOX,
        Blocks.BEACON,
        Blocks.CRAFTING_TABLE,
        Blocks.ENCHANTING_TABLE,
        Blocks.FURNACE,
        Blocks.FLOWER_POT,
        Blocks.JUKEBOX,
        Blocks.LODESTONE,
        Blocks.RESPAWN_ANCHOR,
        Blocks.ANVIL,
        Blocks.CHIPPED_ANVIL,
        Blocks.DAMAGED_ANVIL,
        Blocks.BARREL,
        Blocks.BLAST_FURNACE,
        Blocks.BREWING_STAND,
        Blocks.CARTOGRAPHY_TABLE,
        Blocks.COMPOSTER,
        Blocks.FLETCHING_TABLE,
        Blocks.GRINDSTONE,
        Blocks.LECTERN,
        Blocks.LOOM,
        Blocks.SMITHING_TABLE,
        Blocks.SMOKER,
        Blocks.STONECUTTER,
        Blocks.CAULDRON,
        Blocks.LAVA_CAULDRON,
        Blocks.WATER_CAULDRON,
        Blocks.LAVA,
        Blocks.WATER,
        Blocks.END_PORTAL,
        Blocks.END_PORTAL_FRAME,
        Blocks.NETHER_PORTAL,
        Blocks.CHAIN_COMMAND_BLOCK,
        Blocks.COMMAND_BLOCK,
        Blocks.REPEATING_COMMAND_BLOCK,
        Blocks.BOOKSHELF,
        Blocks.CLAY,
        Blocks.DRAGON_EGG,
        Blocks.FIRE,
        Blocks.SPAWNER,
        Blocks.TNT,
    )

    private val blocks by addSetting(BlockListSetting("Blocks", defaultBlocks.toMutableList()))

    fun isEnabled(): Boolean = enabled && !Lifestolen.killSwitch
    fun shouldRender(state: BlockState): Boolean = state.block in blocks

    override fun enable() = mc.levelExtractor.allChanged()
    override fun disable() = mc.levelExtractor.allChanged()
}
