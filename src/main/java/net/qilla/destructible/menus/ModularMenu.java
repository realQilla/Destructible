package net.qilla.destructible.menus;

import net.kyori.adventure.text.Component;
import net.qilla.destructible.mining.item.DItemStack;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.event.inventory.ClickType;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class ModularMenu extends DestructibleMenu {

    private final List<Integer> modularSlots;
    private final Collection<DItemStack> itemPopulation;
    private int shiftIndex = 0;

    public ModularMenu(DPlayer dPlayer, GUISize size, Component title, Collection<DItemStack> itemPopulation, List<Integer> modularSlots) {
        super(dPlayer, size, title);
        this.modularSlots = modularSlots;
        this.itemPopulation = itemPopulation;

        this.populateModular();
    }

    public void rotateNext(Slot slotInfo, ClickType clickType, int amount) {
        if(clickType.isShiftClick()) this.shiftIndex += modularSlots.size();
        else this.shiftIndex += amount;
        this.refresh();
    }
    public void rotatePrevious(Slot slotInfo, ClickType clickType, int amount) {
        if(clickType.isShiftClick()) this.shiftIndex -= modularSlots.size();
        else this.shiftIndex -= amount;

        if(shiftIndex < 0) shiftIndex = 0;

        this.refresh();
    }

    public void populateModular() {
        List<DItemStack> shiftedList = new LinkedList<>(itemPopulation).subList(shiftIndex, Math.min(shiftIndex + modularSlots.size(), itemPopulation.size()));

        Iterator<Integer> iterator = modularSlots.iterator();
        shiftedList.iterator().forEachRemaining(item -> {
            if(iterator.hasNext()) {
                setSlot(createSlot(iterator.next(), item));
            }
        });
        super.getSlotHolder().getRemainingSlots(modularSlots).forEach(slotNum -> super.setSlot(Slots.EMPTY_ITEM.index(slotNum).build()));
        super.getSlotHolder().getRemainingSlots().forEach(slotNum -> super.setSlot(Slots.FILLER_ITEM.index(slotNum).build()));
    }

    public void refresh() {
        super.unsetSlots(modularSlots);
        populateMenu();
        populateModular();
    }

    public abstract Slot createSlot(int index, DItemStack item);

    public List<Integer> getModularSlots() {
        return this.modularSlots;
    }

    public Collection<DItemStack> getItemPopulation() {
        return this.itemPopulation;
    }

    public int getShiftIndex() {
        return this.shiftIndex;
    }

    public abstract void populateMenu();
}
