package net.qilla.destructible.mining.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public final class DDrop {

    private final ItemStack itemStack;
    private final int minAmount;
    private final int maxAmount;
    private final float dropChance;

    public DDrop(final Properties properties) {
        this.itemStack = properties.itemStack;
        this.minAmount = properties.minAmount;
        this.maxAmount = properties.maxAmount;
        this.dropChance = properties.dropChance;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public int getMinAmount() {
        return this.minAmount;
    }

    public int getMaxAmount() {
        return this.maxAmount;
    }

    public float getDropChance() {
        return this.dropChance;
    }

    public static class Properties {
        private ItemStack itemStack;
        private int minAmount;
        private int maxAmount;
        private float dropChance;

        public static DDrop.Properties of() {
            return new DDrop.Properties();
        }

        public DDrop.Properties itemStack(final ItemStack itemStack) {
            this.itemStack = itemStack;
            return this;
        }

        public DDrop.Properties itemStack(final Material material, final Consumer<ItemStack> consumer) {
            ItemStack itemStack = new ItemStack(material);
            consumer.accept(itemStack);
            this.itemStack = itemStack;
            return this;
        }

        public DDrop.Properties amount(final int min, final int max) {
            if(min < 0 || min > max) return this;
            this.minAmount = min;
            this.maxAmount = max;
            return this;
        }

        public DDrop.Properties amount(final int amount) {
            this.minAmount = amount;
            this.maxAmount = amount;
            return this;
        }

        public DDrop.Properties dropChance(final float chance) {
            if(chance > 1.0f || chance < 0.0f) return this;
            this.dropChance = chance;
            return this;
        }

        private Properties() {
            this.itemStack = new ItemStack(Material.AIR);
            this.minAmount = 1;
            this.maxAmount = 1;
            this.dropChance = 1.0f;
        }
    }
}
