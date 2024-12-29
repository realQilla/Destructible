package net.qilla.destructible.mining.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DItem {
    private final String id;
    private final Material material;
    private final Component displayName;
    private final List<Component> lore;
    private final int stackSize;
    private final Rarity rarity;

    protected DItem(@NotNull DItem.Builder Builder) {
        this.id = Builder.id;
        this.material = Builder.material;
        this.displayName = Builder.displayName;
        this.lore = Builder.lore;
        this.stackSize = Builder.stackSize;
        this.rarity = Builder.rarity;
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

    public static class Builder {
        private String id;
        private Material material;
        private Component displayName;
        private List<Component> lore;
        private int stackSize;
        private Rarity rarity;

        public Builder() {
            this.material = Material.AIR;
            this.displayName = Component.text(material.name());
            this.lore = List.of();
            this.stackSize = 1;
            this.rarity = Rarity.NONE;
        }

        public Builder id(@NotNull String id) {
            this.id = id;
            return this;
        }

        public Builder material(@NotNull Material material) {
            this.material = material;
            return this;
        }

        public Builder displayName(@NotNull Component name) {
            this.displayName = name;
            return this;
        }

        public Builder defaultDisplayName() {
            this.displayName = MiniMessage.miniMessage().deserialize("<!italic><white>" + this.id);
            return this;
        }

        public Builder lore(@NotNull List<Component> lore) {
            this.lore = lore;
            return this;
        }

        public Builder noLore() {
            this.lore = List.of();
            return this;
        }

        public Builder stackSize(int amount) {
            this.stackSize = Math.max(1, Math.min(99, amount));
            return this;
        }

        public Builder rarity(@NotNull Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public DItem build() {
            return new DItem(this);
        }
    }
}