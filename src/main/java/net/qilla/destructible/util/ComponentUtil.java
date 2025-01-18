package net.qilla.destructible.util;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.qilla.destructible.mining.item.DItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ComponentUtil {

    private ComponentUtil() {
    }

    public static @NotNull String cleanComponent(@NotNull Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    public static @NotNull Component pluralizer(@NotNull Component component, int amount) {
        return amount == 1 ? component : component.append(Component.text("'s"));
    }

    public static @NotNull Component getItemAmountAndType(@NotNull DItem dItem, int amount) {
        Preconditions.checkNotNull(dItem, "DItem cannot be null");
        net.kyori.adventure.text.Component itemName = dItem.getDisplayName() != null ? dItem.getDisplayName() : net.kyori.adventure.text.Component.text("Unknown Item");
        return formatItemAmountAndType(amount, itemName);
    }

    public static @NotNull Component getItemAmountAndType(@NotNull ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "ItemStack cannot be null");
        net.kyori.adventure.text.Component itemName = itemStack.getData(DataComponentTypes.ITEM_NAME) != null
                ? itemStack.getData(DataComponentTypes.ITEM_NAME)
                : net.kyori.adventure.text.Component.text("Unknown Item");
        return formatItemAmountAndType(itemStack.getAmount(), itemName);
    }

    private static @NotNull Component formatItemAmountAndType(int amount, @NotNull Component itemName) {
        return MiniMessage.miniMessage()
                .deserialize("<white>" + amount + "x ")
                .append(pluralizer(itemName, amount));
    }
}
