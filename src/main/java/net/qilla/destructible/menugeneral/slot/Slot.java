package net.qilla.destructible.menugeneral.slot;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BundleContents;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.DataKey;
import net.qilla.destructible.data.SoundSettings;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class Slot {

    private final ItemStack itemStack;
    private final SoundSettings appearSound;
    private final SoundSettings clickSound;

    private Slot(Builder builder) {
        if(builder.contents.isEmpty()) itemStack = new ItemStack(Material.BARRIER);
        else itemStack = new ItemStack(Material.BUNDLE);

        itemStack.getDataTypes().forEach(dataType -> this.itemStack.unsetData(dataType));
        itemStack.editMeta(meta -> {
            meta.getPersistentDataContainer().set(DataKey.GUI_ITEM, PersistentDataType.BOOLEAN, true);
        });

        if(builder.material != null) {
            itemStack.setData(DataComponentTypes.ITEM_MODEL, builder.material.getKey());
        }

        this.itemStack.setData(DataComponentTypes.MAX_STACK_SIZE, builder.amount);
        this.itemStack.setAmount(builder.amount);

        if(builder.hideTooltip) {
            itemStack.setData(DataComponentTypes.HIDE_TOOLTIP);
        } else {
            itemStack.setData(DataComponentTypes.ITEM_NAME, builder.displayName);
            itemStack.setData(DataComponentTypes.LORE, builder.lore);
        }

        if(builder.glow) {
            itemStack.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        if(!builder.contents.isEmpty()) {
            BundleContents.Builder bundleContents = BundleContents.bundleContents();
            bundleContents.addAll(builder.contents);
            itemStack.setData(DataComponentTypes.BUNDLE_CONTENTS, bundleContents.build());
        }

        this.appearSound = builder.appearSound;
        this.clickSound = builder.clickSound;
    }

    public static Slot of(@NotNull Consumer<Builder> builder) {
        Preconditions.checkNotNull(builder, "Builder cannot be null");
        Builder newBuilder = new Builder();
        builder.accept(newBuilder);
        return new Slot(newBuilder);
    }

    public Slot modifyDisplayName(Component displayName) {
        this.itemStack.setData(DataComponentTypes.ITEM_NAME, displayName);
        return this;
    }

    public Slot modifyLore(ItemLore lore) {
        this.itemStack.setData(DataComponentTypes.LORE, lore);
        return this;
    }

    public ItemStack getItem() {
        return this.itemStack;
    }

    public SoundSettings getAppearSound() {
        return this.appearSound;
    }

    public SoundSettings getClickSound() {
        return this.clickSound;
    }

    public static class Builder {

        private Material material;
        private int amount;
        private Component displayName;
        private boolean hideTooltip;
        private boolean glow;
        private ItemLore lore;
        private List<ItemStack> contents;
        private SoundSettings appearSound;
        private SoundSettings clickSound;

        private Builder() {
            this.material = Material.BARRIER;
            this.amount = 1;
            this.displayName = MiniMessage.miniMessage().deserialize("<red>Missing Item");
            this.hideTooltip = false;
            this.glow = false;
            this.lore = ItemLore.lore().build();
            this.contents = List.of();
            this.appearSound = null;
            this.clickSound = null;
        }

        public Builder material(@Nullable Material material) {
            this.material = material;
            return this;
        }

        public Builder amount(int amount) {
            this.amount = Math.max(1, Math.min(99, amount));
            return this;
        }

        public Builder displayName(@NotNull Component displayName) {
            Preconditions.checkNotNull(displayName, "Display name cannot be null");
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

        public Builder lore(@NotNull ItemLore lore) {
            Preconditions.checkNotNull(lore, "Lore cannot be null");
            this.lore = lore;
            return this;
        }

        public Builder contents(@NotNull List<ItemStack> contents) {
            Preconditions.checkNotNull(contents, "Contents cannot be null");
            this.contents = contents;
            return this;
        }

        public Builder appearSound(@NotNull SoundSettings soundSettings) {
            Preconditions.checkNotNull(soundSettings, "Appear sound cannot be null");
            this.appearSound = soundSettings;
            return this;
        }

        public Builder clickSound(@NotNull SoundSettings soundSettings) {
            Preconditions.checkNotNull(soundSettings, "Click sound cannot be null");
            this.clickSound = soundSettings;
            return this;
        }
    }
}