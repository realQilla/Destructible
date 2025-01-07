package net.qilla.destructible.menus;

import net.kyori.adventure.text.Component;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.SlotType;
import net.qilla.destructible.menus.slot.Socket;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.player.PlayType;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public abstract class DestructibleMenu implements InventoryHolder {

    private final DPlayer dPlayer;
    private final Inventory inventory;
    private final Socket socket;

    public DestructibleMenu(DPlayer dPlayer, MenuSize size, Component title) {
        this.dPlayer = dPlayer;
        this.inventory = Bukkit.createInventory(this, size.getSize(), title);
        this.socket = new Socket(size);
    }

    public void handleClick(InventoryClickEvent event) {
        if(dPlayer.getCooldown().has(CooldownType.MENU_CLICK)) return;
        dPlayer.getCooldown().set(CooldownType.MENU_CLICK);

        Slot slot = this.socket.get(event.getSlot());
        if(slot == null) return;
        slot.onClick(event.getClick());
    }

    public void returnToPreviousMenu() {
        if(dPlayer.getCooldown().has(CooldownType.OPEN_MENU)) return;
        dPlayer.getCooldown().set(CooldownType.OPEN_MENU);

        dPlayer.playSound(SoundSettings.of(Sound.BLOCK_NOTE_BLOCK_BELL, 0.33f, 1.25f, SoundCategory.PLAYERS, PlayType.PLAYER), true);
        DestructibleMenu lastMenu = dPlayer.getMenuData().getLastMenu();
        if(lastMenu != null) lastMenu.reopenInventory();
        else this.close();
    }

    public void openInventory() {
        dPlayer.getMenuData().addMenu(this);
        dPlayer.getCraftPlayer().openInventory(this.inventory);
    }

    public void reopenInventory() {
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

    public Socket getSocket() {
        return this.socket;
    }

    public void register(Slot slot) {
        this.socket.register(slot);
        this.inventory.setItem(slot.getIndex(), slot.getDisplay().get());
    }

    public void register(Slot slot, SlotType slotType) {
        this.socket.register(slot, slotType);
        this.inventory.setItem(slot.getIndex(), slot.getDisplay().get());
    }

    public void unregister(int index) {
        this.socket.unregister(index);
        this.inventory.clear(index);
    }

    public void unregister(SlotType slotType) {
        Slot slot = this.socket.get(slotType);
        this.socket.unregister(slotType);
        if(slot == null) return;
        this.socket.unregister(slot.getIndex());
        this.inventory.clear(slot.getIndex());
    }

    public void clear() {
        this.socket.clear();
        this.inventory.clear();
    }

    public DPlayer getDPlayer() {
        return this.dPlayer;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}