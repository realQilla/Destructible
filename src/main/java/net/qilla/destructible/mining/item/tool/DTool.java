package net.qilla.destructible.mining.item.tool;

import net.kyori.adventure.text.Component;
import net.qilla.destructible.mining.item.Breakable;
import net.qilla.destructible.mining.item.Rarity;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public abstract class DTool implements Breakable {
    private final String id;
    private final Component displayName;
    private final Material material;
    private final DToolType dToolType;
    private final Rarity rarity;
    private final int stackSize;
    private final int durability;
    private final int strength;
    private final float efficiency;

    public DTool(DTool.Properties properties) {
        this.id = properties.id;
        this.displayName = properties.displayName;
        this.material = properties.material;
        this.dToolType = properties.dToolType;
        this.rarity = properties.rarity;
        this.stackSize = properties.stackSize;
        this.durability = properties.durability;
        this.strength = properties.strength;
        this.efficiency = properties.efficiency;
    }

    @NotNull
    @Override
    public String getId() {
        return this.id;
    }

    @NotNull
    @Override
    public Component getDisplayName() {
        return this.displayName;
    }

    @NotNull
    @Override
    public Material getMaterial() {
        return this.material;
    }

    @NotNull
    @Override
    public Rarity getRarity() {
        return this.rarity;
    }

    @Override
    public int getStackSize() {
        return this.stackSize;
    }

    @Override
    public int getDurability() {
        return this.durability;
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
        private final String id;
        private Component displayName;
        private Material material;
        private DToolType dToolType;
        private Rarity rarity;
        private int stackSize;
        private int durability;
        private int strength;
        private float efficiency;

        public static DTool.Properties of(String id) {
            return new DTool.Properties(id);
        }

        /**
         * Displayed name for tool
         * @param displayName
         * @return
         */
        public DTool.Properties displayName(Component displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * The item that the tool will be
         * @param material
         * @return
         */
        public DTool.Properties material(Material material) {
            this.material = material;
            return this;
        }

        /**
         * What type enum the tool is
         * @param type
         * @return
         */
        public DTool.Properties dToolType(DToolType type) {
            this.dToolType = type;
            return this;
        }

        /**
         * The tools set rarity
         * @param rarity
         * @return
         */
        public DTool.Properties rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        /**
         * The amount this tool can be stacked to
         * @param amount
         * @return
         */
        public DTool.Properties stackSize(int amount) {
            this.stackSize = Math.max(0, Math.min(99, amount));
            return this;
        }

        /**
         * Total durability of the tool
         * @param amount
         * @return
         */
        public DTool.Properties durability(int amount) {
            this.durability = Math.max(1, amount);
            return this;
        }

        /**
         * Flags tool to have infinite durability
         * @return
         */
        public DTool.Properties noDurability() {
            this.durability = -1;
            return this;
        }

        /**
         * The tool's strength, used when mining custom blocks
         * @param amount
         * @return
         */
        public DTool.Properties strength(int amount) {
            this.strength = Math.max(0, amount);
            return this;
        }

        /**
         * Rate at which the tool mines a block, base rate is 1
         * @param amount
         * @return
         */
        public DTool.Properties efficiency(float amount) {
            this.efficiency = Math.max(0, amount);
            return this;
        }

        private Properties(String id) {
            this.id = id;
            this.displayName = Component.empty();
            this.material = Material.AIR;
            this.dToolType = DToolType.ANY;
            this.rarity = Rarity.NONE;
            this.stackSize = 1;
            this.durability = -1;
            this.strength = 0;
            this.efficiency = 1;
        }
    }
}