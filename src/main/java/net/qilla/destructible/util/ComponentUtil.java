package net.qilla.destructible.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.DItemStack;

public class ComponentUtil {

    public static Component getItem(DItem dItem, int amount) {
        return MiniMessage.miniMessage().deserialize("<white>" + amount + "x ").append(dItem.getDisplayName().asComponent());
    }

    public static Component getItem(DItemStack dItemStack) {
        return getItem(dItemStack.getDItem(), dItemStack.getAmount());
    }
}