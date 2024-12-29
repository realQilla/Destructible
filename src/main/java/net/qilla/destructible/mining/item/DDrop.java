package net.qilla.destructible.mining.item;

import net.qilla.destructible.data.Registries;
import org.jetbrains.annotations.NotNull;

public class DDrop {

    private final DItem dItem;
    private final int minAmount;
    private final int maxAmount;
    private final double dropChance;

    public DDrop(@NotNull Properties properties) {
        this.dItem = properties.dItem;
        this.minAmount = properties.minAmount;
        this.maxAmount = properties.maxAmount;
        this.dropChance = properties.dropChance;
    }

    public static DDrop of(@NotNull Properties properties) {
        return new DDrop(properties);
    }

    public @NotNull DItem getDItem() {
        return this.dItem;
    }

    public int getMinAmount() {
        return this.minAmount;
    }

    public int getMaxAmount() {
        return this.maxAmount;
    }

    public double getDropChance() {
        return this.dropChance;
    }

    public static class Properties {
        private DItem dItem;
        private int minAmount;
        private int maxAmount;
        private double dropChance;

        public static Properties of() {
            return new Properties();
        }

        public Properties dItem(@NotNull String id) {
            if(!Registries.DESTRUCTIBLE_ITEMS.containsKey(id)) {
                throw new IllegalArgumentException("Unknown destructible item id: " + id);
            }
            this.dItem = Registries.DESTRUCTIBLE_ITEMS.get(id);
            return this;
        }

        public Properties amount(int minAmount, int maxAmount) {
            if(minAmount < 1) minAmount = 1;
            if(minAmount > maxAmount) minAmount = maxAmount;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            return this;
        }

        public Properties amount(int amount) {
            this.minAmount = amount;
            this.maxAmount = amount;
            return this;
        }

        public Properties minAmount(int amount) {
            this.minAmount = amount;
            return this;
        }

        public Properties maxAmount(int amount) {
            this.maxAmount = amount;
            return this;
        }

        public Properties dropChance(double chance) {
            this.dropChance = chance;
            return this;
        }

        private Properties() {
            this.dItem = DItems.DEFAULT;
            this.minAmount = 1;
            this.maxAmount = 1;
            this.dropChance = 1.0f;
        }
    }
}