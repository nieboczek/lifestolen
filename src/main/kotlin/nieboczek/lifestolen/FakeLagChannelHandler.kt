package nieboczek.lifestolen

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.PacketType
import net.minecraft.network.protocol.common.CommonPacketTypes
import net.minecraft.network.protocol.configuration.ConfigurationPacketTypes
import net.minecraft.network.protocol.game.GamePacketTypes
import net.minecraft.network.protocol.handshake.HandshakePacketTypes
import net.minecraft.network.protocol.ping.PingPacketTypes
import net.minecraft.network.protocol.status.StatusPacketTypes
import nieboczek.lifestolen.module.FakeLagModule
import java.util.*

class FakeLagChannelHandler : ChannelDuplexHandler() {
    companion object {
        val flushOnTypes: Set<PacketType<*>> = setOf(
            GamePacketTypes.SERVERBOUND_INTERACT,
            GamePacketTypes.SERVERBOUND_SWING,
            GamePacketTypes.SERVERBOUND_USE_ITEM_ON,
            GamePacketTypes.SERVERBOUND_PLAYER_ACTION,
            GamePacketTypes.CLIENTBOUND_SET_HEALTH,
            GamePacketTypes.CLIENTBOUND_PLAYER_POSITION,
            GamePacketTypes.CLIENTBOUND_LOGIN,
            GamePacketTypes.CLIENTBOUND_RESPAWN,
            CommonPacketTypes.CLIENTBOUND_DISCONNECT
        )

        val passOnTypes: Set<PacketType<*>> = setOf(
            HandshakePacketTypes.CLIENT_INTENTION,
            StatusPacketTypes.SERVERBOUND_STATUS_REQUEST,
            PingPacketTypes.SERVERBOUND_PING_REQUEST,
            GamePacketTypes.SERVERBOUND_CHAT,
            GamePacketTypes.CLIENTBOUND_SYSTEM_CHAT,
            GamePacketTypes.CLIENTBOUND_DISGUISED_CHAT,
            GamePacketTypes.SERVERBOUND_CHAT_COMMAND,
            GamePacketTypes.CLIENTBOUND_SOUND,
            ConfigurationPacketTypes.SERVERBOUND_FINISH_CONFIGURATION,
            ConfigurationPacketTypes.CLIENTBOUND_FINISH_CONFIGURATION
        )
    }

    private val packetQueue = ArrayDeque<QueuedPacket>()
    private val random = Random()
    private var lastFlushTime: Long = 0
    private var currentDelay = 0

    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        if (msg !is Packet<*> || !FakeLagModule.INSTANCE.isEnabled) {
            super.write(ctx, msg, promise)
            return
        }

        val cfg = FakeLagModule.INSTANCE.cfg
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastFlushTime < cfg.recoilTimeMs) {
            super.write(ctx, msg, promise)
            return
        }

        if (passOnTypes.contains(msg.type())) {
            super.write(ctx, msg, promise)
            return
        }

        if (flushOnTypes.contains(msg.type())) {
            flushQueue(msg, ctx, promise)
            return
        }

        val firstPacket = packetQueue.peekFirst()
        if (firstPacket != null && currentTime - firstPacket.timestamp > currentDelay) {
            currentDelay = random.nextInt(cfg.delayMsMin, cfg.delayMsMax + 1)
            flushQueue(msg, ctx, promise)
            return
        }

        packetQueue.add(QueuedPacket(msg, currentTime))
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        super.channelRead(ctx, msg)
    }

    private fun flushQueue(packet: Packet<*>, ctx: ChannelHandlerContext, promise: ChannelPromise) {
        lastFlushTime = System.currentTimeMillis()
        packetQueue.add(QueuedPacket(packet, lastFlushTime))

        while (!packetQueue.isEmpty()) {
            val queued = packetQueue.poll()
            if (queued != null) {
                ctx.write(queued.packet, promise)
            }
        }
    }

    private data class QueuedPacket(val packet: Packet<*>, val timestamp: Long)
}
