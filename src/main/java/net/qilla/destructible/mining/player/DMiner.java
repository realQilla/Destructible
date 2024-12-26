package net.qilla.destructible.mining.player;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.*;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.block.DBlocks;
import net.qilla.destructible.mining.item.tool.DTool;
import net.qilla.destructible.mining.item.tool.DToolType;
import net.qilla.destructible.mining.item.tool.DTools;
import net.qilla.destructible.util.CoordUtil;
import net.qilla.destructible.util.DBlockUtil;
import net.qilla.destructible.util.ItemUtil;
import net.qilla.destructible.util.RandomUtil;
import org.bukkit.*;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Necessary player related data.
 */
public final class DMiner {

    private static final int ITEM_MAGNET_DELAY = 8;
    private static final int ITEM_ALTERNATE_DELAY = 5;
    private static final int ITEM_DELETE_DELAY = 4;
    private static final RandomSource RANDOM = RandomSource.createNewThreadLocalInstance();

    private final Destructible plugin;
    private final Player player;
    private final Location location;
    private final ServerPlayer serverPlayer;
    private final ServerLevel serverLevel;
    private final Equipment equipment;
    private volatile DData dData;

    public DMiner(@NotNull final Destructible plugin, @NotNull final Player player, @NotNull final Equipment equipment) {
        this.plugin = plugin;
        this.player = player;
        this.location = player.getLocation();
        this.serverPlayer = ((CraftPlayer) player).getHandle();
        this.serverLevel = serverPlayer.serverLevel();
        this.equipment = equipment;
    }

    public void init(@NotNull final ServerboundPlayerActionPacket actionPacket) {
        if(player.getGameMode() == GameMode.CREATIVE) return;

        ChunkPos chunkPos = new ChunkPos(actionPacket.getPos());
        int chunkInt = CoordUtil.posToChunkLocalPos(actionPacket.getPos());

        if(this.dData == null || actionPacket.getPos().hashCode() != this.dData.getBlockPos().hashCode()) {
            DData dData = new DData(this.player.getWorld(), actionPacket.getPos(), chunkPos, chunkInt, actionPacket.getDirection());
            this.dData = dData;
            this.dData.setDBlock(getDBlock(dData));
        }
    }

    private DBlock getDBlock(@NotNull final DData dData) {
        var blockCache = Registries.DESTRUCTIBLE_BLOCKS_CACHE.computeIfPresent(dData.getChunkPos(), (k, v) -> v);
        if(blockCache == null) return DBlocks.NONE;
        String blockString = blockCache.computeIfPresent(dData.getChunkInt(), (k2, v2) -> v2);
        return blockString == null ? DBlocks.NONE : Registries.DESTRUCTIBLE_BLOCKS.getOrDefault(blockString, DBlocks.NONE);
    }

    private DTool getDTool() {
        String toolString = equipment.getHeldItem().getPersistentDataContainer().getOrDefault(DataKey.TOOL, PersistentDataType.STRING, "");
        if(toolString.isEmpty()) return DTools.DEFAULT;
        return Registries.DESTRUCTIBLE_TOOLS.getOrDefault(toolString, DTools.DEFAULT);
    }

    public void tickBlock(final ServerboundSwingPacket swingPacket) {
        if(this.dData == null || this.dData.getDBlock().getDurability() < 0 || !swingPacket.getHand().equals(InteractionHand.MAIN_HAND)) return;

        this.dData.setDTool(getDTool());
        if(!canMine(this.dData)) return;

        this.dData.damageBlock(dData.getDTool().getEfficiency());
        if(dData.isBroken()) {
            this.destroyBlock(dData);
        } else {
            this.serverLevel.getChunkSource().broadcastAndSend(
                    this.serverPlayer, new ClientboundBlockDestructionPacket(
                            this.dData.getBlockPos().hashCode(), this.dData.getBlockPos(), this.dData.getCrackLevel()));
        }
    }

    private void destroyBlock(@NotNull DData dData) {
        Registries.DESTRUCTIBLE_BLOCK_DATA.computeIfAbsent(this.dData.getChunkPos(), k ->
                new DestructibleRegistry<>()).computeIfAbsent(this.dData.getChunkInt(), k2 ->
                new DBlockData()).mined(this.player, this.dData.getDBlock().getMsCooldown());

        Bukkit.getScheduler().runTask(this.plugin, () -> {
            BlockState blockState = ((CraftBlockState) dData.getLocation().getBlock().getState()).getHandle();
            Vec3 midFace = DBlockUtil.getMidFace(dData.getDirection());
            float[] midOffset = DBlockUtil.getOffsetFace(dData.getDirection());

            this.damageTool(dData, 1);
            this.serverLevel.getChunkSource().broadcastAndSend(
                    this.serverPlayer, new ClientboundBlockDestructionPacket(dData.getBlockPos().hashCode(), dData.getBlockPos(), 10));
            dData.getLocation().getWorld().playSound(dData.getLocation(), dData.getDBlock().getSound(), 1, (float) RandomUtil.between(0.75, 1.25));
            dData.getLocation().getWorld().spawnParticle(Particle.BLOCK,
                    new Location(dData.getLocation().getWorld(),
                            dData.getBlockPos().getX() + 0.5 + midFace.x,
                            dData.getBlockPos().getY() + 0.6 + midFace.y,
                            dData.getBlockPos().getZ() + 0.5 + midFace.z),
                    50, midOffset[0], midOffset[1], midOffset[2], 0,
                    dData.getDBlock().getParticle().createBlockData());

            this.serverLevel.getChunkSource().broadcastAndSend(this.serverPlayer, new ClientboundBlockUpdatePacket(dData.getBlockPos(), Blocks.DEAD_BUBBLE_CORAL_BLOCK.defaultBlockState()));

            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                serverLevel.getChunkSource().broadcastAndSend(this.serverPlayer,
                        new ClientboundBlockUpdatePacket(dData.getBlockPos(), blockState));
                dData.getLocation().getWorld().playSound(dData.getLocation(), Sound.ENTITY_CHICKEN_EGG, 0.25f, (float) RandomUtil.between(0.75, 1.50));
                dData.getLocation().getWorld().spawnParticle(Particle.BLOCK,
                        dData.getLocation().toCenterLocation(),
                        50, 0.30, 0.30, 0.30, 0,
                        dData.getDBlock().getParticle().createBlockData());
            }, dData.getDBlock().getMsCooldown() / 50);
        });

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            List<ItemStack> items = ItemUtil.rollItemDrops(this.dData.getDBlock().getItemDrops());
            Vec3 faceVec = this.dData.getDirection().getUnitVec3();

            Vec3 itemMidFace = DBlockUtil.getMidFaceItem(dData.getDirection());

            for(ItemStack item : items) {
                ItemEntity itemEntity = createItemEntity(dData.getBlockPos(), item, itemMidFace, faceVec);

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

    private ItemEntity createItemEntity(final BlockPos blockPos, final ItemStack item, final Vec3 itemMidFace, final Vec3 faceVec) {
        return new ItemEntity(
                this.serverLevel,
                blockPos.getX() + 0.5 + itemMidFace.x,
                blockPos.getY() + 0.5 + itemMidFace.y,
                blockPos.getZ() + 0.5 + itemMidFace.z,
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

    public void stop() {
        if(this.dData == null) return;

        this.serverLevel.getChunkSource().broadcastAndSend(serverPlayer,
                new ClientboundBlockDestructionPacket(this.dData.getBlockPos().hashCode(), this.dData.getBlockPos(), 10));
        this.dData = null;
    }

    private boolean canMine(@NotNull final DData dData) {
        if(Registries.DESTRUCTIBLE_BLOCK_DATA.computeIfAbsent(dData.getChunkPos(), k ->
                new DestructibleRegistry<>()).computeIfAbsent(dData.getChunkInt(), k ->
                new DBlockData()).isOnCooldown()) return false;
        if(dData.getDBlock().getStrength() > dData.getDTool().getStrength() || isToolBroken()) return false;
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