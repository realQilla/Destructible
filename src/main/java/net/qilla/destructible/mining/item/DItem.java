package net.qilla.destructible.mining.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DItem {
    private final String id;
    private final Material material;
    private final Component displayName;
    private final List<Component> lore;
    private final int stackSize;
    private final Rarity rarity;

    public DItem(@NotNull DItem.Properties Properties) {
        this.id = Properties.id;
        this.material = Properties.material;
        this.displayName = Properties.displayName;
        this.lore = Properties.lore;
        this.stackSize = Properties.stackSize;
        this.rarity = Properties.rarity;
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

    public static class Properties {
        @Nullable
        private String id;
        private Material material;
        private Component displayName;
        private List<Component> lore;
        private int stackSize;
        private Rarity rarity;

        public static Properties of() {
            return new Properties();
        }

        public Properties id(@NotNull String id) {
            this.id = id;
            return this;
        }

        public Properties material(@NotNull Material material) {
            this.material = material;
            return this;
        }

        public Properties displayName(@NotNull Component name) {
            this.displayName = name;
            return this;
        }

        public Properties defaultDisplayName() {
            this.displayName = MiniMessage.miniMessage().deserialize("<!italic><white>" + this.material.name());
            return this;
        }

        public Properties lore(@NotNull List<Component> lore) {
            this.lore = lore;
            return this;
        }

        public Properties noLore() {
            this.lore = List.of();
            return this;
        }

        public Properties stackSize(int amount) {
            this.stackSize = Math.max(1, Math.min(99, amount));
            return this;
        }

        public Properties rarity(@NotNull Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        private Properties() {
            this.material = Material.AIR;
            this.displayName = Component.text(material.name());
            this.lore = List.of();
            this.stackSize = 1;
            this.rarity = Rarity.NONE;
        }

    }
}