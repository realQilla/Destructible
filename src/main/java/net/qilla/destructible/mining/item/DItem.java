package net.qilla.destructible.mining.item;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DItem {
    private final String id;
    private final Material material;
    private final Component displayName;
    private final List<Component> lore;
    private final int stackSize;
    private final Rarity rarity;
    private final int durability;

    public DItem(DItem.Properties properties) {
        this.id = properties.id;
        this.material = properties.material;
        this.displayName = properties.displayName;
        this.lore = properties.lore;
        this.stackSize = properties.stackSize;
        this.rarity = properties.rarity;
        this.durability = properties.durability;
    }

    public String getId() {
        return this.id;
    }

    public Material getMaterial() {
        return this.material;
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    public List<Component> getLore() {
        return this.lore;
    }

    public int getStackSize() {
        return this.stackSize;
    }

    public Rarity getRarity() {
        return this.rarity;
    }

    public int getDurability() {
        return this.durability;
    }

    public static class Properties {
        @Nullable
        private String id;
        private Material material;
        private Component displayName;
        private List<Component> lore;
        private int stackSize;
        private Rarity rarity;
        private int durability;

        public static DItem.Properties of() {
            return new DItem.Properties();
        }

        public DItem.Properties id(String id) {
            this.id = id;
            return this;
        }

        public DItem.Properties material(Material material) {
            this.material = material;
            return this;
        }

        public DItem.Properties displayName(Component name) {
            this.displayName = name;
            return this;
        }

        public DItem.Properties lore(List<Component> lore) {
            this.lore = lore;
            return this;
        }

        public DItem.Properties stackSize(int amount) {
            this.stackSize = Math.max(1, Math.min(99, amount));
            return this;
        }

        public DItem.Properties rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public DItem.Properties durability(int amount) {
            this.durability = Math.max(1, amount);
            return this;
        }

        public DItem.Properties noDurability() {
            this.durability = -1;
            return this;
        }

        protected Properties() {
            this.displayName = Component.empty();
            this.lore = List.of();
            this.stackSize = 1;
            this.rarity = Rarity.NONE;
            this.durability = -1;
        }

    }
}