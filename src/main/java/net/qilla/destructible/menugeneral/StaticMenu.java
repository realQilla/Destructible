package net.qilla.destructible.menugeneral;

import com.google.common.base.Preconditions;
import net.qilla.destructible.menugeneral.slot.Slot;
import net.qilla.destructible.menugeneral.slot.Slots;
import net.qilla.destructible.menugeneral.slot.Socket;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.stream.IntStream;

public abstract class StaticMenu implements InventoryHolder {

    private final Inventory inventory;
    private final DPlayer dPlayer;
    private final Map<Integer, Socket> socketHolder;
    private final List<Integer> totalIndexes;

    public StaticMenu(@NotNull DPlayer dPlayer) {
        Preconditions.checkNotNull(dPlayer, "DPlayer cannot be null");
        this.inventory = Bukkit.createInventory(this, staticConfig().menuSize().getSize(), staticConfig().title());
        this.dPlayer = dPlayer;
        this.socketHolder = new HashMap<>(staticConfig().menuSize().getSize());
        this.totalIndexes = IntStream.range(0, staticConfig().menuSize().getSize()).boxed().toList();

        totalIndexes.forEach(index -> inventory.setItem(index, Slots.FILLER.getItem()));
        this.addSocket(menuSocket(), 0);
        this.addSocket(returnSocket(), 0);
    }

    public void finalizeMenu() {
        this.totalIndexes.stream()
                .filter(index -> !this.socketHolder.containsKey(index))
                .forEach(index -> this.inventory.addItem(Slots.FILLER.getItem()));
    }

    public void open(boolean toHistory) {
        dPlayer.getCraftPlayer().openInventory(this.inventory);
        if(toHistory) dPlayer.getMenuData().pushToHistory(this);
    }

    public void close() {
        dPlayer.getCraftPlayer().closeInventory();
    }

    public void handleClick(@NotNull InventoryClickEvent event) {
        Preconditions.checkNotNull(event, "InventoryClickEvent cannot be null");
        if(dPlayer.getCooldown().has(CooldownType.MENU_CLICK)) return;
        dPlayer.getCooldown().set(CooldownType.MENU_CLICK);

        Socket socket = socketHolder.get(event.getSlot());
        if(socket != null) socket.onClick(dPlayer, event);
    }

    public boolean returnMenu() {
        if(dPlayer.getCooldown().has(CooldownType.OPEN_MENU)) return false;
        dPlayer.getCooldown().set(CooldownType.OPEN_MENU);

        Optional<StaticMenu> optional = dPlayer.getMenuData().popFromHistory();

        if(optional.isEmpty()) {
            this.close();
            return true;
        }
        StaticMenu menu = optional.get();
        menu.refreshSockets();
        menu.open(false);
        return true;
    }

    public Socket addSocket(@NotNull Socket socket) {
        Preconditions.checkNotNull(socket, "Socket cannot be null");
        Slot slot = socket.slot();

        socketHolder.put(socket.index(), socket);
        inventory.setItem(socket.index(), slot.getItem());
        return socket;
    }

    public Socket addSocket(@NotNull Socket socket, int delayMillis) {
        Preconditions.checkNotNull(socket, "Socket cannot be null");
        Slot slot = socket.slot();

        Runnable runnable = () -> {
            addSocket(socket);
            inventory.setItem(socket.index(), slot.getItem());
            dPlayer.playSound(slot.getAppearSound(), true);
        };

        if(delayMillis > 0) {
            Bukkit.getScheduler().runTaskAsynchronously(dPlayer.getPlugin(), () -> {
                try {
                    Thread.sleep(delayMillis);
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Bukkit.getScheduler().runTask(dPlayer.getPlugin(), runnable);
            });
        } else runnable.run();
        return socket;
    }

    public List<Socket> addSocket(@NotNull List<Socket> sockets) {
        sockets.forEach(this::addSocket);
        return sockets;
    }

    public List<Socket> addSocket(@NotNull List<Socket> sockets, int delayMillis) {
        Preconditions.checkNotNull(sockets, "Socket list cannot be null");

        Bukkit.getScheduler().runTaskAsynchronously(dPlayer.getPlugin(), () -> {
            sockets.forEach(socket -> {
                try {
                    Thread.sleep(delayMillis);
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Bukkit.getScheduler().runTask(dPlayer.getPlugin(), () -> addSocket(socket, 0));
            });
        });
        return sockets;
    }

    public Socket removeSocket(int index) {
        inventory.setItem(index, Slots.FILLER.getItem());
        return socketHolder.remove(index);
    }

    public void inventoryClickEvent(InventoryClickEvent event) {
        event.setCancelled(true);
    }

    public void inventoryOpenEvent(InventoryOpenEvent event) {
    }

    public void inventoryCloseEvent(InventoryCloseEvent event) {
        if(!event.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) {
            dPlayer.getMenuData().clearHistory();
            inventory.clear();
        }
    }

    private Socket returnSocket() {
        return new Socket(staticConfig().returnIndex(), Slots.RETURN, event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                return this.returnMenu();
            } else return false;
        });
    }

    public DPlayer getDPlayer() {
        return this.dPlayer;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public List<Integer> getTotalIndexes() {
        return this.totalIndexes;
    }

    public abstract void refreshSockets();
    public abstract Socket menuSocket();
    public abstract StaticConfig staticConfig();
}