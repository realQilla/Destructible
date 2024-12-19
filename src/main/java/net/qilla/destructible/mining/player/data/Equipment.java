package net.qilla.destructible.mining.player.data;

import net.qilla.destructible.Destructible;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public class Equipment {
    private final DMiner dMiner;
    private ItemStack heldItem;
    private ItemStack offhandItem;
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;

    public Equipment(DMiner dMiner) {
        this.dMiner = dMiner;
        tick();
    }

    public void tick() {
        PlayerInventory inventory = dMiner.getPlayer().getInventory();
        Bukkit.getScheduler().runTaskTimer(Destructible.getInstance(), () -> {
            this.heldItem = inventory.getItemInMainHand();
            this.offhandItem = inventory.getItemInOffHand();
            this.helmet = inventory.getHelmet();
            this.chestplate = inventory.getChestplate();
            this.leggings = inventory.getLeggings();
            this.boots = inventory.getBoots();
        }, 0, 10);
    }

    @NotNull
    public ItemStack getHeldItem() {
        return heldItem;
    }

    @NotNull
    public ItemStack getOffhandItem() {
        return offhandItem;
    }

    @NotNull
    public ItemStack getHelmet() {
        return helmet;
    }

    @NotNull
    public ItemStack getChestplate() {
        return chestplate;
    }

    @NotNull
    public ItemStack getLeggings() {
        return leggings;
    }

    @NotNull
    public ItemStack getBoots() {
        return boots;
    }
}
