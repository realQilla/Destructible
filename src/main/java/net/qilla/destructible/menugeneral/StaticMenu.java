package net.qilla.destructible.menugeneral;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.Sounds;
import net.qilla.destructible.menugeneral.input.SignInput;
import net.qilla.destructible.menugeneral.slot.Slot;
import net.qilla.destructible.menugeneral.slot.Slots;
import net.qilla.destructible.menugeneral.slot.Socket;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.DItems;
import net.qilla.destructible.mining.item.ItemStackFactory;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.ComponentUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.stream.IntStream;

public abstract class StaticMenu implements InventoryHolder {

    private final Destructible plugin;
    private final Inventory inventory;
    private final StaticConfig staticConfig;
    private final DPlayer dPlayer;
    private final Map<Integer, Socket> socketHolder = new HashMap<>();
    private final List<Integer> totalIndexes = IntStream.range(0, staticConfig().menuSize().getSize()).boxed().toList();

    protected StaticMenu(@NotNull Destructible plugin, @NotNull DPlayer dPlayer) {
        Preconditions.checkNotNull(plugin, "Plugin cannot be null");
        Preconditions.checkNotNull(dPlayer, "DPlayer cannot be null");
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(this, staticConfig().menuSize().getSize(), staticConfig().title());
        this.staticConfig = staticConfig();
        this.dPlayer = dPlayer;

        totalIndexes.forEach(index -> inventory.setItem(index, Slots.FILLER.getItem()));
        this.addSocket(menuSocket());
        this.addSocket(returnSocket());
    }

    public void finalizeMenu() {
        totalIndexes.stream()
                .filter(index -> !socketHolder.containsKey(index))
                .forEach(index -> inventory.setItem(index, Slots.FILLER.getItem()));
    }

    public void open(boolean toHistory) {
        dPlayer.getCraftPlayer().openInventory(inventory);
        if(toHistory) dPlayer.getMenuHolder().pushToHistory(this);
    }

    public void close() {
        dPlayer.getCraftPlayer().closeInventory();
    }

    public void handleClick(@NotNull InventoryClickEvent event) {
        Preconditions.checkNotNull(event, "InventoryClickEvent cannot be null");
        Socket socket = socketHolder.get(event.getSlot());
        if(socket != null) {
            socket.onClick(dPlayer, event);
        }
    }

    public boolean returnMenu() {
        Optional<StaticMenu> optional = dPlayer.getMenuHolder().popFromHistory();
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
                Bukkit.getScheduler().runTaskLater(plugin, runnable, delayMillis / 50);
        } else runnable.run();
        return socket;
    }

    public List<Socket> addSocket(@NotNull List<Socket> sockets) {
        sockets.forEach(this::addSocket);
        return sockets;
    }

    public List<Socket> addSocket(@NotNull List<Socket> sockets, int delayMillis) {
        Preconditions.checkNotNull(sockets, "Socket list cannot be null");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            sockets.forEach(socket -> {
                try {
                    Thread.sleep(delayMillis);
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Bukkit.getScheduler().runTask(plugin, () -> addSocket(socket, 0));
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
        if(event.getClickedInventory().getHolder() instanceof StaticMenu) {
            this.handleClick(event);
        }
    }

    public void inventoryOpenEvent(InventoryOpenEvent event) {
    }

    public void inventoryCloseEvent(InventoryCloseEvent event) {
    }

    private Socket returnSocket() {
        return new Socket(staticConfig.returnIndex(), Slots.RETURN, event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                return this.returnMenu();
            } else return false;
        }, CooldownType.OPEN_MENU);
    }

    public void getItem(@NotNull DItem item) {
        getDPlayer().give(ItemStackFactory.of(item, 1));
        getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>You received ").append(ComponentUtil.getItemAmountAndType(item, 1)).append(MiniMessage.miniMessage().deserialize("!")));
    }

    public void getItemAmount(@NotNull List<String> signText, @NotNull DItem dItem) {
        SignInput signInput = new SignInput(plugin, getDPlayer(), signText);
        signInput.init(result -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    int value = Integer.parseInt(result);

                    getDPlayer().give(ItemStackFactory.of(dItem, value));
                    getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>You received ").append(ComponentUtil.getItemAmountAndType(dItem, value)).append(MiniMessage.miniMessage().deserialize("!")));
                    getDPlayer().playSound(Sounds.SIGN_INPUT, true);
                } catch(NumberFormatException ignored) {
                }
                this.open(false);
            });
        });
    }

    public Destructible getPlugin() {
        return this.plugin;
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