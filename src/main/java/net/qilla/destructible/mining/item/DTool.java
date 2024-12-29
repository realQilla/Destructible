package net.qilla.destructible.mining.item;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DTool extends DItem {
    private final List<DToolType> dToolType;
    private final int strength;
    private final double efficiency;
    private final int durability;

    protected DTool(Builder toolBuilder) {
        super(toolBuilder.dItemBuilder);
        this.dToolType = toolBuilder.dToolType;
        this.strength = toolBuilder.strength;
        this.efficiency = toolBuilder.efficiency;
        this.durability = toolBuilder.durability;
    }

    @NotNull
    public List<DToolType> getToolType() {
        return this.dToolType;
    }

    public int getStrength() {
        return this.strength;
    }

    public double getEfficiency() {
        return this.efficiency;
    }

    public int getDurability() {
        return this.durability;
    }

    public static class Builder extends DItem.Builder {
        private DItem.Builder dItemBuilder;
        private List<DToolType> dToolType;
        private int strength;
        private double efficiency;
        private int durability;

        public Builder() {
            this.dToolType = List.of();
            this.strength = 0;
            this.efficiency = 1.0f;
            this.durability = -1;
        }

        public Builder dItem(DItem.Builder dItemBuilder) {
            this.dItemBuilder = dItemBuilder;
            return this;
        }

        /**
         * The type of tool
         *
         * @param type
         *
         * @return
         */
        public Builder dToolType(List<DToolType> type) {
            this.dToolType = type;
            return this;
        }

        /**
         * The tool's strength, used when mining custom blocks
         *
         * @param amount
         *
         * @return
         */
        public Builder strength(int amount) {
            this.strength = Math.max(0, amount);
            return this;
        }

        /**
         * Rate at which the tool mines a block, base rate is 1
         *
         * @param amount
         *
         * @return
         */
        public Builder efficiency(double amount) {
            this.efficiency = Math.max(1, amount);
            return this;
        }

        public Builder durability(int amount) {
            this.durability = Math.max(1, amount);
            return this;
        }

        public Builder noDurability() {
            this.durability = -1;
            return this;
        }

        public DTool build() {
            return new DTool(this);
        }
    }
}