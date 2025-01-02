package net.qilla.destructible.mining.item;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.DataKey;
import net.qilla.destructible.data.Registries;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class DItemStack {

    private final DItem dItem;
    private int amount;

    private DItemStack(DItem dItem, int amount) {
        this.dItem = dItem;
        this.amount = amount;
    }

    @NotNull
    public static DItemStack of(DItem dItem) {
        Preconditions.checkArgument(dItem != null, "DItem cannot be null");
        return new DItemStack(dItem, 1);
    }

    @NotNull
    public static DItemStack of(DItem dItem, int amount) {
        Preconditions.checkArgument(dItem != null, "DItem cannot be null");
        return new DItemStack(dItem, amount);
    }

    @NotNull
    public static DItemStack of(ItemStack itemStack) {
        Preconditions.checkArgument(itemStack != null, "ItemStack cannot be null");
        return of(Registries.DESTRUCTIBLE_ITEMS.get(itemStack.getItemMeta().getPersistentDataContainer().get(DataKey.DESTRUCTIBLE_ID, PersistentDataType.STRING)), itemStack.getAmount());
    }

    @NotNull
    public static DItemStack of(String id) {
        Preconditions.checkArgument(id != null, "ID cannot be null");
        return of(Registries.DESTRUCTIBLE_ITEMS.get(id));
    }

    @Nullable
    public static DItem getDItem(ItemStack itemStack) {
        Preconditions.checkArgument(itemStack != null, "ItemStack is not a DItem");
        String id = itemStack.getPersistentDataContainer().get(DataKey.DESTRUCTIBLE_ID, PersistentDataType.STRING);
        if(id == null) return null;
        return Registries.DESTRUCTIBLE_ITEMS.get(id);
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return this.amount;
    }

    public ItemStack getItemStack() {
            ItemStack itemStack = ItemStack.of(this.dItem.getMaterial(), this.amount);
            itemStack.editMeta(meta -> {
                meta.getPersistentDataContainer().set(DataKey.DESTRUCTIBLE_ID, PersistentDataType.STRING, dItem.getId());
            });

            ItemLore.Builder lore = ItemLore.lore();
            lore.addLines(dItem.getLore().lines());

            if(this.dItem instanceof DTool dTool) {
                if(dTool.getDurability() != -1) {
                    itemStack.editMeta(meta -> {
                        meta.getPersistentDataContainer().set(DataKey.DURABILITY, PersistentDataType.INTEGER, dTool.getDurability());
                    });
                    itemStack.setData(DataComponentTypes.MAX_DAMAGE, dTool.getDurability());
                }
                lore.addLines(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Efficiency " + dTool.getEfficiency()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Strength " + dTool.getStrength())
                ));
            }
            lore.addLines(List.of(
                    Component.empty(),
                    dItem.getRarity().getComponent()
            ));
            itemStack.unsetData(DataComponentTypes.ATTRIBUTE_MODIFIERS);
            itemStack.setData(DataComponentTypes.ITEM_NAME, dItem.getDisplayName());
            itemStack.setData(DataComponentTypes.LORE, lore);
            itemStack.setData(DataComponentTypes.MAX_STACK_SIZE, dItem.getStackSize());
        return itemStack;
    }

    @NotNull
    public DItem getDItem() {
        return this.dItem;
    }

    @Override
    public @NotNull DItemStack clone() {
        return of(this.dItem, this.amount);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DItemStack dItemStack)) return false;
        return super.equals(obj) && dItem.getId().equals(dItemStack.dItem.getId());
    }
}