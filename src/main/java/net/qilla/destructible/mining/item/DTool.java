package net.qilla.destructible.mining.item;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public final class DTool extends DItem {
    private final Set<ToolType> toolType;
    private final int strength;
    private final int efficiency;
    private final int durability;

    private DTool(Builder builder) {
        super(builder.itemBuilder);
        this.toolType = builder.toolType;
        this.strength = builder.strength;
        this.efficiency = builder.efficiency;
        this.durability = builder.durability;
    }

    @NotNull
    public Set<ToolType> getToolType() {
        return Collections.unmodifiableSet(this.toolType);
    }

    public int getStrength() {
        return this.strength;
    }

    public int getEfficiency() {
        return this.efficiency;
    }

    public int getDurability() {
        return this.durability;
    }

    public static class Builder extends DItem.Builder {
        private DItem.Builder itemBuilder;
        private Set<ToolType> toolType;
        private int strength;
        private int efficiency;
        private int durability;

        public Builder() {
            this.toolType = Set.of();
            this.strength = 0;
            this.efficiency = 1;
            this.durability = -1;
        }

        public Builder item(DItem.Builder dItemBuilder) {
            Preconditions.checkArgument(dItemBuilder != null, "DItem builder cannot be null");
            this.itemBuilder = dItemBuilder;
            return this;
        }

        /**
         * The type of tool
         *
         * @param type
         *
         * @return
         */
        public Builder toolType(Set<ToolType> type) {
            Preconditions.checkArgument(type != null, "Tool type cannot be null");
            this.toolType = type;
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
        public Builder efficiency(int amount) {
            this.efficiency = Math.max(0, amount);
            return this;
        }

        public Builder durability(int amount) {
            this.durability = Math.max(-1, amount);
            return this;
        }

        public DTool build() {
            return new DTool(this);
        }
    }
}