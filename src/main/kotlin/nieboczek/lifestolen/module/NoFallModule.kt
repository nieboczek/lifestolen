package nieboczek.lifestolen.module

import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket

object NoFallModule : Module("NoFall", Category.MOVEMENT) {
    const val MAX_FALL_DISTANCE = 1.5f

    fun handlePacket(packet: Packet<*>) {
        if (enabled && packet is ServerboundMovePlayerPacket && player.fallDistance >= MAX_FALL_DISTANCE) {
            packet.onGround = true
            player.resetFallDistance()
        }
    }
}
