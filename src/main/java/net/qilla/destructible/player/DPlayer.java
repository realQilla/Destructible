package net.qilla.destructible.player;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.data.SoundSettings;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.ItemDrop;
import net.qilla.destructible.mining.logic.MiningManager;
import net.qilla.destructible.util.RandomUtil;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class DPlayer {

    private static final Random RANDOM = new Random();
    private static final Destructible PLUGIN = Destructible.getInstance();
    private CraftPlayer craftPlayer;
    private Overflow overflow;
    private MiningManager minerData;
    private DBlockEdit dBlockEdit;
    private Cooldown cooldown;
    private MenuData menuData;

    public DPlayer(CraftPlayer craftPlayer) {
        this.craftPlayer = craftPlayer;
    }

    public void sendMessage(String message) {
        craftPlayer.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    public void sendMessage(Component component) {
        craftPlayer.sendMessage(component);
    }

    public void playSound(Sound sound, float volume, float pitch, SoundCategory category, PlayType playType) {
        switch(playType) {
            case PlayType.BROADCAST_CUR_LOC ->
                    getCraftPlayer().getWorld().playSound(getCraftPlayer().getLocation(), sound, category, volume, pitch);
            case PlayType.PLAYER_CUR_LOC -> craftPlayer.playSound(craftPlayer.getLocation(), sound, volume, pitch);
            case PlayType.PLAYER -> craftPlayer.playSound(craftPlayer, sound, volume, pitch);
        }
    }

    public void playSound(SoundSettings soundSettings, boolean randomPitch) {
        if(soundSettings == null) return;
        this.playSound(soundSettings.getSound(), soundSettings.getVolume(),  randomPitch ? RandomUtil.between(0.75f, 1.25f) : soundSettings.getPitch(), soundSettings.getCategory(), soundSettings.getPlayType());
    }

    public void sendPacket(Packet<?> packet) {
        craftPlayer.getHandle().connection.send(packet);
    }

    public void broadcastPacket(Packet<?> packet) {
        craftPlayer.getHandle().serverLevel().getChunkSource().broadcastAndSend(craftPlayer.getHandle(), packet);
    }

    public int getSpace(ItemStack itemStack) {
        int maxStackSize = itemStack.getMaxStackSize();
        int preExisting = Arrays.stream(craftPlayer.getInventory().getStorageContents())
                .filter(i -> i != null && i.isSimilar(itemStack))
                .mapToInt(i -> maxStackSize - i.getAmount())
                .sum();
        int empty = (int) Arrays.stream(craftPlayer.getInventory().getStorageContents())
                .filter(Objects::isNull)
                .count() * maxStackSize;
        return preExisting + empty;
    }

    public void give(ItemStack itemStack) {
        ItemStack clone = itemStack.clone();
        int space = getSpace(clone);
        if(space >= clone.getAmount()) {
            craftPlayer.getInventory().addItem(clone);
            return;
        }

        ItemStack splitItem = clone.clone();
        splitItem.setAmount(space);
        craftPlayer.getInventory().addItem(splitItem);
        int remaining = clone.getAmount() - space;
        if(remaining <= 0) return;
        clone.setAmount(remaining);
        Overflow overflow = Registries.DESTRUCTIBLE_PLAYERS.get(craftPlayer.getUniqueId()).getOverflow();
        overflow.put(clone);

        craftPlayer.getWorld().playSound(craftPlayer.getLocation(), Sound.ENTITY_HORSE_SADDLE, 0.25f, 1);

        craftPlayer.sendActionBar(MiniMessage.miniMessage().deserialize("<green>+" + clone.getAmount() + " ")
                .append(clone.getData(DataComponentTypes.ITEM_NAME))
                .append(MiniMessage.miniMessage().deserialize(" added to stash!")));
    }

    public Map<DItem, Integer> calculateItemDrops(List<ItemDrop> itemDrops) {
        return itemDrops.stream()
                .filter(this::hasChanceToDrop)
                .collect(Collectors.toMap(
                        ItemDrop::getDItem,
                        this::calculateAmount,
                        Integer::sum
                ));
    }

    private boolean hasChanceToDrop(ItemDrop itemDrop) {
        double dropChance = itemDrop.getChance();
        return RANDOM.nextDouble() < dropChance;
    }

    private int calculateAmount(ItemDrop itemDrop) {
        return RANDOM.nextInt(itemDrop.getMaxAmount() - itemDrop.getMinAmount() + 1) + itemDrop.getMinAmount();
    }

    public void resetCraftPlayer(CraftPlayer craftPlayer) {
        this.craftPlayer = craftPlayer;
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
        if(this.minerData == null) this.minerData = new MiningManager(this);
        return this.minerData;
    }

    public synchronized Overflow getOverflow() {
        if(this.overflow == null) this.overflow = new Overflow(this);
        return this.overflow;
    }

    @NotNull
    public synchronized DBlockEdit getDBlockEdit() {
        if(this.dBlockEdit == null) this.dBlockEdit = new DBlockEdit(this);
        return this.dBlockEdit;
    }

    public boolean hasDBlockEdit() {
        return this.dBlockEdit != null;
    }

    public synchronized void removeDBlockEdit() {
        this.dBlockEdit = null;
    }

    public synchronized Cooldown getCooldown() {
        if(this.cooldown == null) this.cooldown = new Cooldown();
        return this.cooldown;
    }

    public synchronized MenuData getMenuData() {
        if(this.menuData == null) this.menuData = new MenuData();
        return this.menuData;
    }

    public Destructible getPlugin() {
        return PLUGIN;
    }
}