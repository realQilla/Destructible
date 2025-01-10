package net.qilla.destructible.menus;

import net.kyori.adventure.text.Component;
import net.qilla.destructible.menus.input.SignInput;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.UniqueSlot;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.player.PlayType;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class SearchMenu<T> extends ModularMenu<T> {

    private List<T> localItemPopulation;
    private final Slot searchSlot;
    private final Slot resetSearchSlot;

    public SearchMenu(DPlayer dPlayer, MenuSize menuSize, Component title, List<Integer> modularSlots, List<T> itemPopulation) {
        super(dPlayer, menuSize, title, modularSlots, itemPopulation);
        this.localItemPopulation = itemPopulation;
        this.searchSlot = getSearchSlot();
        this.resetSearchSlot = getResetSearchSlot();

        super.register(this.searchSlot);
    }

    @Override
    protected void populateModular() {
        int fromIndex = Math.min(super.getShiftIndex(), localItemPopulation.size());
        int toIndex = Math.min(fromIndex + getModularSlots().size(), localItemPopulation.size());
        List<T> shiftedList = new ArrayList<>(localItemPopulation).subList(fromIndex, toIndex);

        Iterator<Integer> iterator = getModularSlots().iterator();
        shiftedList.forEach(item -> {
            if(iterator.hasNext()) {
                register(createSlot(iterator.next(), item));
            }
        });

        super.getSocket().getRemaining(getModularSlots()).forEach(slotNum -> super.register(Slot.of(slotNum, Displays.EMPTY_SLOT)));
        super.getSocket().getRemaining().forEach(slotNum -> super.register(Slot.of(slotNum, Displays.FILLER)));
    }

    public void searchFor() {
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Block name to",
                "search for");

        SignInput signInput = new SignInput(super.getDPlayer(), signText);
        signInput.init(result -> {
            Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {
                try {
                    this.localItemPopulation = getItemPopulation().stream()
                            .filter(item -> matchSearchCriteria(item, result))
                            .toList();

                    super.register(this.resetSearchSlot);
                    super.resetIndex();
                    refreshModular();
                    getDPlayer().playSound(SoundSettings.of(Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER), true);

                } catch(NumberFormatException ignored) {
                }
                super.openInventory(false);
            });
        });
    }

    public void resetItemPopulation() {
        this.localItemPopulation = getItemPopulation();
        super.unregister(UniqueSlot.RESET_SEARCH);
        refreshModular();
    }

    public boolean matchSearchCriteria(T item, String search) {
        return getString(item).toLowerCase().contains(search.toLowerCase());
    }

    protected abstract String getString(T item);
    protected abstract Slot getSearchSlot();
    protected abstract Slot getResetSearchSlot();
}