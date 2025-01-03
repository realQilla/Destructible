package net.qilla.destructible.mining.item;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class DTool extends DItem {
    private final List<ToolType> toolType;
    private final int toolStrength;
    private final int breakingEfficiency;
    private final int toolDurability;

    private DTool(Builder builder) {
        super(builder.itemBuilder);
        this.toolType = builder.toolType;
        this.toolStrength = builder.toolStrength;
        this.breakingEfficiency = builder.breakingEfficiency;
        this.toolDurability = builder.toolDurability;
    }

    @NotNull
    public List<ToolType> getToolType() {
        return this.toolType;
    }

    public int getToolStrength() {
        return this.toolStrength;
    }

    public int getBreakingEfficiency() {
        return this.breakingEfficiency;
    }

    public int getToolDurability() {
        return this.toolDurability;
    }

    public static class Builder extends DItem.Builder {
        private DItem.Builder itemBuilder;
        private List<ToolType> toolType;
        private int toolStrength;
        private int breakingEfficiency;
        private int toolDurability;

        public Builder() {
            this.toolType = List.of();
            this.toolStrength = 0;
            this.breakingEfficiency = 1;
            this.toolDurability = -1;
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
        public Builder toolType(List<ToolType> type) {
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
        public Builder toolStrength(int amount) {
            this.toolStrength = Math.max(0, amount);
            return this;
        }

        /**
         * Rate at which the tool mines a block, base rate is 1
         *
         * @param amount
         *
         * @return
         */
        public Builder toolEfficiency(int amount) {
            this.breakingEfficiency = Math.max(0, amount);
            return this;
        }

        public Builder toolDurability(int amount) {
            this.toolDurability = Math.max(-1, amount);
            return this;
        }

        public Builder noToolDurability() {
            this.toolDurability = -1;
            return this;
        }

        public DTool build() {
            return new DTool(this);
        }
    }
}