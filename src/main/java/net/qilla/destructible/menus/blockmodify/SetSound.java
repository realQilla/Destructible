package net.qilla.destructible.menus.blockmodify;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.command.Sounds;
import net.qilla.destructible.menus.MenuSize;
import net.qilla.destructible.menus.SearchMenu;
import net.qilla.destructible.menus.SoundSettings;
import net.qilla.destructible.menus.slot.Display;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.UniqueSlot;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.player.PlayType;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.inventory.ClickType;

import java.util.Collection;
import java.util.List;

public class SetSound extends SearchMenu<Sound> {

    private final BlockMenuModify menu;

    public SetSound(DPlayer dPlayer, BlockMenuModify menu) {
        super(dPlayer, Registry.SOUNDS.stream()
                .toList());

        this.menu = menu;
        super.populateModular();
    }

    @Override
    public Slot createSocket(int index, Sound item) {
        return Slot.of(index, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(Material.MUSIC_DISC_RELIC)
                        .displayName(MiniMessage.miniMessage().deserialize(item.toString()))
                        .lore(ItemLore.lore(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to select sound"),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Right Click to listen")
                        )))
                ))
                .action((slot, event) -> {
                    ClickType clickType = event.getClick();
                    if(clickType.isLeftClick()) {
                        this.menu.setBreakSound(item);
                        super.returnToPrevious();
                    } else if(clickType.isRightClick()) {
                        getDPlayer().getCraftPlayer().stopAllSounds();
                        getDPlayer().playSound(SoundSettings.of(item, 0.5f, 1f, SoundCategory.PLAYERS, PlayType.PLAYER), true);
                    }
                })
        );
    }

    @Override
    public String getString(Sound item) {
        return item.toString();
    }

    @Override
    public List<Integer> modularIndexes() {
        return List.of(
                9, 10, 11, 12, 13, 14, 15, 16, 17,
                18, 19, 20, 21, 22, 23, 24, 25, 26,
                27, 28, 29, 30, 31, 32, 33, 34, 35,
                36, 37, 38, 39, 40, 41, 42, 43, 44
        );
    }

    @Override
    public int returnIndex() {
        return 49;
    }

    @Override
    public int nextIndex() {
        return 52;
    }

    @Override
    public int previousIndex() {
        return 7;
    }

    @Override
    public int rotateAmount() {
        return 9;
    }

    @Override
    public Component tile() {
        return Component.text("Sound Search");
    }

    @Override
    public MenuSize menuSize() {
        return MenuSize.SIX;
    }

    @Override
    public Slot menuSlot() {
        return Slot.of(4, Display.of(builder -> builder
                .material(Material.NAUTILUS_SHELL)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_aqua>Sound Search"))
        ));
    }

    @Override
    public int searchIndex() {
        return 47;
    }

    @Override
    public int resetSearchIndex() {
        return 45;
    }
}