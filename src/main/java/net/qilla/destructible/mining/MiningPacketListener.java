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
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.ChunkPos;
import net.qilla.destructible.data.BlockMemory;
import net.qilla.destructible.data.DestructibleRegistry;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.util.CoordUtil;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public final class MiningPacketListener {
    private final Destructible plugin;

    public MiningPacketListener(final Destructible plugin) {
        this.plugin = plugin;
    }

    public void addListener(final Player player, final MiningCore miningCore) {
        ChannelDuplexHandler handler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext context, Object object) throws Exception {
                Packet<?> packet = (Packet<?>) object;

                if(packet instanceof ServerboundPlayerActionPacket actionPacket) {
                    switch(actionPacket.getAction()) {
                        case ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK -> miningCore.init(actionPacket.getPos(), actionPacket.getDirection());
                        case ServerboundPlayerActionPacket.Action.DROP_ITEM,
                             ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS -> miningCore.stop();
                        default -> miningCore.stop();
                    }
                } else if(packet instanceof ServerboundSwingPacket swingPacket) {
                    miningCore.tickBlock(swingPacket.getHand());
                }
                if(packet instanceof ServerboundUseItemOnPacket usePacket) {
                    BlockPos blockPos = usePacket.getHitResult().getBlockPos();
                    ChunkPos chunkPos = new ChunkPos(blockPos);
                    int chunkInt = CoordUtil.posToChunkLocalPos(blockPos);
                    if(Registries.DESTRUCTIBLE_BLOCK_DATA.computeIfAbsent(chunkPos, v ->
                            new DestructibleRegistry<>()).computeIfAbsent(chunkInt, v ->
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

    public void removeListener(final Player player) {
        ServerGamePacketListenerImpl playerCon = ((CraftPlayer) player).getHandle().connection;
        Channel channel = playerCon.connection.channel;

        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(player.getName());
            return null;
        });
    }
}