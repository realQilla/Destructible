package net.qilla.destructible.menus;

import com.google.common.base.Preconditions;

import java.util.*;

public final class SlotHolder {

    private static final int CAPACITY = 54;
    private final Map<Integer, Slot> slotHolder = new HashMap<>(CAPACITY);

    public SlotHolder() {
    }

    public void registerSlot(Slot slot) {
        Preconditions.checkNotNull(slot, "Slot cannot be null");
        slotHolder.put(slot.getIndex(), slot);
    }

    public void unregisterSlot(int index) {
        slotHolder.remove(index);
    }

    public Slot getSlot(int index) {
        return slotHolder.get(index);
    }

    public Map<Integer, Slot> getSlots() {
        return Collections.unmodifiableMap(slotHolder);
    }

    public List<Integer> getRemainingSlots(List<Integer> slotsToCheck) {
        List<Integer> unregisteredSlots = new ArrayList<>();
        for(int slot : slotsToCheck) {
            if(!slotHolder.containsKey(slot)) {
                unregisteredSlots.add(slot);
            }
        }
        return unregisteredSlots;
    }

    public List<Integer> getRemainingSlots() {
        List<Integer> unregisteredSlots = new ArrayList<>();
        for(int i = 0; i < CAPACITY; i++) {
            if(!slotHolder.containsKey(i)) {
                unregisteredSlots.add(i);
            }
        }
        return unregisteredSlots;
    }

    public void clearSlots() {
        slotHolder.clear();
    }
}