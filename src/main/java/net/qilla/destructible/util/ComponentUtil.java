package net.qilla.destructible.util;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.mining.item.DItem;
import org.bukkit.inventory.ItemStack;

public class ComponentUtil {

    public static Component getItem(DItem dItem, int amount) {
        Preconditions.checkNotNull(dItem, "DItem cannot be null");
        return MiniMessage.miniMessage().deserialize("<white>" + amount + "x ").append(dItem.getDisplayName().asComponent());
    }

    public static Component getItem(ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "ItemStack cannot be null");

        return MiniMessage.miniMessage().deserialize("<white>" + itemStack.getAmount() + "x ").append(itemStack.getData(DataComponentTypes.ITEM_NAME));
    }
}