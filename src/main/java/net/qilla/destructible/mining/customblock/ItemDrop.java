package net.qilla.destructible.mining.customblock;

import org.bukkit.Material;

public final class ItemDrop {

    private final Material material;
    private final int minAmount;
    private final int maxAmount;
    private final float dropChance;

    public ItemDrop(final Properties properties) {
        this.material = properties.material;
        this.minAmount = properties.minAmount;
        this.maxAmount = properties.maxAmount;
        this.dropChance = properties.dropChance;
    }

    public Material getMaterial() {
        return this.material;
    }

    public int getMinAmount() {
        return this.minAmount;
    }

    public int getMaxAmount() {
        return this.maxAmount;
    }

    public float getDropChance() {
        return this.dropChance;
    }

    public static class Properties {
        Material material;
        int minAmount;
        int maxAmount;
        float dropChance;

        public static ItemDrop.Properties of() {
            return new ItemDrop.Properties();
        }

        public ItemDrop.Properties setMaterial(final Material material) {
            this.material = material;
            return this;
        }

        public ItemDrop.Properties setAmount(final int min, final int max) {
            if(min < 0 || min > max) return this;
            this.minAmount = min;
            this.maxAmount = max;
            return this;
        }

        public ItemDrop.Properties setDropChance(final float chance) {
            if(chance > 1.0f || chance < 0.0f) return this;
            this.dropChance = chance;
            return this;
        }

        private Properties() {
            this.material = Material.STONE;
            this.minAmount = 1;
            this.maxAmount = 1;
            this.dropChance = 1.0f;
        }
    }
}
