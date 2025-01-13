package net.qilla.destructible.menus;

import net.qilla.destructible.command.Sounds;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.Socket;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class ModularMenu<T> extends DestructibleMenu implements ModularConfig {

    private final List<Integer> modularSlots;
    private final Collection<T> itemPopulation;
    private int shiftIndex;

    public ModularMenu(@NotNull DPlayer dPlayer, @NotNull Collection<T> itemPopulation) {
        super(dPlayer);
        this.itemPopulation = itemPopulation;
        this.modularSlots = modularIndexes();
        this.shiftIndex = 0;

        if(itemPopulation.size() > modularSlots.size()) super.register(nextSocket());
    }

    public void populateModular() {
        int fromIndex = Math.min(this.shiftIndex, this.itemPopulation.size());
        int toIndex = Math.min(fromIndex + modularSlots.size(), this.itemPopulation.size());
        List<T> shiftedList = new ArrayList<>(this.itemPopulation).subList(fromIndex, toIndex);

        Iterator<Integer> iterator = modularSlots.iterator();
        shiftedList.forEach(item -> {
            if(iterator.hasNext()) {
                super.register(createSocket(iterator.next(), item));
            }
        });

        super.getSocket().getRemaining(modularSlots).forEach(index -> super.register(Socket.of(index, Slot.of(Displays.EMPTY_SLOT))));
        super.getSocket().getRemaining().forEach(index -> super.register(Socket.of(index, Slot.of(Displays.FILLER))));
    }

    public void rotateNext(@NotNull Slot slot, InventoryClickEvent event, int amount) {
        ClickType clickType = event.getClick();
        if(clickType.isShiftClick() && clickType.isLeftClick()) this.shiftIndex += modularSlots.size();
        else if(clickType.isLeftClick()) this.shiftIndex += amount;

        //if(shiftIndex + modularSlots.size() > itemPopulation.size()) shiftIndex = (itemPopulation.size() - modularSlots.size() + (itemPopulation.size() % modularSlots.size()));

        this.updateModular();
        super.getDPlayer().playSound(Sounds.MENU_NEXT, true);
    }

    public void rotatePrevious(@NotNull Slot slot, InventoryClickEvent event, int amount) {
        ClickType clickType = event.getClick();
        if(clickType.isShiftClick() && clickType.isLeftClick()) this.shiftIndex -= modularSlots.size();
        else if(clickType.isLeftClick()) this.shiftIndex -= amount;

        if(shiftIndex < 0) shiftIndex = 0;

        this.updateModular();
        super.getDPlayer().playSound(Sounds.MENU_PREVIOUS, true);
    }

    public void updateModular() {
        modularSlots.forEach(super::unregister);

        if(shiftIndex > 0) super.register(previousSocket());
        else super.unregister(previousSocket().index());
        if(shiftIndex + modularSlots.size() < itemPopulation.size()) super.register(nextSocket());
        else super.unregister(nextSocket().index());

        populateModular();
    }

    @Override
    public void returnToPrevious() {
        if(super.getDPlayer().getCooldown().has(CooldownType.OPEN_MENU)) return;
        super.getDPlayer().getCooldown().set(CooldownType.OPEN_MENU);

        DestructibleMenu lastMenu = super.getDPlayer().getMenuData().getLastMenu();

        if(lastMenu == null) {
            this.closeMenu();
            return;
        }
        if(lastMenu instanceof ModularMenu<?> modularMenu) {
            modularMenu.updateModular();
        }
        lastMenu.openMenu(false);
    }

    public Collection<T> getItemPopulation() {
        return itemPopulation;
    }

    public int getShiftIndex() {
        return shiftIndex;
    }

    public void resetIndex() {
        this.shiftIndex = 0;
    }

    public abstract Socket nextSocket();
    public abstract Socket previousSocket();
    public abstract Socket createSocket(int index, T item);
}