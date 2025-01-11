package net.qilla.destructible.menus;

import net.kyori.adventure.text.Component;
import net.qilla.destructible.command.Sounds;
import net.qilla.destructible.menus.input.SignInput;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.Bukkit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class SearchMenu<T> extends ModularMenu<T> {
    private List<T> localPopulation;

    public SearchMenu(DPlayer dPlayer, MenuSize menuSize, Component title, List<Integer> modularSlots, List<T> itemPopulation) {
        super(dPlayer, menuSize, title, modularSlots, itemPopulation);
        this.localPopulation = new ArrayList<>(itemPopulation);

        super.register(this.getSearchSlot());
    }

    @Override
    protected void populateModular() {
        int fromIndex = Math.min(super.getShiftIndex(), this.localPopulation.size());
        int toIndex = Math.min(fromIndex + super.getModularSlots().size(), this.localPopulation.size());
        List<T> shiftedList = new ArrayList<>(this.localPopulation).subList(fromIndex, toIndex);

        Iterator<Integer> iterator = super.getModularSlots().iterator();
        shiftedList.forEach(item -> {
            if(iterator.hasNext()) {
                super.register(createSlot(iterator.next(), item));
            }
        });

        super.getSocket().getRemaining(super.getModularSlots()).forEach(slotNum -> super.register(Slot.of(slotNum, Displays.EMPTY_SLOT)));
        super.getSocket().getRemaining().forEach(slotNum -> super.register(Slot.of(slotNum, Displays.FILLER)));
    }

    protected void searchFor() {
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Block name to",
                "search for");

        SignInput signInput = new SignInput(super.getDPlayer(), signText);
        signInput.init(result -> {
            Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {
                try {
                    this.localPopulation = super.getItemPopulation().stream()
                            .filter(item -> matchSearchCriteria(item, result))
                            .toList();

                    super.register(this.getResetSearchSlot());
                    super.resetIndex();
                    updateModular();
                    getDPlayer().playSound(Sounds.SIGN_INPUT, true);
                } catch(NumberFormatException ignored) {
                }
                super.openInventory(false);
            });
        });
    }

    protected void resetSearch() {
        this.localPopulation = new ArrayList<>(super.getItemPopulation());
        super.unregister(this.getResetSearchSlot().getIndex());
        super.resetIndex();
        updateModular();
    }

    protected boolean matchSearchCriteria(T item, String search) {
        return getString(item).toLowerCase().contains(search.toLowerCase());
    }

    protected abstract String getString(T item);
    protected abstract Slot getSearchSlot();
    protected abstract Slot getResetSearchSlot();
}