package net.qilla.destructible.mining.item;

import com.google.common.base.Preconditions;
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
            Preconditions.checkArgument(Registries.DESTRUCTIBLE_ITEMS.containsKey(id), "DItem ID: " + id + " does not exist");
            this.dItem = Registries.DESTRUCTIBLE_ITEMS.get(id);
            return this;
        }

        public Builder amount(int minAmount, int maxAmount) {
            Preconditions.checkArgument(minAmount > 0, "Minimum amount must be greater than 0");
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            return this;
        }

        public Builder amount(int amount) {
            Preconditions.checkArgument(amount > 0, "Amount must be greater than 0");
            this.minAmount = amount;
            this.maxAmount = amount;
            return this;
        }

        public Builder minAmount(int amount) {
            Preconditions.checkArgument(amount > 0, "Minimum amount must be greater than 0");
            this.minAmount = amount;
            return this;
        }

        public Builder maxAmount(int amount) {
            Preconditions.checkArgument(amount > 0, "Maximum amount must be greater than 0");
            this.maxAmount = amount;
            return this;
        }

        public Builder dropChance(double chance) {
            Preconditions.checkArgument(chance >= 0.0 && chance <= 1.0, "Drop chance must be between 0.0 and 1.0");
            this.dropChance = chance;
            return this;
        }

        public DDrop build() {
            return new DDrop(this);
        }
    }
}