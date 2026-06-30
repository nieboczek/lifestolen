package nieboczek.lifestolen.module

import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse
import net.minecraft.world.entity.vehicle.boat.ChestBoat
import net.minecraft.world.entity.vehicle.boat.ChestRaft
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer
import net.minecraft.world.entity.vehicle.minecart.MinecartHopper
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity
import net.minecraft.world.level.block.entity.BarrelBlockEntity
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity
import net.minecraft.world.level.block.entity.ChestBlockEntity
import net.minecraft.world.level.block.entity.CrafterBlockEntity
import net.minecraft.world.level.block.entity.DispenserBlockEntity
import net.minecraft.world.level.block.entity.EnderChestBlockEntity
import net.minecraft.world.level.block.entity.HopperBlockEntity
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import nieboczek.lifestolen.util.Renderer3d

object ChestESPModule : Module("ChestESP", Category.VISUALS) {
    val range by int("Range", 128, 8..512, "blocks")
    val color by color("Color", 0x55FFFFFF)
    val boxScale by float("Box Scale", 1f, 0.1f..3f, step = 0.01f)
    val lineWidth by float("Line Width", 2f, 0.5f..8f, step = 0.1f)

    val showChests by boolean("Show Chests")
    val showBarrels by boolean("Show Barrels")
    val showEnderChests by boolean("Show Ender Chests")
    val showFurnaces by boolean("Show Furnaces")
    val showBrewingStands by boolean("Show Brewing Stands")
    val showDispensers by boolean("Show Dispensers")
    val showHoppers by boolean("Show Hoppers")
    val showShulkerBoxes by boolean("Show Shulker Boxes")

    private val blockPositions = mutableListOf<Pair<BlockPos, StorageType>>()
    private val entityRefs = mutableListOf<EntityRef>()

    private enum class StorageType {
        CHEST,
        BARREL,
        ENDER_CHEST,
        FURNACE,
        BREWING_STAND,
        DISPENSER,
        HOPPER,
        SHULKER_BOX;

        fun isEnabled(): Boolean = when (this) {
            CHEST -> showChests
            BARREL -> showBarrels
            ENDER_CHEST -> showEnderChests
            FURNACE -> showFurnaces
            BREWING_STAND -> showBrewingStands
            DISPENSER -> showDispensers
            HOPPER -> showHoppers
            SHULKER_BOX -> showShulkerBoxes
        }
    }

    private data class EntityRef(val entityId: Int, val type: StorageType)

    override fun tick() {
        val level = mc.level ?: return
        val playerPos = player.blockPosition()
        val rangeSq = (range * range).toLong()

        blockPositions.clear()
        entityRefs.clear()

        val chunkRadius = (range / 16) + 1
        val playerChunkX = SectionPos.blockToSectionCoord(playerPos.x)
        val playerChunkZ = SectionPos.blockToSectionCoord(playerPos.z)

        for (cz in (playerChunkZ - chunkRadius)..(playerChunkZ + chunkRadius)) {
            for (cx in (playerChunkX - chunkRadius)..(playerChunkX + chunkRadius)) {
                val chunk = level.chunkSource.getChunkNow(cx, cz) ?: continue
                for ((pos, be) in chunk.getBlockEntities()) {
                    val type = categorizeBlockEntity(be) ?: continue
                    if (!type.isEnabled() || pos.distSqr(playerPos) > rangeSq) continue
                    blockPositions.add(pos.immutable() to type)
                }
            }
        }

        // entities with chests (boats, horses, minecarts, etc.)
        val playerPosVec = player.position()
        val aabb = AABB(playerPosVec.subtract(range.toDouble()), playerPosVec.add(range.toDouble()))
        for (entity in level.getEntities(null, aabb) { categorizeEntity(it) != null }) {
            val type = categorizeEntity(entity) ?: continue
            if (!type.isEnabled()) continue
            entityRefs.add(EntityRef(entity.id, type))
        }
    }

    override fun render3d() {
        val cameraPos = mc.entityRenderDispatcher.camera!!.position()
        val alphaColor = color

        for ((blockPos, type) in blockPositions) {
            val pos = Vec3(
                blockPos.x - cameraPos.x,
                blockPos.y - cameraPos.y,
                blockPos.z - cameraPos.z
            )
            val min = 1.0 - (1.0 * boxScale)
            val max = 1.0 * boxScale
            Renderer3d.renderBoxOutline(AABB(min, min, min, max, max, max), alphaColor, pos, lineWidth)
        }

        for (entityRef in entityRefs) {
            val entity = mc.level?.getEntity(entityRef.entityId) ?: continue
            val entityPos = Renderer3d.computeSmoothRelativeToCameraPos(
                entity.oldPosition(), entity.position(), cameraPos
            )
            val dimensions = entity.getDimensions(entity.pose).scale(boxScale)
            val w = dimensions.width / 2.0
            val h = dimensions.height.toDouble()
            Renderer3d.renderBoxOutline(AABB(-w, 0.0, -w, w, h, w), alphaColor, entityPos, lineWidth)
        }
    }

    private fun categorizeBlockEntity(blockEntity: BlockEntity?): StorageType? = when (blockEntity) {
        is ChestBlockEntity -> StorageType.CHEST
        is BarrelBlockEntity -> StorageType.BARREL
        is EnderChestBlockEntity -> StorageType.ENDER_CHEST
        is AbstractFurnaceBlockEntity -> StorageType.FURNACE
        is BrewingStandBlockEntity -> StorageType.BREWING_STAND
        is DispenserBlockEntity -> StorageType.DISPENSER
        is CrafterBlockEntity -> StorageType.DISPENSER
        is HopperBlockEntity -> StorageType.HOPPER
        is ShulkerBoxBlockEntity -> StorageType.SHULKER_BOX
        else -> null
    }

    private fun categorizeEntity(entity: Entity?): StorageType? = when (entity) {
        // Must check MinecartHopper BEFORE AbstractMinecartContainer (MinecartHopper extends it)
        is MinecartHopper -> StorageType.HOPPER
        is AbstractMinecartContainer -> StorageType.CHEST
        is ChestBoat -> StorageType.CHEST
        is ChestRaft -> StorageType.CHEST
        is AbstractChestedHorse -> StorageType.CHEST.takeIf { entity.hasChest() }
        else -> null
    }
}
