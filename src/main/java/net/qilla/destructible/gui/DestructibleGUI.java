package net.qilla.destructible.gui;

import net.kyori.adventure.text.Component;
import net.qilla.destructible.player.Cooldown;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;

public abstract class DestructibleGUI implements InventoryHolder {

    protected final DPlayer dPlayer;
    protected final Inventory inventory;
    protected SlotHolder slotHolder = new SlotHolder();

    public DestructibleGUI(DPlayer dPlayer, GUISize size, Component title) {
        this.dPlayer = dPlayer;
        this.inventory = Bukkit.createInventory(this, size.getSize(), title);
    }

    public abstract void onClick(InventoryInteractEvent event);

    public void handleClick(InventoryClickEvent event) {

        if(dPlayer.hasCooldown(Cooldown.MENU_CLICK)) return;
        dPlayer.setCooldown(Cooldown.MENU_CLICK);

        int slotIndex = event.getSlot();
        Slot slot = this.slotHolder.getSlot(slotIndex);
        if(slot != null) {
            slot.onClick();
        }
    }

    public void open() {
        this.dPlayer.openInventory(this.inventory);
    }

    public abstract void onOpen(InventoryOpenEvent event);

    public abstract void onClose(InventoryCloseEvent event);

    public void setSlots(List<Slot> slots) {
        slots.forEach(slot -> this.inventory.setItem(slot.getIndex(), slot.getItemStack()));
    }

    public void setSlot(Slot slot) {
        this.inventory.setItem(slot.getIndex(), slot.getItemStack());
    }

    public void unsetSlot(int slot) {
        this.inventory.clear(slot);
    }

    public void unsetSlots(List<Integer> slots) {
        slots.forEach(this.inventory::clear);
    }

    public void unsetAllSlots() {
        this.slotHolder.clearSlots();
        this.inventory.clear();
    }
}
