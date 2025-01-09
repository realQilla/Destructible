package net.qilla.destructible.menus.slot;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.DataKey;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import java.util.function.Consumer;

public class Display {
    private final Builder builder;
    private final ItemStack itemStack;

    private Display(Builder builder) {
        this.builder = builder;
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
    }

    public ItemStack get() {
        return this.itemStack;
    }

    public Display ofNew(Consumer<Builder> builder) {
        Builder curBuilder = this.builder;
        builder.accept(curBuilder);
        return new Display(curBuilder);
    }

    public static Builder of() {
        return new Builder();
    }

    public static Display of(Consumer<Builder> builder) {
        Builder newBuilder = new Builder();
        builder.accept(newBuilder);
        return newBuilder.build();
    }

    public static final class Builder {
        private Material material;
        private int amount;
        private Component displayName;
        private boolean hideTooltip;
        private boolean glow;
        private ItemLore lore;

        private Builder() {
            this.material = Material.BLACK_STAINED_GLASS_PANE;
            this.amount = 1;
            this.displayName = MiniMessage.miniMessage().deserialize("Empty Slot");
            this.lore = ItemLore.lore().build();
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

        public Display build() {
            return new Display(this);
        }
    }
}