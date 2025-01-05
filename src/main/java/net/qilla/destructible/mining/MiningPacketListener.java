package net.qilla.destructible.mining;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.qilla.destructible.data.ChunkPos;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.mining.block.BlockMemory;
import net.qilla.destructible.data.RegistryMap;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.logic.MiningManager;
import net.qilla.destructible.util.CoordUtil;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public final class MiningPacketListener {

    public void addListener(DPlayer dPlayer) {
        Preconditions.checkNotNull(dPlayer, "DPlayer cannot be null");
        ChannelDuplexHandler handler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext context, Object object) throws Exception {
                Packet<?> packet = (Packet<?>) object;
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
                    ChunkPos chunkPos = new ChunkPos(blockPos);
                    int chunkInt = CoordUtil.posToChunkLocalPos(blockPos);
                    if(Registries.DESTRUCTIBLE_BLOCK_DATA.computeIfAbsent(chunkPos, v ->
                            new RegistryMap<>()).computeIfAbsent(chunkInt, v ->
                            new BlockMemory()).isOnCooldown()) {
                        return;
                    }
                } else if(packet instanceof ServerboundSignUpdatePacket signPacket) {
                    dPlayer.getMenuData().setSignText(signPacket.getLines()[0]);
                }
                super.channelRead(context, object);
            }
        };

        ServerGamePacketListenerImpl playerCon = dPlayer.getServerPlayer().connection;
        Channel channel = playerCon.connection.channel;

        channel.pipeline().addBefore("packet_handler", dPlayer.getCraftPlayer().getUniqueId().toString(), handler);
    }

    public void removeListener(@NotNull DPlayer dPlayer) {
        ServerGamePacketListenerImpl playerCon = dPlayer.getServerPlayer().connection;
        Channel channel = playerCon.connection.channel;

        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(dPlayer.getCraftPlayer().getUniqueId().toString());
            return null;
        });

    }
}