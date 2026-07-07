package nieboczek.lifestolen.module

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.inventory.InventoryMenu

object AutoTotemModule : Module("AutoTotem", Category.COMBAT) {
    private val healthThreshold by int("Health Threshold", 12, 0..40)
    private val switchDelay by int("Switch Delay", 50, 0..500, "ms")

    private var lastSwitchTime = 0L

    override fun tick() {
        val player = player
        if (player.isCreative || player.isSpectator || player.isDeadOrDying) return
        if (player.offhandItem.has(DataComponents.DEATH_PROTECTION)) return

        val health = player.health + player.absorptionAmount
        if (health > healthThreshold) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSwitchTime < switchDelay) return
        lastSwitchTime = currentTime

        val totemSlot = findTotemSlot() ?: return
        moveToOffhand(totemSlot)
    }

    private fun findTotemSlot(): Int? {
        val inventory = player.inventory
        for (i in 0 until Inventory.SELECTION_SIZE) {
            if (inventory.getItem(i).has(DataComponents.DEATH_PROTECTION)) return i
        }
        for (i in Inventory.SELECTION_SIZE until Inventory.INVENTORY_SIZE) {
            if (inventory.getItem(i).has(DataComponents.DEATH_PROTECTION)) return i
        }
        return null
    }

    private fun moveToOffhand(slot: Int) {
        val connection = player.connection

        if (slot < Inventory.SELECTION_SIZE) {
            val originalSlot = player.inventory.selectedSlot
            connection.send(ServerboundSetCarriedItemPacket(slot))
            connection.send(
                ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                    BlockPos.ZERO, Direction.DOWN
                )
            )
            if (originalSlot != slot) {
                connection.send(ServerboundSetCarriedItemPacket(originalSlot))
            }
        } else {
            if (player.containerMenu.containerId != 0) return

            val gameMode = mc.gameMode ?: return
            val containerId = player.containerMenu.containerId
            val offhandEmpty = player.offhandItem.isEmpty

            gameMode.handleContainerInput(containerId, slot, 0, ContainerInput.PICKUP, player)
            gameMode.handleContainerInput(containerId, InventoryMenu.SHIELD_SLOT, 0, ContainerInput.PICKUP, player)
            if (!offhandEmpty) {
                gameMode.handleContainerInput(containerId, slot, 0, ContainerInput.PICKUP, player)
            }
        }
    }
}
