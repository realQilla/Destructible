package net.qilla.destructible.gui;

import java.util.*;

public final class SlotHolder {

    private final Map<Integer, Slot> slotHolder = new HashMap<>(54);

    public SlotHolder() {
    }

    public void registerSlot(Slot slot) {
        slotHolder.put(slot.getIndex(), slot);
    }

    public void unregisterSlot(int index) {
        slotHolder.remove(index);
    }

    public Slot getSlot(int index) {
        return slotHolder.get(index);
    }

    public Map<Integer, Slot> getSlots() {
        return slotHolder;
    }

    public void clearSlots() {
        slotHolder.clear();
    }
}