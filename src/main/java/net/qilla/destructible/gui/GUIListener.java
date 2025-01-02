package net.qilla.destructible.gui;

import net.qilla.destructible.Destructible;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;

public class GUIListener implements Listener {

    private final Destructible plugin;

    public GUIListener(Destructible plugin) {
        super();
        this.plugin = plugin;
    }

    @EventHandler
    private void onInventoryInteract(InventoryInteractEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if(holder instanceof DestructibleGUI gui) {
            event.setCancelled(true);
            gui.onClick(event);
        }
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if(holder instanceof DestructibleGUI gui) {
            event.setCancelled(true);
            gui.handleClick(event);
        }
    }

    @EventHandler
    private void onInventoryOpen(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if(holder instanceof DestructibleGUI gui) {
            gui.onOpen(event);
        }
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if(holder instanceof DestructibleGUI gui) {
            gui.onClose(event);
        }
    }
}