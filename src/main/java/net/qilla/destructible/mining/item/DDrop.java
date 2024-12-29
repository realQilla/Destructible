package net.qilla.destructible.mining.item;

import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.Registries;
import org.jetbrains.annotations.NotNull;
import java.util.logging.Logger;

public class DDrop {

    private static final Logger LOGGER = Destructible.getPluginLogger();
    private final DItem dItem;
    private final int minAmount;
    private final int maxAmount;
    private final double dropChance;

    protected DDrop(@NotNull Builder builder) {
        this.dItem = builder.dItem;
        this.minAmount = builder.minAmount;
        this.maxAmount = builder.maxAmount;
        this.dropChance = builder.dropChance;
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

    public static class Builder {
        private DItem dItem;
        private int minAmount;
        private int maxAmount;
        private double dropChance;

        public Builder() {
            this.dItem = DItems.DEFAULT;
            this.minAmount = 1;
            this.maxAmount = 1;
            this.dropChance = 1.0f;
        }

        public Builder dItem(@NotNull String id) {
            this.dItem = Registries.DESTRUCTIBLE_ITEMS.get(id);
            return this;
        }

        public Builder amount(int minAmount, int maxAmount) {
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            return this;
        }

        public Builder amount(int amount) {
            this.minAmount = amount;
            this.maxAmount = amount;
            return this;
        }

        public Builder minAmount(int amount) {
            this.minAmount = amount;
            return this;
        }

        public Builder maxAmount(int amount) {
            this.maxAmount = amount;
            return this;
        }

        public Builder dropChance(double chance) {
            if(chance < 0.0 || chance > 1.0) {
                LOGGER.warning("Drop chance values for item drop: " + dItem.getId() + " should not be under 0.0 or above 1.1");
                chance = 1.0;
            }
            this.dropChance = chance;
            return this;
        }

        public DDrop build() {
            if(maxAmount < 1) {
                LOGGER.warning("Maximum amount for item drop: " + dItem.getId() + " is less than 1, setting to 1");
                maxAmount = 1;
            }
            if(minAmount < 1) {
                LOGGER.warning("Minimum amount for item drop: " + dItem.getId() + " is less than 1, setting to 1");
                minAmount = 1;
            }
            if(minAmount > maxAmount) {
                LOGGER.warning("Minimum amount for item drop: " + dItem.getId() + " is greater than maximum amount, setting minimum to maximum amount");
                minAmount = maxAmount;
            }
            return new DDrop(this);
        }
    }
}