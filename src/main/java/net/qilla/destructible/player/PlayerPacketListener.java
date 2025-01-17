package net.qilla.destructible.player;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.qilla.destructible.mining.block.BlockMemory;
import net.qilla.destructible.data.DRegistry;
import net.qilla.destructible.mining.logic.MiningManager;
import net.qilla.destructible.util.CoordUtil;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

public final class PlayerPacketListener {

    public void addListener(@NotNull DPlayer dPlayer) {
        Preconditions.checkNotNull(dPlayer, "DPlayer cannot be null");
        ChannelDuplexHandler handler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext context, Object object) throws Exception {
                Packet<?> packet = (Packet<?>) object;
                if(packetLogic(packet, dPlayer)) return;
                super.channelRead(context, object);
            }

            @Override
            public void write(ChannelHandlerContext context, Object object, io.netty.channel.ChannelPromise promise) throws Exception {
                super.write(context, object, promise);
            }
        };

        ServerGamePacketListenerImpl playerCon = dPlayer.getServerPlayer().connection;
        Channel channel = playerCon.connection.channel;

        channel.pipeline().addBefore("packet_handler", dPlayer.getCraftPlayer().getUniqueId().toString(), handler);
    }

    private boolean packetLogic(Packet<?> packet, DPlayer dPlayer) {
        MiningManager miningManager = dPlayer.getMinerData();

        if(packet instanceof ServerboundPlayerActionPacket actionPacket) {
            switch(actionPacket.getAction()) {
                case ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK ->
                        miningManager.init(actionPacket.getPos(), actionPacket.getDirection());
                default -> miningManager.stop();
            }
        } else if(packet instanceof ServerboundSwingPacket swingPacket) {
            miningManager.tickBlock(swingPacket.getHand());
        } else if(packet instanceof ServerboundUseItemOnPacket usePacket) {
            BlockPos blockPos = usePacket.getHitResult().getBlockPos();
            long chunkKey = CoordUtil.getChunkKey(blockPos);
            int chunkInt = CoordUtil.getBlockIndexInChunk(blockPos);
            if(DRegistry.DESTRUCTIBLE_BLOCK_DATA.computeIfAbsent(chunkKey, v ->
                    new ConcurrentHashMap<>()).computeIfAbsent(chunkInt, v ->
                    new BlockMemory()).isOnCooldown()) {
                return true;
            }
        } else if(packet instanceof ServerboundSignUpdatePacket signPacket) {
            return dPlayer.getMenuHolder().fulfillInput(signPacket.getLines()[0]);
        }
        return false;
    }

    public void removeListener(DPlayer dPlayer) {
        ServerGamePacketListenerImpl playerCon = dPlayer.getServerPlayer().connection;
        Channel channel = playerCon.connection.channel;

        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(dPlayer.getCraftPlayer().getUniqueId().toString());
            return null;
        });

    }
}