package net.qilla.destructible.player;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.DataKey;
import net.qilla.destructible.data.SoundSettings;
import net.qilla.destructible.data.Sounds;
import net.qilla.destructible.menugeneral.MenuHolder;
import net.qilla.destructible.menugeneral.StaticMenu;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.ItemData;
import net.qilla.destructible.mining.item.ItemDataType;
import net.qilla.destructible.mining.item.ItemDrop;
import net.qilla.destructible.mining.logic.MiningManager;
import net.qilla.destructible.util.ComponentUtil;
import net.qilla.destructible.util.DUtil;
import net.qilla.destructible.util.RandomUtil;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class DPlayer {

    private final Destructible plugin;
    private final Random random;
    private CraftPlayer craftPlayer;
    private final Overflow overflow;
    private final MiningManager minerData;
    private DBlockEdit dBlockEdit;
    private final Cooldown cooldown;
    private final MenuHolder<StaticMenu> menuHolder;

    public DPlayer(@NotNull Destructible plugin, @NotNull CraftPlayer craftPlayer) {
        Preconditions.checkNotNull(plugin, "Plugin cannot be null");
        Preconditions.checkNotNull(craftPlayer, "CraftPlayer cannot be null");
        this.plugin = plugin;
        this.random = new Random();
        this.craftPlayer = craftPlayer;
        this.overflow = new Overflow(this);
        this.minerData = new MiningManager(this);
        this.cooldown = new Cooldown();
        this.menuHolder = new MenuHolder<>();
    }

    public void sendMessage(@NotNull String message) {
        this.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    public void sendMessage(@NotNull Component component) {
        craftPlayer.sendMessage(component);
    }

    public void sendActionBar(@NotNull String message) {
        this.sendActionBar(MiniMessage.miniMessage().deserialize(message));
    }

    public void sendActionBar(@NotNull Component component) {
        craftPlayer.sendActionBar(component);
    }

    public void playSound(Sound sound, float volume, float pitch, SoundCategory category, PlayType playType) {
        switch(playType) {
            case PlayType.BROADCAST_CUR_LOC ->
                    getCraftPlayer().getWorld().playSound(getCraftPlayer().getLocation(), sound, category, volume, pitch);
            case PlayType.PLAYER_CUR_LOC -> craftPlayer.playSound(craftPlayer.getLocation(), sound, volume, pitch);
            case PlayType.PLAYER -> craftPlayer.playSound(craftPlayer, sound, volume, pitch);
        }
    }

    public void playSound(@Nullable SoundSettings soundSettings, boolean randomPitch) {
        if(soundSettings == null) return;
        float pitch = soundSettings.getPitch();
        this.playSound(soundSettings.getSound(), soundSettings.getVolume(),
                randomPitch ? RandomUtil.between(Math.max(0, pitch - 0.25f), Math.min(2, pitch + 0.25f)) : pitch,
                soundSettings.getCategory(), soundSettings.getPlayType());
    }

    public void sendPacket(Packet<?> packet) {
        craftPlayer.getHandle().connection.send(packet);
    }

    public void broadcastPacket(Packet<?> packet) {
        craftPlayer.getHandle().serverLevel().getChunkSource().broadcastAndSend(craftPlayer.getHandle(), packet);
    }

    public int getSpace(@NotNull ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "ItemStack cannot be null");
        int maxStackSize = itemStack.getMaxStackSize();
        int space = 0;

        for (ItemStack stack : craftPlayer.getInventory().getStorageContents()) {
            if (stack == null) {
                space += maxStackSize; // Empty slot
            } else if (stack.isSimilar(itemStack)) {
                space += maxStackSize - stack.getAmount(); // Partial stack space
            }
        }
        return space;
    }

    public void give(@NotNull ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "ItemStack cannot be null");

        int space = this.getSpace(itemStack);
        this.playSound(Sounds.GET_ITEM, true);
        if(space >= itemStack.getAmount()) {
            craftPlayer.getInventory().addItem(itemStack);
            return;
        }
        ItemData itemData = itemStack.getPersistentDataContainer().get(DataKey.DESTRUCTIBLE_ITEM, ItemDataType.ITEM);

        if(itemData != null) {
            DItem dItem = DUtil.getDItem(itemData.getItemID());
            int overflowAmount = itemStack.getAmount() - space;
            itemStack.setAmount(space);

            overflow.put(itemData, overflowAmount);
            craftPlayer.getInventory().addItem(itemStack);

            this.sendActionBar(ComponentUtil.getItemAmountAndType(dItem, overflowAmount).append(MiniMessage.miniMessage().deserialize("<green> added to stash")));
        } else this.sendActionBar("<red>There was a problem adding an item to your stash!");
    }

    public Map<DItem, Integer> calcItemDrops(List<ItemDrop> itemDrops) {
        return itemDrops.stream()
                .filter(this::hasChanceToDrop)
                .sorted(Comparator.comparing(ItemDrop::getChance).reversed())
                .collect(Collectors.toUnmodifiableMap(
                        ItemDrop::getDItem,
                        this::calculateAmount,
                        Integer::sum
                ));
    }

    private boolean hasChanceToDrop(ItemDrop itemDrop) {
        double dropChance = itemDrop.getChance();
        return random.nextDouble() < dropChance;
    }

    private int calculateAmount(ItemDrop itemDrop) {
        return random.nextInt(itemDrop.getMaxAmount() - itemDrop.getMinAmount() + 1) + itemDrop.getMinAmount();
    }

    public void resetCraftPlayer(CraftPlayer craftPlayer) {
        this.craftPlayer = craftPlayer;
    }

    public Player getPlayer() {
        return this.craftPlayer.getPlayer();
    }

    public CraftPlayer getCraftPlayer() {
        return this.craftPlayer;
    }

    public CraftServer getCraftServer() {
        return (CraftServer) getCraftPlayer().getServer();
    }

    public ServerLevel getServerLevel() {
        return this.craftPlayer.getHandle().serverLevel();
    }

    public ServerPlayer getServerPlayer() {
        return this.craftPlayer.getHandle();
    }

    public MiningManager getMinerData() {
        return this.minerData;
    }

    public synchronized Overflow getOverflow() {
        return this.overflow;
    }

    @NotNull
    public synchronized DBlockEdit getDBlockEdit() {
        if(this.dBlockEdit == null) this.dBlockEdit = new DBlockEdit(plugin, this);
        return this.dBlockEdit;
    }

    public boolean hasDBlockEdit() {
        return this.dBlockEdit != null;
    }

    public synchronized void removeDBlockEdit() {
        this.dBlockEdit = null;
    }

    public synchronized Cooldown getCooldown() {
        return this.cooldown;
    }

    public synchronized MenuHolder<StaticMenu> getMenuHolder() {
        return this.menuHolder;
    }

    public Destructible getPlugin() {
        return plugin;
    }
}