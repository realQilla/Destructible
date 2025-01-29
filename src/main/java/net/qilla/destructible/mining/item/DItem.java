package net.qilla.destructible.mining.item;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.qilla.destructible.mining.item.attributes.Attribute;
import net.qilla.destructible.mining.item.attributes.AttributeContainer;
import net.qilla.qlibrary.util.tools.StringUtil;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.function.Consumer;

public final class DItem {
    private final String id;
    private final long version;
    private final Material material;
    private final Component displayName;
    private final ItemLore lore;
    private final Rarity rarity;
    private final int stackSize;
    private final boolean resource;
    private final AttributeContainer staticAttributes;
    private final AttributeContainer dynamicAttributes;

    private DItem(Builder builder) {
        Preconditions.checkNotNull(builder, "Builder cannot be null");

        this.id = builder.id;
        this.version = builder.version;
        this.material = builder.material;
        this.displayName = builder.displayName;
        this.lore = builder.lore;
        this.rarity = builder.rarity;
        this.stackSize = builder.stackSize;
        this.resource = builder.resource;
        this.staticAttributes = builder.staticAttributes;
        this.dynamicAttributes = builder.dynamicAttributes;
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static @NotNull DItem of(@NotNull Consumer<Builder> builder) {
        Builder newBuilder = new Builder();
        builder.accept(newBuilder);
        return newBuilder.build();
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

    public Rarity getRarity() {
        return this.rarity;
    }

    public long getVersion() {
        return this.version;
    }

    public int getStackSize() {
        return this.stackSize;
    }

    public boolean isResource() {
        return this.resource;
    }

    public @NotNull AttributeContainer getStaticAttributes() {
        return this.staticAttributes;
    }

    public @NotNull AttributeContainer getDynamicAttributes() {
        return this.dynamicAttributes;
    }

    public static final class Builder implements ItemBuilder<DItem>{
        private String id = StringUtil.uniqueIdentifier(8);
        private long version = System.currentTimeMillis();
        private Material material = Material.AIR;
        private Component displayName = Component.text(material.name());
        private ItemLore lore = ItemLore.lore(List.of());
        private Rarity rarity = Rarity.NONE;
        private int stackSize = 1;
        private boolean resource = true;
        private AttributeContainer dynamicAttributes = new AttributeContainer();
        private AttributeContainer staticAttributes = new AttributeContainer();

        private Builder() {
        }

        public @NotNull Builder id(@NotNull String id) {
            Preconditions.checkNotNull(id, "ID cannot be null");

            this.id = id;
            return this;
        }

        public @NotNull Builder version(long version) {
            this.version = version;
            return this;
        }

        public @NotNull Builder material(@NotNull Material material) {
            Preconditions.checkNotNull(material, "Material cannot be null");

            this.material = material;
            return this;
        }

        public @NotNull Builder displayName(@NotNull Component displayName) {
            Preconditions.checkNotNull(displayName, "Display name cannot be null");

            this.displayName = displayName;
            return this;
        }

        public @NotNull Builder lore(@NotNull ItemLore lore) {
            Preconditions.checkNotNull(lore, "Lore cannot be null");

            this.lore = lore;
            return this;
        }

        public @NotNull Builder rarity(@NotNull Rarity rarity) {
            Preconditions.checkNotNull(rarity, "Rarity cannot be null");

            this.rarity = rarity;
            return this;
        }

        public @NotNull Builder stackSize(int amount) {
            this.stackSize = Math.max(1, Math.min(99, amount));
            return this;
        }

        public @NotNull Builder resource(boolean resource) {
            this.resource = resource;
            return this;
        }

        public @NotNull Builder staticAttributes(@NotNull Set<Attribute<?>> attributes) {
            Preconditions.checkNotNull(attributes, "Attributes cannot be null");

            this.staticAttributes.set(attributes);
            return this;
        }

        public @NotNull Builder dynamicAttributes(@NotNull Set<Attribute<?>> attributes) {
            Preconditions.checkNotNull(attributes, "Attributes cannot be null");

            this.dynamicAttributes.set(attributes);
            return this;
        }

        @Override
        public @NotNull DItem build() {
            return new DItem(this);
        }
    }
}