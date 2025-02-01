package net.qilla.destructible.mining.logic;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.data.DataKey;
import net.qilla.destructible.data.DSounds;
import net.qilla.destructible.mining.BlockInstance;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.*;
import net.qilla.destructible.mining.item.attributes.AttributeContainer;
import net.qilla.destructible.mining.item.attributes.AttributeTypes;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.DUtil;
import net.qilla.qlibrary.util.sound.QSound;
import net.qilla.qlibrary.util.tools.RandomUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BlockMiner {
    private static final BlockState BROKEN_STATE = Blocks.DEAD_BUBBLE_CORAL_BLOCK.defaultBlockState();
    private static final int ITEM_MAGNET_DELAY = 8;
    private static final int ITEM_ALTERNATE_DELAY = 5;
    private static final int ITEM_DELETE_DELAY = 4;

    private final RandomSource random = RandomSource.createNewThreadLocalInstance();
    private final Plugin plugin;
    private final DPlayer player;

    public BlockMiner(@NotNull Plugin plugin, @NotNull DPlayer player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void tickBlock(@NotNull ItemStack itemStack, @NotNull ItemData itemData, @NotNull DItem dItem, @NotNull BlockInstance blockInstance) {
        if(!this.canMine(blockInstance, itemData, dItem)) return;

        int efficiency = Math.max(1, itemData.getAttributes().getValue(AttributeTypes.MINING_EFFICIENCY) + dItem.getStaticAttributes().getValue(AttributeTypes.MINING_EFFICIENCY));

        if(dItem.getStaticAttributes().has(AttributeTypes.ITEM_MAX_DURABILITY)) {
            int durabilityLost = itemData.getAttribute(AttributeTypes.ITEM_DURABILITY_LOST);
            int maxDurability = dItem.getStaticAttributes().getValue(AttributeTypes.ITEM_MAX_DURABILITY);
            if(durabilityLost >= maxDurability) return;

            blockInstance.damageBlock(efficiency);

            if(blockInstance.isDestroyed()) {
                blockInstance.getDBlockData().setLocked(true);
                this.destroyBlock(blockInstance, itemData, dItem);
                if(dItem.getStaticAttributes().has(AttributeTypes.ITEM_MAX_DURABILITY)) this.damageTool(itemStack, itemData, durabilityLost, maxDurability);
            } else {
                player.broadcastPacket(new ClientboundBlockDestructionPacket(blockInstance.getBlockPos().hashCode(), blockInstance.getBlockPos(), blockInstance.getCrackLevel()));
            }
            return;
        }

        blockInstance.damageBlock(efficiency);

        if(blockInstance.isDestroyed()) {
            blockInstance.getDBlockData().setLocked(true);
            this.destroyBlock(blockInstance, itemData, dItem);
        } else {
            player.broadcastPacket(new ClientboundBlockDestructionPacket(blockInstance.getBlockPos().hashCode(), blockInstance.getBlockPos(), blockInstance.getCrackLevel()));
        }
    }

    public void endProgress(@NotNull BlockInstance blockInstance) {
        player.broadcastPacket(new ClientboundBlockDestructionPacket(blockInstance.getBlockPos().hashCode(), blockInstance.getBlockPos(), 10));
    }

    private void destroyBlock(@NotNull BlockInstance blockInstance, @NotNull ItemData itemData, @NotNull DItem dItem) {
        blockInstance.getDBlockData().setLocked(true);
        Bukkit.getScheduler().runTask(plugin, () -> {
            long msCooldown = RandomUtil.offset(blockInstance.getDBlock().getCooldown(), 2500);

            this.playBlockBreakingEffects(blockInstance);
            this.updateBlockToBroken(blockInstance, msCooldown);
            this.revertBlockState(blockInstance, msCooldown);
            blockInstance.getDBlockData().setLocked(false);
        });

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> this.handleItemDrops(blockInstance, itemData, dItem));
    }

    private void showItemPopVisual(CraftItem itemEntity) {
        player.broadcastPacket(new ClientboundAddEntityPacket(
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

        player.broadcastPacket(new ClientboundSetEntityDataPacket(
                itemEntity.getEntityId(),
                itemEntity.getHandle().getEntityData().packAll()
        ));

        player.broadcastPacket(new ClientboundSetEntityMotionPacket(
                itemEntity.getEntityId(),
                itemEntity.getHandle().getDeltaMovement()));
    }

    private void handleItemDrops(@NotNull BlockInstance blockInstance, @NotNull ItemData itemData, @NotNull DItem dItem) {
        Map<DItem, Integer> itemDrops = player.calcItemDrops(blockInstance.getDBlock().getLootpool(), itemData, dItem);
        Vec3 dropOffset = blockInstance.getDirection().getUnitVec3();
        Vec3 faceCenter = DUtil.getCenterOfFaceForItem(blockInstance.getDirection());

        for(Map.Entry<DItem, Integer> itemDrop : itemDrops.entrySet()) {
            CraftItem itemEntity = createItemEntity(blockInstance.getBlockPos(), faceCenter, dropOffset, itemDrop);
            scheduleItemVisuals(itemEntity, itemDrop);
            try {
                Thread.sleep(ITEM_ALTERNATE_DELAY * 50);
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void scheduleItemVisuals(CraftItem itemEntity, Map.Entry<DItem, Integer> itemDrop) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            showItemPopVisual(itemEntity);
            magnetItemToPlayer(itemEntity, itemDrop);
        });
    }

    private void magnetItemToPlayer(CraftItem itemEntity, Map.Entry<DItem, Integer> itemDrop) {
        Bukkit.getScheduler().runTaskLater(Destructible.getInstance(), () -> {
            player.broadcastPacket(new ClientboundTakeItemEntityPacket(
                    itemEntity.getEntityId(),
                    player.getHandle().getId(),
                    itemDrop.getValue())
            );

            if(DRegistry.ITEMS.containsKey(itemDrop.getKey().getID())) {
                player.give(DItemFactory.of(itemDrop.getKey(), itemDrop.getValue()));
            }
        }, ITEM_MAGNET_DELAY);

        removeItemAfterDelay(itemEntity);
    }

    private void removeItemAfterDelay(CraftItem itemEntity) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if(!itemEntity.isDead()) {
                player.broadcastPacket(new ClientboundRemoveEntitiesPacket(itemEntity.getEntityId()));
            }
        }, ITEM_MAGNET_DELAY + ITEM_DELETE_DELAY);
    }

    private void playBlockBreakingEffects(@NotNull BlockInstance blockInstance) {
        BlockPos blockPos = blockInstance.getBlockPos();
        Vec3 centerFace = DUtil.getCenterOfFace(blockInstance.getDirection());
        float[] sideOffset = DUtil.getSideOffset(blockInstance.getDirection());

        player.broadcastPacket(new ClientboundBlockDestructionPacket(blockPos.hashCode(), blockPos, 10));
        blockInstance.getLocation().getWorld().playSound(blockInstance.getLocation(), blockInstance.getDBlock().getBreakSound(), 1, 1);
        blockInstance.getLocation().getWorld().spawnParticle(Particle.BLOCK,
                new Location(blockInstance.getLocation().getWorld(),
                        blockPos.getX() + 0.5 + centerFace.x,
                        blockPos.getY() + 0.5 + centerFace.y,
                        blockPos.getZ() + 0.5 + centerFace.z),
                40, sideOffset[0], sideOffset[1], sideOffset[2], 0,
                blockInstance.getDBlock().getBreakParticle().createBlockData());
    }

    private void updateBlockToBroken(@NotNull BlockInstance blockInstance, long msCooldown) {
        player.broadcastPacket(new ClientboundBlockUpdatePacket(blockInstance.getBlockPos(), BROKEN_STATE));
        blockInstance.getDBlockData().mined(player, msCooldown);
    }

    private void revertBlockState(@NotNull BlockInstance blockInstance, long msCooldown) {
        BlockState originalState = this.getBlockState(blockInstance.getLocation());
        QSound sound = DSounds.BLOCK_RETURN;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.broadcastPacket(new ClientboundBlockUpdatePacket(blockInstance.getBlockPos(), originalState));
            blockInstance.getLocation().getWorld().playSound(blockInstance.getLocation(), sound.getSound(), sound.getVolume(), RandomUtil.offset(sound.getPitch(), 0.5f));
            blockInstance.getLocation().getWorld().spawnParticle(Particle.BLOCK,
                    blockInstance.getLocation().toCenterLocation(),
                    50, 0.35, 0.35, 0.35, 0,
                    blockInstance.getDBlock().getBreakParticle().createBlockData());
        }, msCooldown / 50);
    }

    private CraftItem createItemEntity(BlockPos blockPos, Vec3 faceCenter, Vec3 dropOffset, Map.Entry<DItem, Integer> itemDrop) {
        ItemEntity itemEntity = new ItemEntity(
                player.getHandle().serverLevel(),
                blockPos.getX() + 0.5 + faceCenter.x,
                blockPos.getY() + 0.5 + faceCenter.y,
                blockPos.getZ() + 0.5 + faceCenter.z,
                CraftItemStack.asCraftCopy(ItemStack.of(itemDrop.getKey().getMaterial(), itemDrop.getValue())).handle,
                dropOffset.offsetRandom(random, 1.2f).x * 0.25,
                dropOffset.y * 0.25,
                dropOffset.offsetRandom(random, 1.2f).z * 0.25);

        return new CraftItem((CraftServer) player.getServer(), itemEntity);
    }

    public BlockState getBlockState(@NotNull Location loc) {
        return ((CraftBlockState) loc.getBlock().getState()).getHandle();
    }

    public boolean canMine(@NotNull BlockInstance blockInstance, @NotNull ItemData itemData, @NotNull DItem dItem) {
        DBlock dBlock = blockInstance.getDBlock();

        if(blockInstance.getDBlockData().isLocked() ||
                blockInstance.getDBlockData().isOnCooldown() ||
                dBlock.getStrength() > dItem.getStaticAttributes().getValue(AttributeTypes.MINING_STRENGTH) ||
                dBlock.getDurability() < 0) {
            return false;
        }

        if(dBlock.getStrength() <= 0) return true;

        return dBlock.getCorrectTools().stream().anyMatch(toolType -> dItem.getStaticAttributes().getValue(AttributeTypes.TOOL_TYPE).equals(toolType));
    }

    public void damageTool(@NotNull ItemStack itemStack, @NotNull ItemData itemData, int durabilityLost, int maxDurability) {
        AttributeContainer attributeContainer = itemData.getAttributes();
        int newDurability = durabilityLost + 1;

        attributeContainer.set(AttributeTypes.ITEM_DURABILITY_LOST, newDurability);

        Bukkit.getScheduler().runTask(plugin, () -> {
            if(newDurability >= maxDurability) {
                Bukkit.getScheduler().runTask(plugin, () -> itemStack.editMeta(meta -> {
                    player.sendMessage("<red>Your currently active tool has broken!");
                    player.playSound(DSounds.ITEM_BREAK, true);
                }));
            }

            itemStack.editMeta(meta -> {
                meta.getPersistentDataContainer().set(DataKey.DESTRUCTIBLE_ITEM, ItemDataType.ITEM, itemData);
            });
            itemStack.setData(DataComponentTypes.DAMAGE, newDurability);
        });
    }
}