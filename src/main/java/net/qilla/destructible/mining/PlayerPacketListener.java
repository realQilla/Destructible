package net.qilla.destructible.mining;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.player.DMiner;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public final class PlayerPacketListener {
    private final Destructible plugin;

    public PlayerPacketListener(final Destructible plugin) {
        this.plugin = plugin;
    }

    public void addListener(final Player player, final DMiner dMiner) {
        ChannelDuplexHandler handler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext context, Object object) throws Exception {
                Packet<?> packet = (Packet<?>) object;

                if(packet instanceof ServerboundPlayerActionPacket actionPacket) {
                    switch(actionPacket.getAction()) {
                        case ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK -> dMiner.init(actionPacket);
                        case ServerboundPlayerActionPacket.Action.DROP_ITEM,
                             ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS -> dMiner.stop();
                        default -> dMiner.stop();
                    }
                } else if(packet instanceof ServerboundSwingPacket swingPacket) {
                    dMiner.tickBlock(swingPacket);
                }
                super.channelRead(context, object);
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