package net.qilla.destructible.menus.slot;

import com.google.common.base.Preconditions;
import net.qilla.destructible.menus.MenuSize;

import java.util.*;

public final class Socket {

    private final MenuSize menuSize;
    private final Map<Integer, Slot> socket;
    private final Map<SlotType, Slot> uniqueSocket = new HashMap<>();

    public Socket(MenuSize menuSize) {
        this.menuSize = menuSize;
        this.socket = new HashMap<>(menuSize.getSize());
    }

    public void register(Slot slot) {
        Preconditions.checkNotNull(slot, "Slot cannot be null");
        socket.put(slot.getIndex(), slot);
    }

    public void register(Slot slot, SlotType slotType) {
        Preconditions.checkNotNull(slot, "Slot cannot be null");
        socket.put(slot.getIndex(), slot);
        uniqueSocket.put(slotType, slot);
    }

    public void unregister(int index) {
        socket.remove(index);
    }

    public void unregister(SlotType slotType) {
        socket.remove(uniqueSocket.get(slotType).getIndex());
        uniqueSocket.remove(slotType);
    }

    public Slot get(int index) {
        return socket.get(index);
    }

    public Slot get(SlotType slotType) {
        return uniqueSocket.get(slotType);
    }

    public Map<Integer, Slot> getAll() {
        return Collections.unmodifiableMap(socket);
    }

    public List<Integer> getRemaining(List<Integer> slotsToCheck) {
        List<Integer> unregisteredSlots = new ArrayList<>();
        for(int slot : slotsToCheck) {
            if(!socket.containsKey(slot)) {
                unregisteredSlots.add(slot);
            }
        }
        return unregisteredSlots;
    }

    public List<Integer> getRemaining() {
        List<Integer> unregisteredSlots = new ArrayList<>();
        for(int i = 0; i < menuSize.getSize(); i++) {
            if(!socket.containsKey(i)) {
                unregisteredSlots.add(i);
            }
        }
        return unregisteredSlots;
    }

    public void clear() {
        socket.clear();
        uniqueSocket.clear();
    }
}