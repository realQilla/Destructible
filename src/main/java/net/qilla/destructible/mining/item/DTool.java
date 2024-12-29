package net.qilla.destructible.mining.item;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class DTool extends DItem {
    private final List<DToolType> dToolType;
    private final int strength;
    private final float efficiency;
    private final int durability;

    public DTool(DItem.Properties itemProperties, DTool.Properties toolProperties) {
        super(itemProperties);
        this.dToolType = toolProperties.dToolType;
        this.strength = toolProperties.strength;
        this.efficiency = toolProperties.efficiency;
        this.durability = toolProperties.durability;
    }

    @NotNull
    public List<DToolType> getToolType() {
        return this.dToolType;
    }

    public int getStrength() {
        return this.strength;
    }

    public float getEfficiency() {
        return this.efficiency;
    }

    public int getDurability() {
        return this.durability;
    }

    public static class Properties {
        private List<DToolType> dToolType;
        private int strength;
        private float efficiency;
        private int durability;

        public static Properties of() {
            return new DTool.Properties();
        }

        /**
         * The type of tool
         *
         * @param type
         *
         * @return
         */
        public Properties dToolType(List<DToolType> type) {
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
        public Properties strength(int amount) {
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
        public Properties efficiency(float amount) {
            this.efficiency = Math.max(0, amount);
            return this;
        }

        public Properties durability(int amount) {
            this.durability = Math.max(1, amount);
            return this;
        }

        public Properties noDurability() {
            this.durability = -1;
            return this;
        }

        private Properties() {
            this.dToolType = List.of();
            this.strength = 0;
            this.efficiency = 1.0f;
            this.durability = -1;
        }
    }
}