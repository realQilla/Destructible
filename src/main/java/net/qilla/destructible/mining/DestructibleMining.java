package net.qilla.destructible.mining;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.mining.block.DestructibleBlock;
import net.qilla.destructible.mining.item.ItemDrop;
import net.qilla.destructible.mining.player.data.MiningData;
import net.qilla.destructible.mining.player.data.PlayerData;
import net.qilla.destructible.util.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public final class DestructibleMining {

    private final Destructible plugin = Destructible.getInstance();

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

        final ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        final Location location = miningData.getLocation();
        final DestructibleBlock destructibleBlock = miningData.getDestructibleBlock();

        final BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        final ServerLevel level = serverPlayer.serverLevel();

        if(miningData.damage(1)) {
            level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundBlockDestructionPacket(blockPos.hashCode(), blockPos, 10));
            location.getWorld().playSound(location, destructibleBlock.getSound(), 1, 1);
            location.getWorld().spawnParticle(Particle.BLOCK, location.clone().add(0.5, 0.5, 0.5), 50, 0.25, 0.25, 0.25, 0, destructibleBlock.getBlockParticle().createBlockData());
            location.getWorld().getBlockAt(location).setType(Material.COBBLESTONE);

            ItemStack[] items = ItemManager.pullItem(destructibleBlock.getItemDrops());

            Thread thread = new Thread(() -> {
                for(ItemStack item : items) {
                    final ItemEntity itemEntity = new ItemEntity(level, blockPos.getCenter().x, blockPos.getCenter().y + 0.33, blockPos.getCenter().z, ((CraftItemStack) item).handle);

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundAddEntityPacket(itemEntity.getId(), itemEntity.getUUID(), itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), 0, 0, itemEntity.getType(), 0, new Vec3(0, 0, 0), 0));
                        level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundSetEntityDataPacket(itemEntity.getId(), itemEntity.getEntityData().packAll()));
                        level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundTakeItemEntityPacket(itemEntity.getId(), serverPlayer.getId(), item.getAmount()));
                        ItemManager.give(player, item);
                    });
                    try {
                        Thread.sleep(250);
                    } catch(InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                Thread.currentThread().interrupt();
            });

            thread.start();

            miningData.updateBlock();
        } else {
            level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundBlockDestructionPacket(blockPos.hashCode(), blockPos, miningData.getIncrementProgress()));
        }
    }

    public void stop(final PlayerData playerData) {
        final MiningData miningData = playerData.getMiningData();

        if(miningData == null) return;

        final Player player = playerData.getPlayer();
        final ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        final ServerLevel level = (ServerLevel) nmsPlayer.level();
        final Location location = miningData.getLocation();
        final BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        level.getChunkSource().broadcastAndSend(nmsPlayer, new ClientboundBlockDestructionPacket(blockPos.hashCode(), blockPos, 10));


        playerData.setMiningData(null);
    }
}