package nieboczek.lifestolen.module

import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import nieboczek.lifestolen.Lifestolen

object NoFallModule : Module("No Fall", Category.MOVEMENT) {
    const val MAX_FALL_DISTANCE = 1.5f

    fun handlePacket(packet: Packet<*>) {
        if (enabled && !Lifestolen.killSwitch && packet is ServerboundMovePlayerPacket && player.fallDistance >= MAX_FALL_DISTANCE) {
            packet.onGround = true
            player.resetFallDistance()
        }
    }
}
