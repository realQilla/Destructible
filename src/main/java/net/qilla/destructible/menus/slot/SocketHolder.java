package net.qilla.destructible.menus.slot;

import com.google.common.base.Preconditions;
import net.qilla.destructible.menus.MenuSize;

import java.util.*;

public final class SocketHolder {

    private final MenuSize menuSize;
    private final Map<Integer, Socket> socketHolder;
    private final Map<UniqueSlot, Socket> uniqueSocket;

    public SocketHolder(MenuSize menuSize) {
        this.menuSize = menuSize;
        this.socketHolder = new HashMap<>(menuSize.getSize());
        this.uniqueSocket = new HashMap<>(menuSize.getSize());
    }

    public Socket register(Socket socket) {
        Preconditions.checkNotNull(socket, "Socket cannot be null");
        return socketHolder.put(socket.index(), socket);
    }

    public Socket unregister(int index) {
        return socketHolder.remove(index);
    }

    public Socket get(int index) {
        return socketHolder.get(index);
    }

    public List<Integer> getRemaining(List<Integer> slotsToCheck) {
        List<Integer> unregisteredSlots = new LinkedList<>();
        for(int slot : slotsToCheck) {
            if(!socketHolder.containsKey(slot)) {
                unregisteredSlots.add(slot);
            }
        }
        return unregisteredSlots;
    }

    public List<Integer> getRemaining() {
        List<Integer> unregisteredSlots = new LinkedList<>();
        for(int i = 0; i < menuSize.getSize(); i++) {
            if(!socketHolder.containsKey(i)) {
                unregisteredSlots.add(i);
            }
        }
        return unregisteredSlots;
    }
}