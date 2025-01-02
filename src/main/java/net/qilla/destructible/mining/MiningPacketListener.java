package net.qilla.destructible.mining;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
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

public final class MiningPacketListener {

    public MiningPacketListener() {
    }

    public void addListener(@NotNull Player player, @NotNull DPlayer dPlayer) {
        ChannelDuplexHandler handler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext context, Object object) throws Exception {
                Packet<?> packet = (Packet<?>) object;
                MiningManager miningManager = dPlayer.getMinerData();

                if(packet instanceof ServerboundPlayerActionPacket actionPacket) {
                    switch(actionPacket.getAction()) {
                        case ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK -> miningManager.init(actionPacket.getPos(), actionPacket.getDirection());
                        case ServerboundPlayerActionPacket.Action.DROP_ITEM,
                             ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS -> miningManager.stop();
                        default -> miningManager.stop();
                    }
                } else if(packet instanceof ServerboundSwingPacket swingPacket) {
                    miningManager.tickBlock(swingPacket.getHand());
                }
                if(packet instanceof ServerboundUseItemOnPacket usePacket) {
                    BlockPos blockPos = usePacket.getHitResult().getBlockPos();
                    ChunkPos chunkPos = new ChunkPos(blockPos);
                    int chunkInt = CoordUtil.posToChunkLocalPos(blockPos);
                    if(Registries.DESTRUCTIBLE_BLOCK_DATA.computeIfAbsent(chunkPos, v ->
                            new RegistryMap<>()).computeIfAbsent(chunkInt, v ->
                            new BlockMemory()).isOnCooldown()) {
                        return;
                    }
                }
                super.channelRead(context, object);
            }
        };

        ServerGamePacketListenerImpl playerCon = ((CraftPlayer) player).getHandle().connection;
        Channel channel = playerCon.connection.channel;

        channel.pipeline().addBefore("packet_handler", player.getName(), handler);
    }

    public void removeListener(@NotNull Player player) {
        ServerGamePacketListenerImpl playerCon = ((CraftPlayer) player).getHandle().connection;
        Channel channel = playerCon.connection.channel;

        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(player.getName());
            return null;
        });
    }
}