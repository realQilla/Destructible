package net.qilla.destructible.menugeneral;

import net.qilla.destructible.Destructible;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;

public class MenuListener implements Listener {

    private final Destructible plugin;

    public MenuListener(Destructible plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onInventoryInteract(InventoryInteractEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if(holder instanceof StaticMenu) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent event) {
        if(event.getClickedInventory() == null) return;
        if(event.getInventory().getHolder() instanceof StaticMenu menu) {
            menu.inventoryClickEvent(event);
        }

        InventoryHolder holder = event.getClickedInventory().getHolder();

        if(holder instanceof StaticMenu menu) {
            menu.inventoryClickEvent(event);
            menu.handleClick(event);
        }
    }

    @EventHandler
    private void onInventoryOpen(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if(holder instanceof StaticMenu menu) {
            menu.inventoryOpenEvent(event);
        }
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if(holder instanceof StaticMenu menu) {
            menu.inventoryCloseEvent(event);
        }
    }
}