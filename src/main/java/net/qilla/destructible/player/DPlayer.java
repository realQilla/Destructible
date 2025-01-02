package net.qilla.destructible.player;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.server.level.ServerPlayer;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.item.DDrop;
import net.qilla.destructible.mining.item.DItemStack;
import net.qilla.destructible.mining.logic.MiningManager;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DPlayer extends CraftPlayer {

    private static final Random RANDOM = new Random();
    private static final Destructible PLUGIN = Destructible.getInstance();
    private Overflow overflow;
    private MiningManager minerData;
    private EnumMap<Cooldown, Long> cooldowns;
    private DBlockEdit dBlockEdit;

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

    public void give(@NotNull ItemStack itemStack) {
        if(itemStack.isEmpty()) return;

        int space = getSpace(itemStack);
        if(space >= itemStack.getAmount()) {
            getInventory().addItem(itemStack);
            return;
        }

        ItemStack splitItem = itemStack.clone();
        splitItem.setAmount(space);
        this.getInventory().addItem(splitItem);
        itemStack.setAmount(itemStack.getAmount() - space);
        if(itemStack.getAmount() <= 0) return;
        Overflow overflow = Registries.DESTRUCTIBLE_PLAYERS.get(this.getUniqueId()).getOverflow();
        DItemStack dItemStack = DItemStack.of(itemStack);
        overflow.put(OverflowItem.of(dItemStack));

        this.getWorld().playSound(this.getLocation(), Sound.ENTITY_HORSE_SADDLE, 0.25f, 1);

        this.sendActionBar(MiniMessage.miniMessage().deserialize("<green>+" + itemStack.getAmount() + " ")
                .append(dItemStack.getDItem().getDisplayName().asComponent())
                .append(MiniMessage.miniMessage().deserialize(" added to stash!")));
    }

    public List<DItemStack> rollItemDrops(List<DDrop> itemDrops) {
        if(itemDrops.isEmpty()) return List.of();

        return itemDrops.stream().filter(drop -> RANDOM.nextFloat() <= drop.getDropChance())
                .map(drop -> {
                    int amount = RANDOM.nextInt(drop.getMaxAmount() - drop.getMinAmount() + 1) + drop.getMinAmount();
                    DItemStack dItemStack = DItemStack.of(drop.getDItem());
                    dItemStack.setAmount(amount);
                    return dItemStack;
                })
                .toList();
    }

    public MiningManager getMinerData() {
        if(this.minerData == null) this.minerData = new MiningManager(this);
        return this.minerData;
    }

    public Overflow getOverflow() {
        if(this.overflow == null) this.overflow = new Overflow();
        return this.overflow;
    }

    public long getCooldown(Cooldown cooldown) {
        if(cooldowns == null) cooldowns = new EnumMap<>(Cooldown.class);

        return this.cooldowns.computeIfAbsent(cooldown, c -> 0L);
    }

    public boolean hasCooldown(Cooldown cooldown) {
        return this.cooldowns.computeIfAbsent(cooldown, c -> 0L) > System.currentTimeMillis();
    }

    public void setCooldown(Cooldown cooldown, long ms) {
        this.cooldowns.put(cooldown, System.currentTimeMillis() + ms);
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

    public Destructible getPlugin() {
        return PLUGIN;
    }
}