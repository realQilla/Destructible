package net.qilla.destructible.mining;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.qilla.destructible.mining.customblock.DestructibleBlock;
import net.qilla.destructible.player.data.MiningData;
import net.qilla.destructible.player.data.PlayerData;
import net.qilla.destructible.util.GivePlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public final class DestructibleMining {

    public void init(final PlayerData playerData, final ServerboundPlayerActionPacket actionPacket) {
        final MiningData miningData = playerData.getMiningData();
        final BlockPos blockPos = actionPacket.getPos();
        final Location location = new Location(playerData.getPlayer().getWorld(), blockPos.getX(), blockPos.getY(), blockPos.getZ());

        if(miningData == null || miningData.getLocation().hashCode() != location.hashCode()) {
            playerData.setMiningData(new MiningData(location));
        }
    }

    public void tick(final PlayerData playerData, final ServerboundSwingPacket swingPacket) {
        final Player player = playerData.getPlayer();
        final MiningData miningData = playerData.getMiningData();

        if(miningData == null || miningData.getDestructibleBlock() == null) return;
        if(miningData.getDestructibleBlock().getDurability() < 0) return;

        final ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        final Location location = miningData.getLocation();
        final DestructibleBlock destructibleBlock = miningData.getDestructibleBlock();

        if(miningData.damage(1)) {
            ((ServerLevel) nmsPlayer.level()).getChunkSource().broadcastAndSend(nmsPlayer, new ClientboundBlockDestructionPacket(location.hashCode(), new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()), 10));
            location.getWorld().playSound(location, destructibleBlock.getSound(), 1, 1);
            location.getWorld().spawnParticle(Particle.BLOCK, location.clone().add(0.5, 0.5, 0.5), 50, 0.25, 0.25, 0.25, 0, destructibleBlock.getBlockParticle().createBlockData());
            location.getWorld().getBlockAt(location).setType(Material.COBBLESTONE);
            GivePlayer.item(player, destructibleBlock.getItemDrops());
            miningData.updateBlock();
        } else {
            ((ServerLevel) nmsPlayer.level()).getChunkSource().broadcastAndSend(nmsPlayer, new ClientboundBlockDestructionPacket(location.hashCode(), new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()), miningData.getIncrementProgress()));
        }
    }

    public void stop(final PlayerData playerData) {
        final MiningData miningData = playerData.getMiningData();

        if(miningData == null) return;

        final Player player = playerData.getPlayer();
        final ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        final Location location = miningData.getLocation();

        final ServerGamePacketListenerImpl connection = nmsPlayer.connection;
        connection.send(new ClientboundBlockDestructionPacket(location.hashCode(), new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()), 10));
        ((ServerLevel) nmsPlayer.level()).getChunkSource().broadcastAndSend(nmsPlayer, new ClientboundBlockDestructionPacket(location.hashCode(), new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()), 10));


        playerData.setMiningData(null);
    }
}