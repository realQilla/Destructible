package net.qilla.destructible.mining.player.data;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.DataKey;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.tool.DTool;
import net.qilla.destructible.mining.item.tool.DToolType;
import net.qilla.destructible.util.DBlockUtil;
import net.qilla.destructible.util.ItemUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Necessary player related data.
 */
public final class DMiner {

    private static final int ITEM_DELETE_DELAY = 8;
    private static final int ITEM_MAGNET_DELAY = 4;

    private final Destructible plugin;
    private final Player player;
    private final Location location;
    private final ServerPlayer serverPlayer;
    private final ServerLevel serverLevel;
    private final Equipment equipment;
    private DData dData = null;

    public DMiner(@NotNull final Destructible plugin, @NotNull final Player player) {
        this.plugin = plugin;
        this.player = player;
        this.location = player.getLocation();
        this.serverPlayer = ((CraftPlayer) player).getHandle();
        this.serverLevel = serverPlayer.serverLevel();
        this.equipment = new Equipment(this);
    }

    public void init(@NotNull final ServerboundPlayerActionPacket actionPacket) {
        if(this.player.getGameMode() != GameMode.SURVIVAL) return;

        Location blockLoc = DBlockUtil.blockPosToLoc(actionPacket.getPos(), this.player.getWorld());

        if(this.dData == null || blockLoc.hashCode() != this.dData.getBlockLoc().hashCode()) {
            this.setDData(new DData(this, actionPacket));
        }
    }

    public void tickBlock(@NotNull ServerboundSwingPacket swingPacket) {
        if(this.dData == null ||
                !swingPacket.getHand().equals(InteractionHand.MAIN_HAND) ||
                this.dData.getDBlock().getDurability() < 0) return;

        DBlock dBlock = this.dData.getDBlock();
        DTool dTool = this.dData.updateDTool();

        if(!canMine(dBlock, dTool)) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            if(this.dData.damage(dTool.getEfficiency())) {
                this.destroyBlock();
            } else {
                this.serverLevel.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundBlockDestructionPacket(this.dData.getBlockLoc().hashCode(), this.dData.getBlockPos(), this.dData.getBlockStage()));
            }
        });
    }

    private void destroyBlock() {
        Block block = this.dData.getBlockLoc().getBlock();

        this.serverLevel.getChunkSource().broadcastAndSend(this.serverPlayer, new ClientboundBlockDestructionPacket(this.dData.getBlockLoc().hashCode(), this.dData.getBlockPos(), 10));
        this.dData.getWorld().playSound(this.dData.getBlockLoc(), this.dData.getDBlock().getSound(), 1, 1);
        this.dData.getWorld().spawnParticle(Particle.BLOCK, this.dData.getBlockLoc().clone().add(0.5, 0.5, 0.5), 50, 0.25, 0.25, 0.25, 0, this.dData.getDBlock().getParticle().createBlockData());
        block.setType(Material.COBBLESTONE);

        Vec3 midFace = DBlockUtil.getMiddleFace(this.dData.getDirection());
        ItemStack[] items = ItemUtil.rollItemDrops(this.dData.getDBlock().getItemDrops());

        for(ItemStack item : items) {
            Vec3 vec3 = this.dData.getDirection().getUnitVec3().offsetRandom(RandomSource.create(), 1.2f);
            ItemEntity itemEntity = new ItemEntity(
                    this.serverLevel,
                    this.dData.getBlockPos().getX() + 0.5 + midFace.x,
                    this.dData.getBlockPos().getY() + 0.5 + midFace.y,
                    this.dData.getBlockPos().getZ() + 0.5 + midFace.z,
                    ((CraftItemStack) item).handle,
                    vec3.x * 0.3,
                    vec3.y * 0.3,
                    vec3.z * 0.3
            );

            //itemEntity.setNoGravity(true);

                this.serverLevel.getChunkSource().broadcastAndSend(this.serverPlayer, new ClientboundAddEntityPacket(
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

                this.serverLevel.getChunkSource().broadcastAndSend(this.serverPlayer, new ClientboundSetEntityDataPacket(
                        itemEntity.getId(),
                        itemEntity.getEntityData().packAll()
                ));

                this.serverLevel.getChunkSource().broadcastAndSend(this.serverPlayer, new ClientboundSetEntityMotionPacket(
                        itemEntity.getId(),
                        itemEntity.getDeltaMovement()));

                this.damageTool(1);
                ItemUtil.give(this.player, item);
                this.dData.updateBlock();

                Bukkit.getScheduler().runTaskLater(Destructible.getInstance(), () -> {
                    this.serverLevel.getChunkSource().broadcastAndSend(this.serverPlayer, new ClientboundTakeItemEntityPacket(
                            itemEntity.getId(),
                            this.serverPlayer.getId(),
                            item.getAmount()
                    ));

                }, ITEM_MAGNET_DELAY);

                Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                    if(itemEntity.isAlive()) {
                        this.serverLevel.getChunkSource().broadcastAndSend(this.serverPlayer, new ClientboundRemoveEntitiesPacket(itemEntity.getId()));
                    }
                }, ITEM_DELETE_DELAY);
        }
    }

    public void stop() {
        if(this.dData == null) return;

        this.serverLevel.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundBlockDestructionPacket(this.dData.getBlockLoc().hashCode(), this.dData.getBlockPos(), 10));
        this.dData = null;
    }

    private boolean canMine(@NotNull final DBlock dBlock, @NotNull final DTool dTool) {
        if(dBlock.getStrengthRequirement() > dTool.getStrength() || isToolBroken()) return false;
        if(Arrays.stream(dBlock.getProperTools()).noneMatch(properTool -> properTool.equals(dTool.getToolType()) || properTool.equals(DToolType.ANY))) return false;
        return true;
    }

    private void damageTool(int amount) {
        ItemStack tool = this.equipment.getHeldItem();
        PersistentDataContainer pdc = tool.getItemMeta().getPersistentDataContainer();
        if(!pdc.has(DataKey.DURABILITY, PersistentDataType.INTEGER)) return;
        int durability = pdc.get(DataKey.DURABILITY, PersistentDataType.INTEGER) - amount;

        if(durability > 0) {
            tool.editMeta(meta -> {
                meta.getPersistentDataContainer().set(DataKey.DURABILITY, PersistentDataType.INTEGER, durability);
            });
            Damageable toolDmg = (Damageable) tool.getItemMeta();
            toolDmg.setDamage(this.dData.getDTool().getDurability() - durability);
            tool.setItemMeta(toolDmg);
        } else {
            tool.editMeta(meta -> {
                meta.getPersistentDataContainer().set(DataKey.DURABILITY, PersistentDataType.INTEGER, 0);
            });
            Damageable toolDmg = (Damageable) tool.getItemMeta();
            toolDmg.setDamage(this.dData.getDTool().getDurability());
            tool.setItemMeta(toolDmg);
            this.player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Your currently active tool has broken!"));
            this.location.getWorld().playSound(this.location, Sound.ENTITY_ITEM_BREAK, 0.5f, 1);
        }
    }

    public boolean isToolBroken() {
        ItemStack tool = this.equipment.getHeldItem();
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

    @Nullable
    public DData setDData(@Nullable DData dData) {
        return this.dData = dData;
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