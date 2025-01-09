package net.qilla.destructible.menus;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.menus.input.SignInput;
import net.qilla.destructible.menus.slot.Display;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.UniqueSlot;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.player.PlayType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import java.util.List;

public abstract class SearchMenu<T> extends ModularMenu<T> {

    private List<T> localItemPopulation;

    public SearchMenu(DPlayer dPlayer, MenuSize menuSize, Component title, List<Integer> modularSlots, List<T> itemPopulation) {
        super(dPlayer, menuSize, title, modularSlots, itemPopulation);
        this.localItemPopulation = itemPopulation;

        populateModular();
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

                    super.register(Slot.of(47, builder -> builder
                            .display(Display.of(builder2 -> builder2
                                    .material(Material.BARRIER)
                                    .displayName(MiniMessage.miniMessage().deserialize("<red>Reset Search"))
                                    .lore(ItemLore.lore(List.of(MiniMessage.miniMessage().deserialize("<!italic><gray>Resets your currently searched term")
                                    )))
                            ))
                            .uniqueSlot(UniqueSlot.RESET_SEARCH)
                            .action((slot, clickType) -> resetItemPopulation())
                            .appearSound(SoundSettings.of(Sound.ENTITY_CHICKEN_EGG, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))
                            .clickSound(SoundSettings.of(Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER))
                    ));
                    super.resetIndex();
                    refresh();

                } catch(NumberFormatException ignored) {
                }
                super.openInventory(false);
            });
        });
    }

    public void resetItemPopulation() {
        this.localItemPopulation = getItemPopulation();
        super.unregister(UniqueSlot.RESET_SEARCH);
        refresh();
    }

    public boolean matchSearchCriteria(T item, String search) {
        return getString(item).toLowerCase().contains(search.toLowerCase());
    }

    protected abstract String getString(T item);
}