package net.qilla.destructible.mining;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.mining.item.DTool;
import net.qilla.destructible.util.DBlockUtil;
import net.qilla.destructible.util.ItemUtil;
import net.qilla.destructible.util.RandomUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BlockMiner {
    private static final int ITEM_MAGNET_DELAY = 8;
    private static final int ITEM_ALTERNATE_DELAY = 5;
    private static final int ITEM_DELETE_DELAY = 4;
    private static final RandomSource RANDOM = RandomSource.createNewThreadLocalInstance();

    private final Destructible plugin;
    private final Player player;
    private final ServerLevel serverLevel;
    private final ServerPlayer serverPlayer;

    public BlockMiner(@NotNull Destructible plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.player = player;
        this.serverPlayer = ((CraftPlayer) player).getHandle();
        this.serverLevel = serverPlayer.serverLevel();
    }

    public void tickBlock(@NotNull BlockInstance blockInstance, @NotNull DTool dTool, @NotNull ToolManager toolManager) {
        blockInstance.damageBlock(dTool.getEfficiency());

        if(blockInstance.isDestroyed()) {
            blockInstance.getDBlockData().setLocked(true);
            this.destroyBlock(blockInstance);
            toolManager.damageTool(dTool, 1);
        } else {
            this.serverLevel.getChunkSource().broadcastAndSend(
                    serverPlayer, new ClientboundBlockDestructionPacket(
                            blockInstance.getBlockPos().hashCode(), blockInstance.getBlockPos(), blockInstance.getCrackLevel()));
        }
    }

    public void endProgress(@NotNull BlockInstance blockInstance) {
        this.serverLevel.getChunkSource().broadcastAndSend(serverPlayer,
                new ClientboundBlockDestructionPacket(blockInstance.getBlockPos().hashCode(), blockInstance.getBlockPos(), 10));
    }

    private void destroyBlock(@NotNull BlockInstance blockInstance) {
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            int msCooldown = RandomUtil.offset(blockInstance.getDBlock().getMsCooldown(), 2000);
            BlockState blockState = ((CraftBlockState) blockInstance.getLocation().getBlock().getState()).getHandle();
            Vec3 midFace = DBlockUtil.getCenterFaceParticle(blockInstance.getDirection());
            float[] midOffset = DBlockUtil.getFlatOffsetParticles(blockInstance.getDirection());

            this.serverLevel.getChunkSource().broadcastAndSend(
                    this.serverPlayer, new ClientboundBlockDestructionPacket(blockInstance.getBlockPos().hashCode(), blockInstance.getBlockPos(), 10));
            blockInstance.getLocation().getWorld().playSound(blockInstance.getLocation(), blockInstance.getDBlock().getSound(), 1, (float) RandomUtil.between(0.75, 1.25));
            blockInstance.getLocation().getWorld().spawnParticle(Particle.BLOCK,
                    new Location(blockInstance.getLocation().getWorld(),
                            blockInstance.getBlockPos().getX() + 0.5 + midFace.x,
                            blockInstance.getBlockPos().getY() + 0.5 + midFace.y,
                            blockInstance.getBlockPos().getZ() + 0.5 + midFace.z),
                    50, midOffset[0], midOffset[1], midOffset[2], 0,
                    blockInstance.getDBlock().getParticle().createBlockData());

            this.serverLevel.getChunkSource().broadcastAndSend(this.serverPlayer, new ClientboundBlockUpdatePacket(blockInstance.getBlockPos(), Blocks.DEAD_BUBBLE_CORAL_BLOCK.defaultBlockState()));
            blockInstance.getDBlockData().setLocked(false);
            blockInstance.getDBlockData().mined(this.player, msCooldown);

            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                serverLevel.getChunkSource().broadcastAndSend(this.serverPlayer,
                        new ClientboundBlockUpdatePacket(blockInstance.getBlockPos(), blockState));
                blockInstance.getLocation().getWorld().playSound(blockInstance.getLocation(), Sound.ENTITY_CHICKEN_EGG, 0.25f, (float) RandomUtil.between(0.75, 1.50));
                blockInstance.getLocation().getWorld().spawnParticle(Particle.BLOCK,
                        blockInstance.getLocation().toCenterLocation(),
                        50, 0.35, 0.35, 0.35, 0,
                        blockInstance.getDBlock().getParticle().createBlockData());
            }, msCooldown / 50);
        });
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            List<ItemStack> items = ItemUtil.rollItemDrops(blockInstance.getDBlock().getItemDrops());
            Vec3 faceVec = blockInstance.getDirection().getUnitVec3();

            Vec3 itemMidFace = DBlockUtil.getFaceCenterItem(blockInstance.getDirection());

            BlockPos blockPos = blockInstance.getBlockPos();
            for(ItemStack item : items) {
                ItemEntity itemEntity = new ItemEntity(
                        this.serverLevel,
                        blockPos.getX() + 0.5 + itemMidFace.x,
                        blockPos.getY() + 0.5 + itemMidFace.y,
                        blockPos.getZ() + 0.5 + itemMidFace.z,
                        CraftItemStack.asCraftCopy(item).handle,
                        faceVec.offsetRandom(RANDOM, 1.2f).x * 0.3,
                        faceVec.y * 0.3,
                        faceVec.offsetRandom(RANDOM, 1.2f).z * 0.3);

                itemPopVisual(itemEntity);
                magnetVisual(itemEntity, item);
                try {
                    Thread.sleep(ITEM_ALTERNATE_DELAY * 50);
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void magnetVisual(final ItemEntity itemEntity, final ItemStack item) {
        Bukkit.getScheduler().runTaskLater(Destructible.getInstance(), () -> {
            this.serverLevel.getChunkSource().broadcastAndSend(this.serverPlayer,
                    new ClientboundTakeItemEntityPacket(
                            itemEntity.getId(),
                            this.serverPlayer.getId(),
                            item.getAmount()
                    ));
            ItemUtil.give(this.player, item);
        }, ITEM_MAGNET_DELAY);

        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if(itemEntity.isAlive()) {
                this.serverLevel.getChunkSource().broadcastAndSend(this.serverPlayer,
                        new ClientboundRemoveEntitiesPacket(itemEntity.getId()));
            }
        }, ITEM_MAGNET_DELAY + ITEM_DELETE_DELAY);
    }

    private void itemPopVisual(final ItemEntity itemEntity) {
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            this.serverLevel.getChunkSource().broadcastAndSend(this.serverPlayer,
                    new ClientboundAddEntityPacket(
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

            this.serverLevel.getChunkSource().broadcastAndSend(this.serverPlayer,
                    new ClientboundSetEntityDataPacket(
                            itemEntity.getId(),
                            itemEntity.getEntityData().packAll()
                    ));

            this.serverLevel.getChunkSource().broadcastAndSend(this.serverPlayer,
                    new ClientboundSetEntityMotionPacket(
                            itemEntity.getId(),
                            itemEntity.getDeltaMovement()));
        });
    }
}
