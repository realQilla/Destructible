package net.qilla.destructible.menus;

import net.qilla.destructible.command.Sounds;
import net.qilla.destructible.menus.input.SignInput;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.Bukkit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class SearchMenu<T> extends ModularMenu<T> implements SearchConfig {
    private List<T> localPopulation;

    public SearchMenu(DPlayer dPlayer, Collection<T> itemPopulation) {
        super(dPlayer, itemPopulation);
        this.localPopulation = new ArrayList<>(itemPopulation);

        super.register(searchSlot());
    }

    @Override
    public void populateModular() {
        int fromIndex = Math.min(super.getShiftIndex(), this.localPopulation.size());
        int toIndex = Math.min(fromIndex + modularIndexes().size(), this.localPopulation.size());
        List<T> shiftedList = new ArrayList<>(this.localPopulation).subList(fromIndex, toIndex);

        Iterator<Integer> iterator = modularIndexes().iterator();
        shiftedList.forEach(item -> {
            if(iterator.hasNext()) {
                super.register(createSocket(iterator.next(), item));
            }
        });

        super.getSocket().getRemaining(modularIndexes()).forEach(slotNum -> super.register(Slot.of(slotNum, Displays.EMPTY_SLOT)));
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
                    this.localPopulation = getItemPopulation().stream()
                            .filter(item -> matchSearchCriteria(item, result))
                            .toList();

                    super.register(resetSearchSlot());
                    getDPlayer().playSound(Sounds.SIGN_INPUT, true);
                } catch(NumberFormatException ignored) {
                }
                super.openMenu(false);
            });
        });
    }

    protected void resetSearch() {
        this.localPopulation = new ArrayList<>(super.getItemPopulation());
        super.unregister(resetSearchIndex());
        super.resetIndex();
        updateModular();
    }

    protected boolean matchSearchCriteria(T item, String search) {
        return getString(item).toLowerCase().contains(search.toLowerCase());
    }

    protected Slot searchSlot() {
        return Slot.of(searchIndex(), builder -> builder
                .display(Displays.SEARCH)
                .action((slot, event) -> searchFor())
        );
    }

    protected Slot resetSearchSlot() {
        return Slot.of(resetSearchIndex(), builder -> builder
                .display(Displays.RESET_SEARCH)
                .action((slot, event) -> resetSearch())
        );
    }

    protected abstract String getString(T item);
}