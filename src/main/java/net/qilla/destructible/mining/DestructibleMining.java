package net.qilla.destructible.mining;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.block.DBlocks;
import net.qilla.destructible.mining.item.tool.DTool;
import net.qilla.destructible.mining.item.tool.DToolType;
import net.qilla.destructible.mining.player.data.MineData;
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

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DestructibleMining {
    private static final Destructible PLUGIN = Destructible.getInstance();
    private static final Logger LOGGER = PLUGIN.getLogger();
    private static final int ITEM_DELETE_DELAY = 8;
    private static final int ITEM_MAGNET_DELAY = 4;

    public static void init(@NotNull final PlayerData playerData, @NotNull final ServerboundPlayerActionPacket actionPacket) {
        Player player = playerData.getPlayer();
        MineData mineData = playerData.getMiningData();
        Location location = BlockUtil.blockPosToLoc(actionPacket.getPos(), player.getWorld());

        if(mineData == null || mineData.getLocation().hashCode() != (location.hashCode())) {
            playerData.setMiningData(new MineData(playerData.getEquipment(), location, actionPacket.getDirection()));
        }
    }

    public static void tickBlock(@NotNull final PlayerData playerData) {
        MineData mineData = playerData.getMiningData();

        if(mineData == null || mineData.getDBlock() == DBlocks.NONE) return;
        if(mineData.getDBlock().getDurability() < 0) return;

        Bukkit.getScheduler().runTask(PLUGIN, () -> {
            Player player = playerData.getPlayer();

            try {
                ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
                ServerLevel level = serverPlayer.serverLevel();
                Location location = mineData.getLocation();
                BlockPos blockPos = BlockUtil.locToBlockPos(location);
                DBlock dBlock = mineData.getDBlock();
                DTool dTool = mineData.updateTool();

                if(dBlock.getStrengthRequirement() > dTool.getStrength()) return;
                if(Arrays.stream(dBlock.getProperTools()).noneMatch(properTool -> properTool.equals(dTool.getToolType()) || properTool.equals(DToolType.ALL)))
                    return;

                if(mineData.damage(dTool.getEfficiency())) {
                    Vec3 midFace = BlockUtil.getMiddleFace(mineData.getDirection());

                    level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundBlockDestructionPacket(location.hashCode(), BlockUtil.locToBlockPos(location), 10));
                    location.getWorld().playSound(location, dBlock.getSound(), 1, 1);
                    location.getWorld().spawnParticle(Particle.BLOCK, location.clone().add(0.5, 0.5, 0.5), 50, 0.25, 0.25, 0.25, 0, dBlock.getParticle().createBlockData());
                    location.getWorld().getBlockAt(location).setType(Material.COBBLESTONE);

                    ItemStack[] items = ItemUtil.rollItemDrops(dBlock.getItemDrops());

                    Thread thread = new Thread(() -> {
                        for(ItemStack item : items) {
                            Vec3 vec3 = mineData.getDirection().getUnitVec3().offsetRandom(RandomSource.create(), 1.2f);
                            ItemEntity itemEntity = new ItemEntity(
                                    level,
                                    blockPos.getX() + 0.5 + midFace.x,
                                    blockPos.getY() + 0.5 + midFace.y,
                                    blockPos.getZ() + 0.5 + midFace.z,
                                    ((CraftItemStack) item).handle,
                                    vec3.x * 0.3,
                                    vec3.y * 0.3,
                                    vec3.z * 0.3
                            );

                            //itemEntity.setNoGravity(true);

                            Bukkit.getScheduler().runTask(Destructible.getInstance(), () -> {
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

                                level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundSetEntityMotionPacket(
                                        itemEntity.getId(),
                                        itemEntity.getDeltaMovement()));

                                Bukkit.getScheduler().runTaskLater(Destructible.getInstance(), () -> {
                                    level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundTakeItemEntityPacket(
                                            itemEntity.getId(),
                                            serverPlayer.getId(),
                                            item.getAmount()
                                    ));

                                }, ITEM_MAGNET_DELAY);

                                ItemUtil.give(player, item);
                            });
                            try {
                                Thread.sleep(ITEM_DELETE_DELAY * 50);
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

                    mineData.updateBlock();
                } else {
                    level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundBlockDestructionPacket(location.hashCode(), blockPos, mineData.getCrackStage()));
                }
            } catch(Exception e) {
                LOGGER.log(Level.SEVERE, "There was an error in ticking a players actively mined block.");
            }
        });
    }

    public static void stop(@NotNull final PlayerData playerData) {
        try {
            MineData mineData = playerData.getMiningData();

            if(mineData == null) return;

            Player player = playerData.getPlayer();
            ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
            ServerLevel level = (ServerLevel) nmsPlayer.level();
            Location location = mineData.getLocation();
            BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());

            level.getChunkSource().broadcastAndSend(nmsPlayer, new ClientboundBlockDestructionPacket(location.hashCode(), blockPos, 10));

            playerData.setMiningData(null);
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, "There was an error flushing a player's mining data.");
        }
    }
}