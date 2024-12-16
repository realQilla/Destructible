package net.qilla.destructible.mining.player.data;

import net.qilla.destructible.Destructible;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Equipment {
    private final PlayerData playerData;
    private ItemStack heldItem;
    private ItemStack offhandItem;
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;

    public Equipment(PlayerData playerData) {
        this.playerData = playerData;
        tick();
    }

    public void tick() {
        PlayerInventory inventory = playerData.getPlayer().getInventory();
        Bukkit.getScheduler().runTaskTimer(Destructible.getInstance(), () -> {
            this.heldItem = inventory.getItemInMainHand();
            this.offhandItem = inventory.getItemInOffHand();
            this.helmet = inventory.getHelmet();
            this.chestplate = inventory.getChestplate();
            this.leggings = inventory.getLeggings();
            this.boots = inventory.getBoots();
        }, 0, 10);
    }

    public ItemStack getHeldItem() {
        return heldItem;
    }

    public ItemStack getOffhandItem() {
        return offhandItem;
    }

    public ItemStack getHelmet() {
        return helmet;
    }

    public ItemStack getChestplate() {
        return chestplate;
    }

    public ItemStack getLeggings() {
        return leggings;
    }

    public ItemStack getBoots() {
        return boots;
    }
}
