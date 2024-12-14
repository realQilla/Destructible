package net.qilla.destructible.mining;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.mining.block.DestructibleBlock;
import net.qilla.destructible.mining.player.data.MiningData;
import net.qilla.destructible.mining.player.data.PlayerData;
import net.qilla.destructible.util.BlockUtil;
import net.qilla.destructible.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class DestructibleMining {

    private final Destructible plugin = Destructible.getInstance();

    public void init(@NotNull final PlayerData playerData, @NotNull final ServerboundPlayerActionPacket actionPacket) {
        MiningData miningData = playerData.getMiningData();
        BlockPos blockPos = actionPacket.getPos();
        Location location = BlockUtil.blockPosToLoc(blockPos, playerData.getPlayer().getWorld());

        if(miningData == null || !miningData.getLocation().equals(location)) {
            playerData.setMiningData(new MiningData(location, actionPacket.getDirection()));
        }
    }

    public void tick(@NotNull final PlayerData playerData, @NotNull final ServerboundSwingPacket swingPacket) {
        Player player = playerData.getPlayer();
        MiningData miningData = playerData.getMiningData();

        if(miningData == null || miningData.getDestructibleBlock() == null) return;
        if(miningData.getDestructibleBlock().getDurability() < 0) return;

        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ServerLevel level = serverPlayer.serverLevel();
        Location location = miningData.getLocation();
        BlockPos blockPos = BlockUtil.locToBlockPos(location);
        DestructibleBlock destructibleBlock = miningData.getDestructibleBlock();

        if(miningData.damage(1)) {
            Vec3 midFace = BlockUtil.getMiddleFace(miningData.getDirection());

            level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundBlockDestructionPacket(location.hashCode(), BlockUtil.locToBlockPos(location), 10));
            location.getWorld().playSound(location, destructibleBlock.getSound(), 1, 1);
            location.getWorld().spawnParticle(Particle.BLOCK, location.clone().add(0.5, 0.5, 0.5), 50, 0.25, 0.25, 0.25, 0, destructibleBlock.getBlockParticle().createBlockData());
            location.getWorld().getBlockAt(location).setType(Material.COBBLESTONE);

            ItemStack[] items = ItemUtil.rollItemDrops(destructibleBlock.getItemDrops());

            Thread thread = new Thread(() -> {
                for(ItemStack item : items) {
                    Vec3 vec3 = miningData.getDirection().getUnitVec3().offsetRandom(RandomSource.create(), 1.2f);
                    ItemEntity itemEntity = new ItemEntity(
                            level,
                            blockPos.getX() + 0.5 + midFace.x,
                            blockPos.getY() + 0.5 + midFace.y, blockPos.getZ() + 0.5 + midFace.z,
                            ((CraftItemStack) item).handle,
                            vec3.x * 0.30,
                            vec3.y * 0.30,
                            vec3.z * 0.30
                    );
                    itemEntity.setNoGravity(true);

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundAddEntityPacket(
                                itemEntity.getId(),
                                itemEntity.getUUID(),
                                itemEntity.getX(),
                                itemEntity.getY(),
                                itemEntity.getZ(),
                                0,
                                0,
                                itemEntity.getType(),
                                0,
                                itemEntity.getDeltaMovement(),
                                0
                        ));
                        level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundSetEntityDataPacket(
                                itemEntity.getId(),
                                itemEntity.getEntityData().packAll()
                        ));

                        level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundSetEntityMotionPacket(itemEntity.getId(), itemEntity.getDeltaMovement()));

                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundTakeItemEntityPacket(
                                    itemEntity.getId(),
                                    serverPlayer.getId(),
                                    item.getAmount()
                            ));

                        },4);

                        ItemUtil.give(player, item);
                    });
                    try {
                        Thread.sleep(333);
                    } catch(InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if(itemEntity.isAlive()) {
                        level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundRemoveEntitiesPacket(itemEntity.getId()));
                    }
                }
                Thread.currentThread().interrupt();
            });

            thread.start();

            miningData.updateBlock();
        } else {
            level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundBlockDestructionPacket(location.hashCode(), blockPos, miningData.getIncrementProgress()));
        }
    }

    public void stop(@NotNull final PlayerData playerData) {
        MiningData miningData = playerData.getMiningData();

        if(miningData == null) return;

        Player player = playerData.getPlayer();
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        ServerLevel level = (ServerLevel) nmsPlayer.level();
        Location location = miningData.getLocation();
        BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        level.getChunkSource().broadcastAndSend(nmsPlayer, new ClientboundBlockDestructionPacket(location.hashCode(), blockPos, 10));


        playerData.setMiningData(null);
    }
}