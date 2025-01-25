package net.qilla.destructible.mining.item;

import com.google.common.base.Preconditions;
import net.qilla.destructible.data.registry.DRegistry;
import org.jetbrains.annotations.NotNull;

public class ItemDrop {

    private final DItem item;
    private final boolean fortuneAffected;
    private final int minAmount;
    private final int maxAmount;
    private final double chance;

    protected ItemDrop(@NotNull Builder builder) {
        Preconditions.checkNotNull(builder, "Builder cannot be null");

        this.item = builder.item;
        this.fortuneAffected = builder.fortuneAffected;
        this.minAmount = builder.minAmount;
        this.maxAmount = builder.maxAmount;
        this.chance = builder.chance;
    }

    public @NotNull DItem getDItem() {
        return this.item;
    }

    public boolean isFortuneAffected() {
        return this.fortuneAffected;
    }

    public int getMinAmount() {
        return this.minAmount;
    }

    public int getMaxAmount() {
        return this.maxAmount;
    }

    public double getChance() {
        return this.chance;
    }

    public static class Builder implements ItemBuilder<ItemDrop> {
        private DItem item;
        private boolean fortuneAffected;
        private int minAmount;
        private int maxAmount;
        private double chance;

        public Builder() {
            this.item = DItems.MISSING_ITEM;
            this.fortuneAffected = true;
            this.minAmount = 1;
            this.maxAmount = 1;
            this.chance = 1.0f;
        }

        public Builder dItem(@NotNull String id) {
            this.item = DRegistry.ITEMS.getOrDefault(id, DItems.MISSING_ITEM);
            return this;
        }

        public Builder dItem(@NotNull DItem item) {
            this.item = item;
            return this;
        }

        public Builder fortuneAffected(boolean fortuneAffected) {
            this.fortuneAffected = fortuneAffected;
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

        public Builder chance(double chance) {
            Preconditions.checkArgument(chance >= 0.0 && chance <= 1.0, "Drop chance must be between 0.0 and 1.0");

            this.chance = chance;
            return this;
        }

        @Override
        public ItemDrop build() {
            return new ItemDrop(this);
        }
    }
}