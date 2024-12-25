package net.qilla.destructible.mining.player;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.*;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.block.DBlocks;
import net.qilla.destructible.mining.item.tool.DTool;
import net.qilla.destructible.mining.item.tool.DToolType;
import net.qilla.destructible.util.CoordUtil;
import net.qilla.destructible.util.DBlockUtil;
import net.qilla.destructible.util.ItemUtil;
import org.bukkit.*;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Necessary player related data.
 */
public final class DMiner {

    private static final int ITEM_DELETE_DELAY = 12;
    private static final int ITEM_MAGNET_DELAY = 8;
    private static final int ITEM_ALTERNATE_DELAY = 5;
    private static final RandomSource RANDOM = RandomSource.createNewThreadLocalInstance();

    private final Destructible plugin;
    private final Player player;
    private final Location location;
    private final ServerPlayer serverPlayer;
    private final ServerLevel serverLevel;
    private final Equipment equipment;
    private DData dData;

    public DMiner(@NotNull final Destructible plugin, @NotNull final Player player) {
        this.plugin = plugin;
        this.player = player;
        this.location = player.getLocation();
        this.serverPlayer = ((CraftPlayer) player).getHandle();
        this.serverLevel = serverPlayer.serverLevel();
        this.equipment = new Equipment(this);
    }

    public void init(@NotNull final ServerboundPlayerActionPacket actionPacket) {
        if(player.getGameMode() == GameMode.CREATIVE) return;
        DBlock dBlock = getDBlock(actionPacket.getPos());

        if(dBlock == null) return;
        if(this.dData == null || actionPacket.getPos().hashCode() != this.dData.getPosHashCode()) {
            this.dData = new DData(this, actionPacket, dBlock);
        }
    }

    private DBlock getDBlock(final BlockPos blockPos) {
        var chunkMap = Registries.DESTRUCTIBLE_BLOCKS_CACHE.computeIfPresent(new ChunkPos(blockPos), (k, v) -> v);
        if(chunkMap == null) return DBlocks.NONE;
        var blockId = chunkMap.computeIfPresent(CoordUtil.posToChunkLocalPos(blockPos), (k, v) -> v);
        return blockId == null ? DBlocks.NONE: Registries.DESTRUCTIBLE_BLOCKS.get(blockId);
    }

    public void tickBlock(ServerboundSwingPacket swingPacket) {
        if(this.dData == null || this.dData.getDBlock().getDurability() < 0) return;

        this.dData.updateDTool();

        if(!canMine(dData)) return;
        DData dData = this.dData;

        Bukkit.getScheduler().runTask(plugin, () -> {
            if(dData.damage(dData.getDTool().getEfficiency()) || this.player.getGameMode() == GameMode.CREATIVE) this.destroyBlock(dData);
            else this.serverLevel.getChunkSource().broadcastAndSend(serverPlayer,
                    new ClientboundBlockDestructionPacket(dData.getPosHashCode(), dData.getBlockPos(), dData.getBlockStage()));
        });
    }

    private void destroyBlock(final DData dData) {
        Vec3 itemMidFace = DBlockUtil.getMidFaceItem(dData.getDirection());
        Vec3 midFace = DBlockUtil.getMidFace(dData.getDirection());
        float[] midOffset = DBlockUtil.getOffsetFace(dData.getDirection());

        this.serverLevel.getChunkSource().broadcastAndSend(this.serverPlayer,
                new ClientboundBlockDestructionPacket(dData.getPosHashCode(), dData.getBlockPos(), 10));
        dData.getWorld().playSound(dData.getBlockLoc(), dData.getDBlock().getSound(), 1, 1);
        dData.getWorld().spawnParticle(Particle.BLOCK,
                new Location(dData.getWorld(),
                        dData.getBlockPos().getX() + 0.5 + midFace.x,
                        dData.getBlockPos().getY() + 0.6 + midFace.y,
                        dData.getBlockPos().getZ() + 0.5 + midFace.z),
                50, midOffset[0], midOffset[1], midOffset[2], 0,
                dData.getDBlock().getParticle().createBlockData());

        Registries.DESTRUCTIBLE_BLOCK_DATA.computeIfAbsent(dData.getChunkPos(), k ->
                new DestructibleRegistry<>()).computeIfAbsent(dData.getChunkInt(), k ->
                new DBlockData(this.player)).mined(player, dData.getDBlock().getMsCooldown());
        this.damageTool(dData, 1);
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            List<ItemStack> items = ItemUtil.rollItemDrops(dData.getDBlock().getItemDrops());
            Vec3 faceVec = dData.getDirection().getUnitVec3();

            for(ItemStack item : items) {
                ItemEntity itemEntity = createItemEntity(dData, item, itemMidFace, faceVec);

                itemPopVisual(itemEntity);
                magnetVisual(itemEntity, item);
                try {
                    Thread.sleep(ITEM_ALTERNATE_DELAY * 50);
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        if(this.dData != null) this.dData = this.dData.refresh(getDBlock(this.dData.getBlockPos()));
    }

    private ItemEntity createItemEntity(final DData dData, final ItemStack item, final Vec3 itemMidFace, final Vec3 faceVec) {
        return new ItemEntity(
                this.serverLevel,
                dData.getBlockPos().getX() + 0.5 + itemMidFace.x,
                dData.getBlockPos().getY() + 0.5 + itemMidFace.y,
                dData.getBlockPos().getZ() + 0.5 + itemMidFace.z,
                ((CraftItemStack) item).handle,
                faceVec.offsetRandom(RANDOM, 1.2f).x * 0.3,
                faceVec.y * 0.3,
                faceVec.offsetRandom(RANDOM, 1.2f).z * 0.3
        );
        //itemEntity.setNoGravity(true);
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
        }, ITEM_DELETE_DELAY);
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

    public void stop() {
        if(this.dData == null) return;

        this.serverLevel.getChunkSource().broadcastAndSend(serverPlayer,
                new ClientboundBlockDestructionPacket(this.dData.getPosHashCode(), this.dData.getBlockPos(), 10));
        this.dData = null;
    }

    private boolean canMine(@NotNull final DData dData) {
        if(dData.getDBlock().getStrength() > dData.getDTool().getStrength() || isToolBroken()) return false;
        if(Registries.DESTRUCTIBLE_BLOCK_DATA.computeIfAbsent(dData.getChunkPos(), k ->
                new DestructibleRegistry<>()).computeIfAbsent(dData.getChunkInt(), k ->
                new DBlockData(this.player)).isOnCooldown()) return false;
        return dData.getDBlock().getProperTools().stream().anyMatch(dToolType -> dToolType.equals(DToolType.ANY) || dData.getDTool().getToolType().contains(dToolType));
    }

    private void damageTool(final DData dData, int amount) {
        ItemStack tool = this.equipment.getHeldItem();
        if(tool.isEmpty() || !tool.hasItemMeta()) return;
        PersistentDataContainer pdc = tool.getItemMeta().getPersistentDataContainer();
        if(!pdc.has(DataKey.DURABILITY, PersistentDataType.INTEGER)) return;
        int durability = pdc.get(DataKey.DURABILITY, PersistentDataType.INTEGER) - amount;

        if(durability > 0) {
            tool.editMeta(meta -> {
                meta.getPersistentDataContainer().set(DataKey.DURABILITY, PersistentDataType.INTEGER, durability);
            });
            Damageable toolDmg = (Damageable) tool.getItemMeta();
            toolDmg.setDamage(dData.getDTool().getDurability() - durability);
            tool.setItemMeta(toolDmg);
        } else {
            tool.editMeta(meta -> {
                meta.getPersistentDataContainer().set(DataKey.DURABILITY, PersistentDataType.INTEGER, 0);
            });
            Damageable toolDmg = (Damageable) tool.getItemMeta();
            toolDmg.setDamage(dData.getDTool().getDurability());
            tool.setItemMeta(toolDmg);
            this.player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Your currently active tool has broken!"));
            this.location.getWorld().playSound(this.location, Sound.ENTITY_ITEM_BREAK, 0.5f, 1);
        }
    }

    public boolean isToolBroken() {
        ItemStack tool = this.equipment.getHeldItem();
        if(tool.isEmpty()) return false;
        PersistentDataContainer pdc = tool.getItemMeta().getPersistentDataContainer();
        if(!pdc.has(DataKey.DURABILITY, PersistentDataType.INTEGER)) return false;
        return pdc.get(DataKey.DURABILITY, PersistentDataType.INTEGER) <= 0;
    }

    @NotNull
    public Player getPlayer() {
        return this.player;
    }

    @NotNull
    public Location getLocation() {
        return this.location;
    }

    @NotNull
    public ServerPlayer getServerPlayer() {
        return this.serverPlayer;
    }

    @NotNull
    public ServerLevel getServerLevel() {
        return this.serverLevel;
    }

    @NotNull
    public Equipment getEquipment() {
        return this.equipment;
    }

    @Nullable
    public DData getMiningData() {
        return this.dData;
    }
}