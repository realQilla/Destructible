package net.qilla.destructible.player;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.qilla.destructible.data.registry.DPlayerDataRegistry;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.mining.block.BlockMemory;
import net.qilla.destructible.mining.logic.MiningManager;
import net.qilla.qlibrary.util.tools.CoordUtil;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerPacketListener {

    private static PlayerPacketListener INSTANCE;
    private static final Map<Long, Map<Integer, BlockMemory>> BLOCK_MEMORY_MAP = DRegistry.LOADED_BLOCK_MEMORY;

    public static PlayerPacketListener getInstance() {
        if(INSTANCE == null) INSTANCE = new PlayerPacketListener();
        return INSTANCE;
    }

    private PlayerPacketListener() {
    }

    public void addListener(@NotNull Player player) {
        Preconditions.checkNotNull(player, "Player cannot be null");

        ChannelDuplexHandler handler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext context, Object object) throws Exception {
                Packet<?> packet = (Packet<?>) object;
                if(packetLogic(packet, player)) return;
                super.channelRead(context, object);
            }
        };

        ServerGamePacketListenerImpl playerCon = ((CraftPlayer) player).getHandle().connection;
        Channel channel = playerCon.connection.channel;

        channel.pipeline().addBefore("packet_handler", player.getUniqueId() + "destructible", handler);
    }

    private boolean packetLogic(Packet<?> packet, Player player) {
        DPlayerData playerData = DPlayerDataRegistry.getInstance().getData(player);
        MiningManager miningManager = playerData.getMiningManager();

        if(packet instanceof ServerboundPlayerActionPacket actionPacket) {
            switch(actionPacket.getAction()) {
                case ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK -> miningManager.init(actionPacket.getPos(), actionPacket.getDirection());
                default -> miningManager.stop();
            }
        } else if(packet instanceof ServerboundSwingPacket swingPacket) {
            miningManager.tickBlock(swingPacket.getHand());
        } else if(packet instanceof ServerboundUseItemOnPacket usePacket) {
            BlockPos blockPos = usePacket.getHitResult().getBlockPos();
            long chunkKey = CoordUtil.getChunkKey(blockPos);
            int subChunkKey = CoordUtil.getSubChunkKey(blockPos);

            return BLOCK_MEMORY_MAP.computeIfAbsent(chunkKey, v ->
                    new ConcurrentHashMap<>()).computeIfAbsent(subChunkKey, v ->
                    new BlockMemory()).isOnCooldown();
        } else if(packet instanceof ServerboundSignUpdatePacket signPacket) {
            return playerData.fulfillInput(signPacket.getLines()[0]);
        }
        return false;
    }

    public void removeListener(@NotNull Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        ServerGamePacketListenerImpl playerCon = craftPlayer.getHandle().connection;
        Channel channel = playerCon.connection.channel;

        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(player.getUniqueId() + "destructible");
            return null;
        });

    }
}