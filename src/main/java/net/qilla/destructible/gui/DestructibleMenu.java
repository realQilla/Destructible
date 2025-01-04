package net.qilla.destructible.gui;

import net.kyori.adventure.text.Component;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;

public abstract class DestructibleMenu implements InventoryHolder {

    private final DPlayer dPlayer;
    private final Inventory inventory;
    private final SlotHolder slotHolder;

    public DestructibleMenu(DPlayer dPlayer, GUISize size, Component title) {
        this.dPlayer = dPlayer;
        this.inventory = Bukkit.createInventory(this, size.getSize(), title);
        this.slotHolder = new SlotHolder();
    }

    public void handleClick(InventoryClickEvent event) {
        if(dPlayer.getCooldown().has(CooldownType.MENU_CLICK)) return;
        dPlayer.getCooldown().set(CooldownType.MENU_CLICK);

        int slotIndex = event.getSlot();
        Slot slot = this.slotHolder.getSlot(slotIndex);
        if(slot != null) {
            slot.onClick();
        }
    }

    public void returnToPreviousMenu() {
        if(dPlayer.getCooldown().has(CooldownType.OPEN_MENU)) return;
        dPlayer.getCooldown().set(CooldownType.OPEN_MENU);

        DestructibleMenu lastMenu = dPlayer.getMenuData().getLastMenu();
        if(lastMenu != null) lastMenu.reopenInventory();
        else this.close();
    }

    public void openInventory() {
        dPlayer.getMenuData().addMenu(this);
        dPlayer.getCraftPlayer().openInventory(this.inventory);
    }

    private void reopenInventory() {
        dPlayer.getCraftPlayer().openInventory(this.inventory);
    }

    public void close() {
        dPlayer.getCraftPlayer().closeInventory();
    }

    public abstract void onInteract(InventoryInteractEvent event);

    public abstract void onOpen(InventoryOpenEvent event);

    public void onClose(InventoryCloseEvent event) {
        if(!event.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) {
            dPlayer.getMenuData().clearHistory();
        }
    }

    public void setSlot(Slot slot) {
        this.slotHolder.registerSlot(slot);
        this.inventory.setItem(slot.getIndex(), slot.getItemStack());
    }

    public void setSlots(List<Slot> slots) {
        slots.forEach(this::setSlot);
    }

    public void unsetSlot(int slot) {
        this.slotHolder.unregisterSlot(slot);
        this.inventory.clear(slot);
    }

    public void unsetSlots(List<Integer> slots) {
        slots.forEach(this::unsetSlot);
    }

    public void unsetAllSlots() {
        this.slotHolder.clearSlots();
        this.inventory.clear();
    }

    public DPlayer getDPlayer() {
        return this.dPlayer;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}