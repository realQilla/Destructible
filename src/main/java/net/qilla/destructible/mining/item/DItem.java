package net.qilla.destructible.mining.item;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DItem {
    private final String id;
    private final Material material;
    private final Component displayName;
    private final ItemLore lore;
    private final int stackSize;
    private final Rarity rarity;

    protected DItem(@NotNull Builder builder) {
        this.id = builder.id;
        this.material = builder.material;
        this.displayName = builder.displayName;
        this.lore = builder.lore;
        this.stackSize = builder.stackSize;
        this.rarity = builder.rarity;
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

    public ItemLore getLore() {
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
        private ItemLore lore;
        private int stackSize;
        private Rarity rarity;

        public Builder() {
            this.id = UUID.randomUUID().toString();
            this.material = Material.AIR;
            this.displayName = Component.text(material.name());
            this.lore = ItemLore.lore().build();
            this.stackSize = 1;
            this.rarity = Rarity.NONE;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder material(Material material) {
            Preconditions.checkArgument(material != null, "Material cannot be null");
            this.material = material;
            return this;
        }

        public Builder displayName(Component name) {
            Preconditions.checkArgument(name != null, "Name cannot be null");
            this.displayName = name;
            return this;
        }

        public Builder defaultDisplayName() {
            this.displayName = MiniMessage.miniMessage().deserialize("<!italic><white>" + this.id);
            return this;
        }

        public Builder lore(ItemLore lore) {
            Preconditions.checkArgument(lore != null, "Lore cannot be null");
            this.lore = lore;
            return this;
        }

        public Builder noLore() {
            this.lore = ItemLore.lore().build();
            return this;
        }

        public Builder stackSize(int amount) {
            this.stackSize = Math.max(1, Math.min(99, amount));
            return this;
        }

        public Builder rarity(Rarity rarity) {
            Preconditions.checkArgument(rarity != null, "Rarity cannot be null");
            this.rarity = rarity;
            return this;
        }

        public DItem build() {
            return new DItem(this);
        }
    }
}