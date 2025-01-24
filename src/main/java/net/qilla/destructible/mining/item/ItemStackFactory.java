package net.qilla.destructible.mining.item;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Unbreakable;
import net.qilla.destructible.data.DataKey;
import net.qilla.destructible.mining.item.attributes.AttributeTypes;
import net.qilla.destructible.util.ComponentUtil;
import net.qilla.destructible.util.DUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class ItemStackFactory {

    private ItemStackFactory() {
    }

    public static @NotNull ItemStack of(@NotNull DItem item, int amount) {
        Preconditions.checkNotNull(item, "DItem cannot be null");

        return getItemStack(item, amount);
    }

    public static @NotNull ItemStack of(@NotNull ItemData itemData, int amount) {
        Preconditions.checkNotNull(itemData, "ItemData cannot be null");
        DItem dItem = DUtil.getDItem(itemData.getItemID());

        return getItemStack(dItem, amount);
    }

    public static @NotNull ItemStack ofUpdated(@NotNull ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "ItemStack cannot be null");

        return updateItem(itemStack);
    }

    private static @NotNull ItemStack getItemStack(@NotNull DItem item, int amount) {
        Preconditions.checkNotNull(item, "DItem cannot be null");

        ItemStack itemStack = ItemStack.of(item.isResource() ? Material.STICK : item.getMaterial(), amount);
        ItemData itemData = new ItemData(item);

        itemStack.getDataTypes().forEach(itemStack::unsetData);
        itemStack.editMeta(meta -> {
            meta.getPersistentDataContainer().set(DataKey.DESTRUCTIBLE_ITEM, ItemDataType.ITEM, itemData);
        });

        if(item.getStaticAttributes().has(AttributeTypes.ITEM_MAX_DURABILITY)) {
            itemStack.setData(DataComponentTypes.MAX_DAMAGE, item.getStaticAttributes().getValue(AttributeTypes.ITEM_MAX_DURABILITY));
            itemData.getAttributes().set(AttributeTypes.ITEM_DURABILITY_LOST, 0);
        }
        else itemStack.setData(DataComponentTypes.UNBREAKABLE, Unbreakable.unbreakable(false));

        itemStack.setData(DataComponentTypes.MAX_STACK_SIZE, item.getStackSize());
        itemStack.setData(DataComponentTypes.ITEM_MODEL, item.getMaterial().getKey());
        itemStack.setData(DataComponentTypes.ITEM_NAME, item.getDisplayName());
        itemStack.setData(DataComponentTypes.LORE, ComponentUtil.getLore(itemData));

        return itemStack;
    }

    private static @NotNull ItemStack updateItem(@NotNull ItemStack itemStack) {
        ItemData itemData = itemStack.getPersistentDataContainer().get(DataKey.DESTRUCTIBLE_ITEM, ItemDataType.ITEM);
        if(itemData == null) return getItemStack(DItems.MISSING_ITEM, itemStack.getAmount());

        DItem dItem = DUtil.getDItem(itemData.getItemID());
        ItemStack newItemStack = getItemStack(dItem, itemStack.getAmount());

        newItemStack.setData(DataComponentTypes.DAMAGE, itemData.getAttributes().getValue(AttributeTypes.ITEM_MAX_DURABILITY));

        newItemStack.editMeta(meta -> {
            meta.getPersistentDataContainer().set(DataKey.DESTRUCTIBLE_ITEM, ItemDataType.ITEM, new ItemData(itemData));
        });

        return newItemStack;
    }
}