package net.qilla.destructible.menus;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.UniqueSlot;
import net.qilla.destructible.menus.slot.Socket;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.RandomUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public abstract class DestructibleMenu implements InventoryHolder {

    private final DPlayer dPlayer;
    private final Inventory inventory;
    private final Socket socket;

    public DestructibleMenu(DPlayer dPlayer, MenuSize menuSize, Component title) {
        Preconditions.checkNotNull(dPlayer, "DPlayer cannot be null");
        this.dPlayer = dPlayer;
        this.inventory = Bukkit.createInventory(this, menuSize.getSize(), title);
        this.socket = new Socket(menuSize);
    }

    public void handleClick(InventoryClickEvent event) {
        Preconditions.checkNotNull(event, "InventoryClickEvent cannot be null");

        if(dPlayer.getCooldown().has(CooldownType.MENU_CLICK)) return;
        dPlayer.getCooldown().set(CooldownType.MENU_CLICK);

        Slot slot = this.socket.get(event.getSlot());
        if(slot != null) {
            slot.onClick(event);
            dPlayer.playSound(slot.getClickSound(), true);
        }
    }

    public void returnToPreviousMenu() {
        if(dPlayer.getCooldown().has(CooldownType.OPEN_MENU)) return;
        dPlayer.getCooldown().set(CooldownType.OPEN_MENU);

        DestructibleMenu lastMenu = dPlayer.getMenuData().getLastMenu();
        if(lastMenu != null) lastMenu.openInventory(false);
        else this.closeInventory();
    }

    public void openInventory(boolean saveMenu) {
        if(saveMenu) dPlayer.getMenuData().addMenu(this);
        dPlayer.getCraftPlayer().openInventory(this.inventory);
    }

    public void closeInventory() {
        dPlayer.getCraftPlayer().closeInventory();
    }

    public void inventoryClickEvent(InventoryClickEvent event) {
        event.setCancelled(true);
    }

    public void inventoryOpenEvent(InventoryOpenEvent event) {
    }

    public void onClose(InventoryCloseEvent event) {
        if(!event.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) {
            dPlayer.getMenuData().clearHistory();
        }
    }

    public Socket getSocket() {
        return this.socket;
    }

    public Slot register(Slot slot) {
        Preconditions.checkNotNull(slot, "Slot cannot be null");

        UniqueSlot uniqueSlot = slot.getUniqueSlot();
        if(uniqueSlot == null) this.socket.register(slot);
        else this.socket.register(slot, uniqueSlot);

        this.inventory.setItem(slot.getIndex(), slot.getDisplay().get());
        dPlayer.playSound(slot.getAppearSound(), true);

        return slot;
    }

    public Slot register(Slot slot, int delay) {
        Preconditions.checkNotNull(slot, "Slot cannot be null");

        Bukkit.getScheduler().runTaskLater(dPlayer.getPlugin(), () -> this.register(slot), delay);
        return slot;
    }

    public void register(List<Slot> slots, int delay) {
        Preconditions.checkNotNull(slots, "List cannot be null");

        Bukkit.getScheduler().runTaskAsynchronously(dPlayer.getPlugin(), () -> {
            slots.forEach(slot -> {
                Bukkit.getScheduler().runTask(dPlayer.getPlugin(), () -> {
                    this.register(slot);
                });
                try {
                    Thread.sleep(delay * 50L);
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    public Slot unregister(int index) {
        this.inventory.clear(index);
        return this.socket.unregister(index);
    }

    public void unregister(UniqueSlot uniqueSlot) {
        Slot slot = this.socket.get(uniqueSlot);
        this.socket.unregister(uniqueSlot);
        if(slot == null) return;
        this.socket.unregister(slot.getIndex());
        this.inventory.clear(slot.getIndex());
    }

    public DPlayer getDPlayer() {
        return this.dPlayer;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    protected abstract void populateMenu();
}