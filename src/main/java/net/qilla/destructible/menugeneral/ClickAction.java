package net.qilla.destructible.menugeneral;

import org.bukkit.event.inventory.InventoryClickEvent;

@FunctionalInterface
public interface ClickAction {

    boolean onClick(InventoryClickEvent event);
}
