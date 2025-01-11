package net.qilla.destructible.menus.slot;

import com.google.common.base.Preconditions;
import net.qilla.destructible.menus.MenuSize;

import java.util.*;

public final class Socket {

    private final MenuSize menuSize;
    private final Map<Integer, Slot> socket;
    private final Map<UniqueSlot, Slot> uniqueSocket = new HashMap<>();

    public Socket(MenuSize menuSize) {
        this.menuSize = menuSize;
        this.socket = new HashMap<>(menuSize.getSize());
    }

    public Slot register(Slot slot) {
        Preconditions.checkNotNull(slot, "Slot cannot be null");
        socket.put(slot.getIndex(), slot);
        return slot;
    }

    public Slot register(Slot slot, UniqueSlot uniqueSlot) {
        Preconditions.checkNotNull(slot, "Slot cannot be null");
        socket.put(slot.getIndex(), slot);
        uniqueSocket.put(uniqueSlot, slot);
        return slot;
    }

    public Slot unregister(int index) {
        return socket.remove(index);
    }

    public Slot unregister(UniqueSlot uniqueSlot) {
        socket.remove(uniqueSocket.get(uniqueSlot).getIndex());
        return uniqueSocket.remove(uniqueSlot);
    }

    public Slot get(int index) {
        return socket.get(index);
    }

    public Slot get(UniqueSlot uniqueSlot) {
        return uniqueSocket.get(uniqueSlot);
    }

    public List<Integer> getRemaining(List<Integer> slotsToCheck) {
        List<Integer> unregisteredSlots = new LinkedList<>();
        for(int slot : slotsToCheck) {
            if(!socket.containsKey(slot)) {
                unregisteredSlots.add(slot);
            }
        }
        return unregisteredSlots;
    }

    public List<Integer> getRemaining() {
        List<Integer> unregisteredSlots = new LinkedList<>();
        for(int i = 0; i < menuSize.getSize(); i++) {
            if(!socket.containsKey(i)) {
                unregisteredSlots.add(i);
            }
        }
        return unregisteredSlots;
    }
}