package net.qilla.destructible.mining.item;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.DataKey;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.util.FormatUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * This class is so complex, and for what reason :/
 * Rework later, maybe?
 * Also, I need to properly implement lore, etc.
 */
public final class DItemStack {

    private final DItem dItem;
    private int amount;
    private ItemStack itemStack;

    private DItemStack(DItem dItem, int amount) {
        this.dItem = dItem;
        this.amount = amount;
    }

    @NotNull
    public static DItemStack of(DItem dItem, int amount) {
        Preconditions.checkArgument(dItem != null, "DItem cannot be null");
        return new DItemStack(dItem, amount);
    }

    @NotNull
    public static DItemStack of(ItemStack itemStack) {
        Objects.requireNonNull(itemStack, "ItemStack cannot be null");
        String id = itemStack.getItemMeta().getPersistentDataContainer().get(DataKey.DESTRUCTIBLE_ID, PersistentDataType.STRING);
        return of(Registries.DESTRUCTIBLE_ITEMS.get(id), itemStack.getAmount());
    }

    @NotNull
    public static DItemStack of(String id) {
        return of(Objects.requireNonNull(Registries.DESTRUCTIBLE_ITEMS.get(id), "ID cannot be null"), 1);
    }

    @Nullable
    public static DItem getDItem(ItemStack itemStack) {
        Preconditions.checkArgument(itemStack != null, "ItemStack is not a DItem");
        String id = itemStack.getPersistentDataContainer().get(DataKey.DESTRUCTIBLE_ID, PersistentDataType.STRING);
        return id == null ? null : Registries.DESTRUCTIBLE_ITEMS.get(id);
    }

    public void setAmount(int amount) {
        if (this.itemStack != null) this.itemStack.setAmount(amount);
        this.amount = amount;
    }

    public int getAmount() {
        return this.itemStack != null ? this.itemStack.getAmount() : this.amount;
    }

    public ItemStack getItemStack() {
        if(this.itemStack != null) return this.itemStack;
        ItemStack itemStack = ItemStack.of(Material.STICK, this.amount);
        itemStack.getDataTypes().forEach(dataType -> itemStack.unsetData(dataType));
        itemStack.editMeta(meta -> {
            meta.getPersistentDataContainer().set(DataKey.DESTRUCTIBLE_ID, PersistentDataType.STRING, dItem.getId());
        });

        ItemLore.Builder lore = ItemLore.lore();
        lore.addLines(dItem.getLore().lines());

        if(this.dItem instanceof DTool dTool) {
            if(dTool.getToolDurability() != -1) {
                itemStack.editMeta(meta -> {
                    meta.getPersistentDataContainer().set(DataKey.DURABILITY, PersistentDataType.INTEGER, dTool.getToolDurability());
                });
                itemStack.setData(DataComponentTypes.MAX_DAMAGE, dTool.getToolDurability());
            }
            lore.addLines(List.of(
                    Component.empty(),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Efficiency " + FormatUtil.romanNumeral(dTool.getBreakingEfficiency())),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Strength " + FormatUtil.romanNumeral(dTool.getToolStrength())),
                    Component.empty(),
                    dTool.getRarity().getComponent().append(MiniMessage.miniMessage().deserialize(" " + FormatUtil.getList(dTool.getToolType())))
            ));
        } else {
            lore.addLines(List.of(
                    Component.empty(),
                    dItem.getRarity().getComponent()
            ));
        }
        itemStack.setData(DataComponentTypes.ITEM_MODEL, dItem.getMaterial().getKey());
        itemStack.setData(DataComponentTypes.ITEM_NAME, dItem.getDisplayName());
        itemStack.setData(DataComponentTypes.LORE, lore);
        itemStack.setData(DataComponentTypes.MAX_STACK_SIZE, dItem.getStackSize());
        this.itemStack = itemStack;
        return this.itemStack;
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
        if(this == obj) return true;
        if(!(obj instanceof DItemStack dItemStack)) return false;
        return super.equals(obj) && dItem.getId().equals(dItemStack.dItem.getId());
    }
}