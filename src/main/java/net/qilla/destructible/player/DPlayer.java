package net.qilla.destructible.player;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.server.level.ServerPlayer;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.gui.DestructibleMenu;
import net.qilla.destructible.mining.item.DDrop;
import net.qilla.destructible.mining.item.DItemStack;
import net.qilla.destructible.mining.logic.MiningManager;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DPlayer extends CraftPlayer {

    private static final Random RANDOM = new Random();
    private static final Destructible PLUGIN = Destructible.getInstance();
    private Overflow overflow;
    private MiningManager minerData;
    private DBlockEdit dBlockEdit;
    private Cooldown cooldown;
    private MenuData menuData;

    public DPlayer(CraftServer server, ServerPlayer entity) {
        super(server, entity);
    }

    public int getSpace(ItemStack itemStack) {
        int maxStackSize = itemStack.getMaxStackSize();
        int preExisting = Arrays.stream(getInventory().getStorageContents())
                .filter(i -> i != null && i.isSimilar(itemStack))
                .mapToInt(i -> maxStackSize - i.getAmount())
                .sum();
        int empty = (int) Arrays.stream(getInventory().getStorageContents())
                .filter(Objects::isNull)
                .count() * maxStackSize;
        return preExisting + empty;
    }

    public void give(@NotNull DItemStack dItemStack) {
        ItemStack itemStack = dItemStack.getItemStack();
        int space = getSpace(itemStack);
        if(space >= dItemStack.getAmount()) {
            getInventory().addItem(itemStack);
            return;
        }

        ItemStack splitItem = itemStack.clone();
        splitItem.setAmount(space);
        this.getInventory().addItem(splitItem);
        int remaining = itemStack.getAmount() - space;
        if(remaining <= 0) return;
        dItemStack.setAmount(remaining);
        Overflow overflow = Registries.DESTRUCTIBLE_PLAYERS.get(this.getUniqueId()).getOverflow();
        overflow.put(dItemStack);

        this.getWorld().playSound(this.getLocation(), Sound.ENTITY_HORSE_SADDLE, 0.25f, 1);

        this.sendActionBar(MiniMessage.miniMessage().deserialize("<green>+" + itemStack.getAmount() + " ")
                .append(dItemStack.getDItem().getDisplayName().asComponent())
                .append(MiniMessage.miniMessage().deserialize(" added to stash!")));
    }

    public List<DItemStack> rollItemDrops(List<DDrop> itemDrops) {
        if(itemDrops.isEmpty()) return List.of();

        return itemDrops.stream().filter(drop -> RANDOM.nextFloat() <= drop.getChance())
                .map(drop -> {
                    int amount = RANDOM.nextInt(drop.getMaxAmount() - drop.getMinAmount() + 1) + drop.getMinAmount();
                    return DItemStack.of(drop.getDItem(), amount);
                })
                .toList();
    }

    public MiningManager getMinerData() {
        if(this.minerData == null) this.minerData = new MiningManager(this);
        return this.minerData;
    }

    public Overflow getOverflow() {
        if(this.overflow == null) this.overflow = new Overflow(this);
        return this.overflow;
    }

    @NotNull
    public DBlockEdit getDBlockEdit() {
        if(this.dBlockEdit == null) this.dBlockEdit = new DBlockEdit(this);
        return this.dBlockEdit;
    }

    public boolean hasDBlockEdit() {
        return this.dBlockEdit != null;
    }

    public void removeDBlockEdit() {
        this.dBlockEdit = null;
    }

    public Cooldown getCooldown() {
        if(this.cooldown == null) this.cooldown = new Cooldown();
        return this.cooldown;
    }

    public MenuData getMenuData() {
        if(this.menuData == null) this.menuData = new MenuData();
        return this.menuData;
    }

    public Destructible getPlugin() {
        return PLUGIN;
    }
}