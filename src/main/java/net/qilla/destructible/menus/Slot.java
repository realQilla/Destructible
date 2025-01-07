package net.qilla.destructible.menus;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.DataKey;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class Slot {
    private final int index;
    private final BiConsumer<Slot, ClickType> clickAction;
    private final ItemStack itemStack;
    private final SoundSettings soundSettings;
    private final Builder builder;

    public Slot(Builder builder) {
        this.index = builder.index;
        this.clickAction = builder.clickAction;
        this.itemStack = ItemStack.of(Material.GLASS_PANE);
        this.itemStack.editMeta(meta -> {
            meta.getPersistentDataContainer().set(DataKey.GUI_ITEM, PersistentDataType.BOOLEAN, true);
        });

        this.itemStack.getDataTypes().forEach(dataType -> this.itemStack.unsetData(dataType));
        if(builder.material != null) {
            this.itemStack.setData(DataComponentTypes.ITEM_MODEL, builder.material.getKey());
        }

        this.itemStack.setData(DataComponentTypes.MAX_STACK_SIZE, builder.amount);
        this.itemStack.setAmount(builder.amount);

        if(builder.hideTooltip) {
            this.itemStack.setData(DataComponentTypes.HIDE_TOOLTIP);
        } else {
            this.itemStack.setData(DataComponentTypes.ITEM_NAME, builder.displayName);
            this.itemStack.setData(DataComponentTypes.LORE, builder.lore);
        }

        if(builder.glow) {
            this.itemStack.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        this.soundSettings = builder.soundSettings;
        this.builder = builder;
    }

    public int getIndex() {
        return this.index;
    }

    public void onClick(ClickType clickType) {
        if(this.clickAction == null) return;
        this.clickAction.accept(this, clickType);
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public SoundSettings getSoundSettings() {
        return this.soundSettings;
    }

    public Builder getBuilder() {
        return this.builder;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Consumer<Builder> consumer) {
        Builder builder = new Builder();
        consumer.accept(builder);
        return builder;
    }

    public static Slot rebuild(Slot slot, Consumer<Builder> builder) {
        Builder newBuilder = new Builder();
        newBuilder.builder(slot.getBuilder());
        builder.accept(newBuilder);
        return new Slot(newBuilder);
    }

    public static final class Builder {
        private int index;
        private BiConsumer<Slot, ClickType> clickAction;
        private Material material;
        private int amount;
        private Component displayName;
        private boolean hideTooltip;
        private boolean glow;
        private ItemLore lore;
        private SoundSettings soundSettings;

        private Builder() {
            this.index = 0;
            this.clickAction = null;
            this.material = Material.BLACK_STAINED_GLASS_PANE;
            this.amount = 1;
            this.displayName = MiniMessage.miniMessage().deserialize("Empty Slot");
            this.lore = ItemLore.lore().build();
            this.soundSettings = null;
        }

        public Builder index(int index) {
            Preconditions.checkArgument(index >= 0 && index < 54, "Slot must be between 0 and 53");
            this.index = index;
            return this;
        }

        public Builder clickAction(BiConsumer<Slot, ClickType> clickAction) {
            this.clickAction = clickAction;
            return this;
        }

        public Builder noMaterial() {
            this.material = null;
            return this;
        }

        public Builder material(Material material) {
            Preconditions.checkArgument(material != null, "Material cannot be null");
            this.material = material;
            return this;
        }

        public Builder amount(int amount) {
            this.amount = Math.max(1, Math.min(99, amount));
            return this;
        }

        public Builder displayName(Component displayName) {
            Preconditions.checkArgument(displayName != null, "Display name cannot be null");
            this.displayName = displayName;
            return this;
        }

        public Builder hideTooltip(boolean hide) {
            this.hideTooltip = hide;
            return this;
        }

        public Builder glow(boolean glow) {
            this.glow = glow;
            return this;
        }

        public Builder lore(ItemLore lore) {
            Preconditions.checkArgument(lore != null, "Lore cannot be null");
            this.lore = lore;
            return this;
        }

        public Builder soundSettings(SoundSettings soundSettings) {
            Preconditions.checkArgument(soundSettings != null, "Sound settings cannot be null");
            this.soundSettings = soundSettings;
            return this;
        }

        public Builder builder(Builder builder) {
            this.index = builder.index;
            this.clickAction = builder.clickAction;
            this.material = builder.material;
            this.amount = builder.amount;
            this.displayName = builder.displayName;
            this.hideTooltip = builder.hideTooltip;
            this.lore = builder.lore;
            this.soundSettings = builder.soundSettings;
            return this;
        }

        public Slot build() {
            return new Slot(this);
        }
    }
}