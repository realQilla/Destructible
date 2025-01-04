package net.qilla.destructible.mining.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.mining.BlockInstance;
import net.qilla.destructible.mining.item.DItemStack;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.mining.item.DTool;
import net.qilla.destructible.util.DBlockUtil;
import net.qilla.destructible.util.RandomUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BlockMiner {
    private static final int ITEM_MAGNET_DELAY = 8;
    private static final int ITEM_ALTERNATE_DELAY = 5;
    private static final int ITEM_DELETE_DELAY = 4;
    private static final RandomSource RANDOM = RandomSource.createNewThreadLocalInstance();

    private final DPlayer dPlayer;

    public BlockMiner(@NotNull DPlayer dPlayer) {
        this.dPlayer = dPlayer;
    }

    public void tickBlock(@NotNull BlockInstance blockInstance, @NotNull DTool dTool, @NotNull ToolManager toolManager) {
        blockInstance.damageBlock(dTool.getBreakingEfficiency());

        if(blockInstance.isDestroyed()) {
            blockInstance.getDBlockData().setLocked(true);
            this.destroyBlock(blockInstance);
            toolManager.damageTool(dTool, 1);
        } else {
            dPlayer.broadcastPacket(new ClientboundBlockDestructionPacket(
                    blockInstance.getBlockPos().hashCode(), blockInstance.getBlockPos(), blockInstance.getCrackLevel()));
        }
    }

    public void endProgress(@NotNull BlockInstance blockInstance) {
        dPlayer.broadcastPacket(new ClientboundBlockDestructionPacket(blockInstance.getBlockPos().hashCode(), blockInstance.getBlockPos(), 10));
    }

    private void destroyBlock(@NotNull BlockInstance blockInstance) {
        Bukkit.getScheduler().runTask(dPlayer.getPlugin(), () -> {
            long msCooldown = RandomUtil.offset(blockInstance.getDBlock().getBlockCooldown(), 2000);
            BlockState blockState = ((CraftBlockState) blockInstance.getLocation().getBlock().getState()).getHandle();
            Vec3 midFace = DBlockUtil.getCenterFaceParticle(blockInstance.getDirection());
            float[] midOffset = DBlockUtil.getFlatOffsetParticles(blockInstance.getDirection());

            dPlayer.broadcastPacket(new ClientboundBlockDestructionPacket(blockInstance.getBlockPos().hashCode(), blockInstance.getBlockPos(), 10));
            blockInstance.getLocation().getWorld().playSound(blockInstance.getLocation(), blockInstance.getDBlock().getBreakSound(), 1, (float) RandomUtil.between(0.75, 1.25));
            blockInstance.getLocation().getWorld().spawnParticle(Particle.BLOCK,
                    new Location(blockInstance.getLocation().getWorld(),
                            blockInstance.getBlockPos().getX() + 0.5 + midFace.x,
                            blockInstance.getBlockPos().getY() + 0.5 + midFace.y,
                            blockInstance.getBlockPos().getZ() + 0.5 + midFace.z),
                    50, midOffset[0], midOffset[1], midOffset[2], 0,
                    blockInstance.getDBlock().getBreakParticle().createBlockData());

            dPlayer.broadcastPacket(new ClientboundBlockUpdatePacket(blockInstance.getBlockPos(), Blocks.DEAD_BUBBLE_CORAL_BLOCK.defaultBlockState()));
            blockInstance.getDBlockData().setLocked(false);
            blockInstance.getDBlockData().mined(dPlayer.getCraftPlayer(), msCooldown);

            Bukkit.getScheduler().runTaskLater(dPlayer.getPlugin(), () -> {
                dPlayer.broadcastPacket(new ClientboundBlockUpdatePacket(blockInstance.getBlockPos(), blockState));
                blockInstance.getLocation().getWorld().playSound(blockInstance.getLocation(), Sound.ENTITY_CHICKEN_EGG, 0.25f, (float) RandomUtil.between(0.75, 1.50));
                blockInstance.getLocation().getWorld().spawnParticle(Particle.BLOCK,
                        blockInstance.getLocation().toCenterLocation(),
                        50, 0.35, 0.35, 0.35, 0,
                        blockInstance.getDBlock().getBreakParticle().createBlockData());
            }, msCooldown / 50);
        });

        Bukkit.getScheduler().runTaskAsynchronously(dPlayer.getPlugin(), () -> {
            List<DItemStack> dItemStacks = dPlayer.rollItemDrops(blockInstance.getDBlock().getItemDrops());
            Vec3 faceVec = blockInstance.getDirection().getUnitVec3();

            Vec3 itemMidFace = DBlockUtil.getFaceCenterItem(blockInstance.getDirection());

            BlockPos blockPos = blockInstance.getBlockPos();
            for(DItemStack dItemStack : dItemStacks) {
                ItemEntity itemEntity = new ItemEntity(
                        dPlayer.getServerLevel(),
                        blockPos.getX() + 0.5 + itemMidFace.x,
                        blockPos.getY() + 0.5 + itemMidFace.y,
                        blockPos.getZ() + 0.5 + itemMidFace.z,
                        CraftItemStack.asCraftCopy(dItemStack.getItemStack()).handle,
                        faceVec.offsetRandom(RANDOM, 1.2f).x * 0.3,
                        faceVec.y * 0.3,
                        faceVec.offsetRandom(RANDOM, 1.2f).z * 0.3);

                CraftItem craftItem = new CraftItem(dPlayer.getCraftServer(), itemEntity);
                Bukkit.getScheduler().runTask(dPlayer.getPlugin(), () -> {
                    this.itemPopVisual(craftItem);
                    this.magnetVisual(craftItem, dItemStack);
                });
                try {
                    Thread.sleep(ITEM_ALTERNATE_DELAY * 50);
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void magnetVisual(CraftItem itemEntity, DItemStack dItemStack) {
        Bukkit.getScheduler().runTaskLater(Destructible.getInstance(), () -> {
            dPlayer.broadcastPacket(new ClientboundTakeItemEntityPacket(
                    itemEntity.getEntityId(),
                    dPlayer.getCraftPlayer().getEntityId(),
                    dItemStack.getAmount()
            ));
            dPlayer.give(dItemStack);
        }, ITEM_MAGNET_DELAY);

        Bukkit.getScheduler().runTaskLater(dPlayer.getPlugin(), () -> {
            if(!itemEntity.isDead()) {
                dPlayer.broadcastPacket(new ClientboundRemoveEntitiesPacket(itemEntity.getEntityId()));
            }
        }, ITEM_MAGNET_DELAY + ITEM_DELETE_DELAY);
    }

    private void itemPopVisual(final CraftItem itemEntity) {
        dPlayer.broadcastPacket(new ClientboundAddEntityPacket(
                itemEntity.getEntityId(),
                itemEntity.getUniqueId(),
                itemEntity.getX(),
                itemEntity.getY(),
                itemEntity.getZ(),
                0,
                0,
                itemEntity.getHandle().getType(),
                0,
                itemEntity.getHandle().getDeltaMovement(),
                0
        ));

        dPlayer.broadcastPacket(new ClientboundSetEntityDataPacket(
                itemEntity.getEntityId(),
                itemEntity.getHandle().getEntityData().packAll()
        ));

        dPlayer.broadcastPacket(new ClientboundSetEntityMotionPacket(
                itemEntity.getEntityId(),
                itemEntity.getHandle().getDeltaMovement()));
    }
}