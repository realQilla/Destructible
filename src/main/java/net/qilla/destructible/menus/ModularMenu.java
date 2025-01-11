package net.qilla.destructible.menus;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.qilla.destructible.command.Sounds;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class ModularMenu<T> extends DestructibleMenu {

    private final List<Integer> modularSlots;
    private Collection<T> itemPopulation;
    private int shiftIndex;
    private final Slot nextSlot;
    private final Slot previousSlot;

    public ModularMenu(DPlayer dPlayer, MenuSize size, Component title, List<Integer> modularSlots, Collection<T> itemPopulation) {
        super(dPlayer, size, title);
        Preconditions.checkNotNull(modularSlots, "Modular slots list cannot be null");
        Preconditions.checkNotNull(itemPopulation, "Item population list cannot be null");
        this.itemPopulation = itemPopulation;
        this.modularSlots = modularSlots;
        this.shiftIndex = 0;
        this.nextSlot = getNextSlot();
        this.previousSlot = getPreviousSlot();

        if(itemPopulation.size() > modularSlots.size()) super.register(getNextSlot());
    }

    protected void populateModular() {
        int fromIndex = Math.min(this.shiftIndex, this.itemPopulation.size());
        int toIndex = Math.min(fromIndex + modularSlots.size(), this.itemPopulation.size());
        List<T> shiftedList = new ArrayList<>(this.itemPopulation).subList(fromIndex, toIndex);

        Iterator<Integer> iterator = modularSlots.iterator();
        shiftedList.forEach(item -> {
            if(iterator.hasNext()) {
                super.register(createSlot(iterator.next(), item));
            }
        });

        super.getSocket().getRemaining(modularSlots).forEach(slotNum -> super.register(Slot.of(slotNum, Displays.EMPTY_SLOT)));
        super.getSocket().getRemaining().forEach(slotNum -> super.register(Slot.of(slotNum, Displays.FILLER)));
    }

    public void rotateNext(Slot slot, InventoryClickEvent event, int amount) {
        ClickType clickType = event.getClick();
        if(clickType.isShiftClick() && clickType.isLeftClick()) this.shiftIndex += modularSlots.size();
        else if(clickType.isLeftClick()) this.shiftIndex += amount;

        //if(shiftIndex + modularSlots.size() > itemPopulation.size()) shiftIndex = (itemPopulation.size() - modularSlots.size() + (itemPopulation.size() % modularSlots.size()));

        this.updateModular();
        super.getDPlayer().playSound(Sounds.MENU_NEXT, true);
    }

    public void rotatePrevious(Slot slot, InventoryClickEvent event, int amount) {
        ClickType clickType = event.getClick();
        if(clickType.isShiftClick() && clickType.isLeftClick()) this.shiftIndex -= modularSlots.size();
        else if(clickType.isLeftClick()) this.shiftIndex -= amount;

        if(shiftIndex < 0) shiftIndex = 0;

        this.updateModular();
        super.getDPlayer().playSound(Sounds.MENU_PREVIOUS, true);
    }

    public void updateModular() {
        modularSlots.forEach(super::unregister);

        if(shiftIndex > 0) super.register(this.previousSlot);
        else super.unregister(this.previousSlot.getIndex());
        if(shiftIndex + modularSlots.size() < itemPopulation.size()) super.register(this.nextSlot);
        else super.unregister(this.nextSlot.getIndex());

        populateModular();
    }

    public List<Integer> getModularSlots() {
        return this.modularSlots;
    }

    public Collection<T> getItemPopulation() {
        return this.itemPopulation;
    }

    public int getShiftIndex() {
        return shiftIndex;
    }

    public void resetIndex() {
        this.shiftIndex = 0;
    }

    protected void setItemPopulation(List<T> newItemPopulation) {
        this.itemPopulation = newItemPopulation;
    }

    protected abstract Slot getNextSlot();
    protected abstract Slot getPreviousSlot();
    protected abstract Slot createSlot(int index, T item);
}