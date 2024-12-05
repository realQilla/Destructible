package net.qilla.destructible.mining;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.mining.player.data.InstancePlayerData;
import net.qilla.destructible.mining.player.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public final class PlayerPacketListener {

    private final DestructibleMining miningData;
    private final InstancePlayerData instancePlayerData;

    public PlayerPacketListener(final DestructibleMining miningData, final InstancePlayerData instancePlayerData) {
        this.miningData = miningData;
        this.instancePlayerData = instancePlayerData;
    }

    public void addListener(final Player player) {
        ChannelDuplexHandler handler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
                if(packet instanceof ServerboundPlayerActionPacket actionPacket) {
                    final PlayerData playerData = instancePlayerData.getPlayerData(player);

                    switch(actionPacket.getAction()) {
                        case ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK -> {
                                miningData.init(playerData, actionPacket);
                        }
                        default-> {
                            miningData.stop(playerData);
                        }
                    }
                } else if(packet instanceof ServerboundSwingPacket swingPacket) {
                    if(!swingPacket.getHand().equals(InteractionHand.MAIN_HAND)) return;
                    final PlayerData playerData = instancePlayerData.getPlayerData(player);

                    Bukkit.getScheduler().runTask(Destructible.getInstance(), () -> {
                        miningData.tick(playerData, swingPacket);
                    });
                }
                super.channelRead(context, packet);
            }
        };

        ServerGamePacketListenerImpl playerCon = ((CraftPlayer) player).getHandle().connection;
        ChannelPipeline pipeline = playerCon.connection.channel.pipeline();

        pipeline.addBefore("packet_handler", player.getName(), handler);
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