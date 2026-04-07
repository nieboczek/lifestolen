package nieboczek.lifestolen;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.CommonPacketTypes;
import net.minecraft.network.protocol.configuration.ConfigurationPacketTypes;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.handshake.HandshakePacketTypes;
import net.minecraft.network.protocol.ping.PingPacketTypes;
import net.minecraft.network.protocol.status.StatusPacketTypes;
import nieboczek.lifestolen.module.FakeLagModule;

import java.util.ArrayDeque;
import java.util.Random;
import java.util.Set;

public class FakeLagChannelHandler extends ChannelDuplexHandler {
    private static final Set<PacketType<?>> FLUSH_ON_TYPES = Set.of(
            GamePacketTypes.SERVERBOUND_INTERACT,
            GamePacketTypes.SERVERBOUND_SWING,
            GamePacketTypes.SERVERBOUND_USE_ITEM_ON,
            GamePacketTypes.SERVERBOUND_PLAYER_ACTION,
            GamePacketTypes.CLIENTBOUND_SET_HEALTH,
            GamePacketTypes.CLIENTBOUND_PLAYER_POSITION,
            GamePacketTypes.CLIENTBOUND_LOGIN,
            GamePacketTypes.CLIENTBOUND_RESPAWN,
            CommonPacketTypes.CLIENTBOUND_DISCONNECT
    );
    private static final Set<PacketType<?>> PASS_ON_TYPES = Set.of(
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
    );

    private final ArrayDeque<QueuedPacket> packetQueue = new ArrayDeque<>();
    private final Random random = new Random();
    private long lastFlushTime = 0;
    private int currentDelay = 0;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof Packet<?> packet) || !FakeLagModule.INSTANCE.enabled) {
            super.write(ctx, msg, promise);
            return;
        }

        FakeLagModule.Config cfg = FakeLagModule.INSTANCE.cfg;
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastFlushTime < cfg.recoilTimeMs) {
            super.write(ctx, msg, promise);
            return;
        }

        if (PASS_ON_TYPES.contains(packet.type())) {
            super.write(ctx, msg, promise);
            return;
        }

        if (FLUSH_ON_TYPES.contains(packet.type())) {
            flushQueue(packet, ctx, promise);
            return;
        }

        QueuedPacket firstPacket = packetQueue.peekFirst();
        if (firstPacket != null && currentTime - firstPacket.timestamp > currentDelay) {
            currentDelay = random.nextInt(cfg.delayMsMin, cfg.delayMsMax + 1);
            flushQueue(packet, ctx, promise);
            return;
        }

        packetQueue.add(new QueuedPacket(packet, currentTime));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    private void flushQueue(Packet<?> packet, ChannelHandlerContext ctx, ChannelPromise promise) {
        lastFlushTime = System.currentTimeMillis();
        packetQueue.add(new QueuedPacket(packet, lastFlushTime));

        while (!packetQueue.isEmpty()) {
            QueuedPacket queued = packetQueue.poll();
            if (queued != null) {
                ctx.write(queued.packet, promise);
            }
        }
    }

    private record QueuedPacket(Packet<?> packet, long timestamp) {
    }
}
