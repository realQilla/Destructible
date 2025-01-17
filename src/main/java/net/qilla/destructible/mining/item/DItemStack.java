package net.qilla.destructible.mining.item;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.qilla.destructible.data.DataKey;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.util.DestructibleUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import java.util.Optional;

public final class DItemStack {

    private DItemStack() {
    }

    @NotNull
    public static ItemStack of(DItem dItem, int amount) {
        Preconditions.checkArgument(dItem != null, "DItem cannot be null");
        return getItemStack(dItem, amount);
    }

    @NotNull
    public static ItemStack of(String id, int amount) {
        return of(Registries.DESTRUCTIBLE_ITEMS.get(id), amount);
    }

    @NotNull
    public static Optional<DItem> getDItem(ItemStack itemStack) {
        Preconditions.checkArgument(itemStack != null, "ItemStack cannot be null");
        String id = itemStack.getPersistentDataContainer().get(DataKey.DESTRUCTIBLE_ID, PersistentDataType.STRING);
        if(id == null) return Optional.empty();
        return Optional.ofNullable(Registries.DESTRUCTIBLE_ITEMS.get(id));
    }

    @NotNull
    private static ItemStack getItemStack(DItem dItem, int amount) {
        ItemStack itemStack = ItemStack.of(Material.STICK, amount);
        itemStack.getDataTypes().forEach(dataType -> itemStack.unsetData(dataType));
        itemStack.editMeta(meta -> {
            meta.getPersistentDataContainer().set(DataKey.DESTRUCTIBLE_ID, PersistentDataType.STRING, dItem.getId());
        });

        ItemLore lore;

        if(dItem instanceof DTool dTool) {
            if(dTool.getDurability() != -1) {
                itemStack.editMeta(meta -> {
                    meta.getPersistentDataContainer().set(DataKey.DURABILITY, PersistentDataType.INTEGER, dTool.getDurability());
                });
                itemStack.setData(DataComponentTypes.MAX_DAMAGE, dTool.getDurability());
            }
            lore = DestructibleUtil.getLore(dTool);
        } else lore = DestructibleUtil.getLore(dItem);
        itemStack.setData(DataComponentTypes.ITEM_MODEL, dItem.getMaterial().getKey());
        itemStack.setData(DataComponentTypes.ITEM_NAME, dItem.getDisplayName());
        itemStack.setData(DataComponentTypes.LORE, lore);
        itemStack.setData(DataComponentTypes.MAX_STACK_SIZE, dItem.getStackSize());
        return itemStack;
    }
}