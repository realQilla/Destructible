package net.qilla.destructible.mining.item.tool;

import net.qilla.destructible.mining.item.DItem;
import org.jetbrains.annotations.NotNull;

public final class DTool extends DItem {
    private final DToolType dToolType;
    private final int strength;
    private final float efficiency;

    public DTool(DItem.Properties itemProperties, DTool.Properties toolProperties) {
        super(itemProperties);
        this.dToolType = toolProperties.dToolType;
        this.strength = toolProperties.strength;
        this.efficiency = toolProperties.efficiency;
    }

    @NotNull
    public DToolType getToolType() {
        return this.dToolType;
    }

    public int getStrength() {
        return this.strength;
    }

    public float getEfficiency() {
        return this.efficiency;
    }

    public static class Properties {
        private DToolType dToolType;
        private int strength;
        private float efficiency;

        public static DTool.Properties of() {
            return new DTool.Properties();
        }

        /**
         * The type of tool
         *
         * @param type
         *
         * @return
         */
        public DTool.Properties dToolType(DToolType type) {
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
        public DTool.Properties strength(int amount) {
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
        public DTool.Properties efficiency(float amount) {
            this.efficiency = Math.max(0, amount);
            return this;
        }

        private Properties() {
            this.dToolType = DToolType.ANY;
            this.strength = 0;
            this.efficiency = 1.0f;
        }
    }
}