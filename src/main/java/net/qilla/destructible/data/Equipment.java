package net.qilla.destructible.data;

import net.qilla.destructible.Destructible;
import net.qilla.destructible.mining.player.DMiner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public class Equipment {
    private final Player player;
    private ItemStack heldItem;
    private ItemStack offhandItem;
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;

    public Equipment(final Player player) {
        this.player = player;
        tick();
    }

    public void tick() {
        PlayerInventory inventory = player.getInventory();
        Bukkit.getScheduler().runTaskTimer(Destructible.getInstance(), () -> {
            this.heldItem = inventory.getItemInMainHand();
            //this.offhandItem = inventory.getItemInOffHand();
           // this.helmet = inventory.getHelmet();
            //this.chestplate = inventory.getChestplate();
            //this.leggings = inventory.getLeggings();
            //this.boots = inventory.getBoots();
        }, 0, 5);
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
