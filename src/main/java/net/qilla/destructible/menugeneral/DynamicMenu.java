package net.qilla.destructible.menugeneral;

import net.qilla.destructible.menugeneral.slot.Slots;
import net.qilla.destructible.menugeneral.slot.Socket;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class DynamicMenu<T> extends StaticMenu {

    private final List<Integer> dynamicSlots;
    private final Collection<T> itemPopulation;
    private int shiftIndex;

    public DynamicMenu(@NotNull DPlayer dPlayer, @NotNull Collection<T> itemPopulation) {
        super(dPlayer);
        this.itemPopulation = itemPopulation;
        this.dynamicSlots = dynamicConfig().dynamicIndexes();
        this.shiftIndex = 0;

        if(itemPopulation.size() > dynamicSlots.size()) super.addSocket(nextSocket(), 5);
    }

    public void finalizeMenu() {
        getTotalIndexes().stream()
                .filter(index -> !dynamicSlots.contains(index))
                .forEach(index -> super.getInventory().addItem(Slots.FILLER.getItem()));
    }

    public void populateModular() {
        int fromIndex = Math.min(this.shiftIndex, this.itemPopulation.size());
        int toIndex = Math.min(fromIndex + dynamicSlots.size(), this.itemPopulation.size());
        List<T> shiftedList = new ArrayList<>(this.itemPopulation).subList(fromIndex, toIndex);

        Iterator<Integer> iterator = dynamicSlots.iterator();
        for(T item : shiftedList) {
            if(iterator.hasNext()) {
                super.addSocket(createSocket(iterator.next(), item), 0);
            }
        }
        iterator.forEachRemaining(index -> super.addSocket(new Socket(index, Slots.EMPTY_MODULAR_SLOT), 0));
    }

    public void rotateNext(InventoryClickEvent event, int amount) {
        ClickType clickType = event.getClick();
        if(clickType.isShiftClick() && clickType.isLeftClick()) {
            for(int i = 0; i < dynamicSlots.size() / amount; i++) {
                if((shiftIndex += amount) + dynamicSlots.size() > itemPopulation.size()) {
                    if((shiftIndex + dynamicSlots.size()) % itemPopulation.size() >= amount) shiftIndex -= amount;
                    break;
                }
            }
        } else if(clickType.isLeftClick()) shiftIndex += amount;

        if(shiftIndex > 0) super.addSocket(previousSocket(), 0);
        else super.removeSocket(previousSocket().index());

        refreshSockets();
    }

    public void rotatePrevious(InventoryClickEvent event, int amount) {
        ClickType clickType = event.getClick();
        if(clickType.isShiftClick() && clickType.isLeftClick()) {
            for(int i = 0; i < dynamicSlots.size() / amount; i++) {
                if((shiftIndex -= amount) < 0) break;
            }
        }
        else if(clickType.isLeftClick()) shiftIndex -= amount;
        if(shiftIndex < 0) shiftIndex = 0;

        refreshSockets();
    }

    @Override
    public void refreshSockets() {
        if(shiftIndex + dynamicSlots.size() < itemPopulation.size()) super.addSocket(nextSocket(), 5);
        else super.removeSocket(nextSocket().index());
        if(shiftIndex > 0) super.addSocket(previousSocket(), 5);
        else super.removeSocket(previousSocket().index());

        populateModular();
    }

    public Collection<T> getItemPopulation() {
        return itemPopulation;
    }

    public int getShiftIndex() {
        return shiftIndex;
    }

    public void setShiftIndex(int shiftIndex) {
        this.shiftIndex = shiftIndex;
    }

    private Socket nextSocket() {
        return new Socket(dynamicConfig().nextIndex(), Slots.NEXT, event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                this.rotateNext(event, dynamicConfig().shiftAmount());
                return true;
            } else return false;
        });
    }

    private Socket previousSocket() {
        return new Socket(dynamicConfig().previousIndex(), Slots.PREVIOUS, event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                this.rotatePrevious(event, dynamicConfig().shiftAmount());
                return true;
            } else return false;
        });
    }

    public abstract DynamicConfig dynamicConfig();
    public abstract Socket createSocket(int index, T item);
}