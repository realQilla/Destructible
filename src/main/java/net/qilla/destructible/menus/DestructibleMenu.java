package net.qilla.destructible.menus;

import com.google.common.base.Preconditions;
import net.qilla.destructible.command.Sounds;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.Socket;
import net.qilla.destructible.menus.slot.SocketHolder;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public abstract class DestructibleMenu implements InventoryHolder, MenuConfig {

    private final DPlayer dPlayer;
    private final Inventory inventory;
    private final SocketHolder socketHolder;

    public DestructibleMenu(@NotNull DPlayer dPlayer) {
        Preconditions.checkNotNull(dPlayer, "DPlayer cannot be null");
        this.dPlayer = dPlayer;
        this.inventory = Bukkit.createInventory(this, menuSize().getSize(), tile());
        this.socketHolder = new SocketHolder(menuSize());
    }

    public void handleClick(@NotNull InventoryClickEvent event) {
        Preconditions.checkNotNull(event, "InventoryClickEvent cannot be null");
        if(dPlayer.getCooldown().has(CooldownType.MENU_CLICK)) return;
        dPlayer.getCooldown().set(CooldownType.MENU_CLICK);

        Slot slot = this.socketHolder.get(event.getSlot()).slot();
        if(slot != null) {
            slot.onClick(event);
            dPlayer.playSound(slot.getClickSound(), true);
        }
    }

    public void openMenu(boolean saveMenu) {
        if(saveMenu) dPlayer.getMenuData().addHistory(this);
        dPlayer.getCraftPlayer().openInventory(this.inventory);
    }

    public DestructibleMenu pullPreviousMenu() {
        return dPlayer.getMenuData().getLastMenu();
    }

    public void returnToPrevious() {
        if(dPlayer.getCooldown().has(CooldownType.OPEN_MENU)) return;
        dPlayer.getCooldown().set(CooldownType.OPEN_MENU);

        DestructibleMenu lastMenu = this.pullPreviousMenu();
        if(lastMenu != null) lastMenu.openMenu(false);
        else this.closeMenu();
    }

    public void closeMenu() {
        dPlayer.getCraftPlayer().closeInventory();
    }

    public void inventoryClickEvent(InventoryClickEvent event) {
        event.setCancelled(true);
    }

    public void inventoryOpenEvent(InventoryOpenEvent event) {
    }

    public void inventoryCloseEvent(InventoryCloseEvent event) {
        if(!event.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) {
            dPlayer.getMenuData().clearHistory();
        }
    }

    public SocketHolder getSocket() {
        return this.socketHolder;
    }

    public Socket register(Socket socket) {
        Preconditions.checkNotNull(socket, "Socket cannot be null");

        Runnable registerTask = () -> {
            socketHolder.register(socket);
            inventory.setItem(socket.index(), socket.slot().getDisplay().get());
            dPlayer.playSound(socket.slot().getAppearSound(), true);
        };

        if(socket.slot().getDelay() > 0) {
            Bukkit.getScheduler().runTaskLater(dPlayer.getPlugin(), registerTask, socket.slot().getDelay());
        } else {
            registerTask.run();
        }

        return socket;
    }

    public Socket unregister(int index) {
        this.inventory.clear(index);
        return this.socketHolder.unregister(index);
    }

    public DPlayer getDPlayer() {
        return this.dPlayer;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public Slot returnSlot() {
        return Slot.of(builder -> builder
                .display(Displays.RETURN)
                .action((slot, event) -> {
                    if(event.getClick().isLeftClick()) {
                        returnToPrevious();
                    }
                })
                .clickSound(Sounds.RETURN_MENU)
        );
    }
}