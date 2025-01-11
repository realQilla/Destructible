package net.qilla.destructible.menus.blockmodify;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.command.Sounds;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.menus.MenuSize;
import net.qilla.destructible.menus.ModularMenu;
import net.qilla.destructible.menus.slot.Display;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.UniqueSlot;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.Material;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SetDItem extends ModularMenu<DItem> {

    private static final MenuSize SIZE = MenuSize.SIX;
    private static final Component TITLE = Component.text("Lootpool");

    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35
    );

    private final ModifyItemDrop menu;

    public SetDItem(DPlayer dPlayer, ModifyItemDrop menu) {
        super(dPlayer, SIZE, TITLE, MODULAR_SLOTS,
                Registries.DESTRUCTIBLE_ITEMS.values().stream().toList());

        this.menu = menu;

        this.populateMenu();
        super.populateModular();
    }

    @Override
    protected void populateMenu() {
        super.register(Slot.of(4, Display.of(builder -> builder
                .material(Material.PINK_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<light_purple>Lootpool"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Make lootpool modifications to"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>the selected destructible block")
                )))
        )));
        super.register(Slot.of(49, builder -> builder
                .display(Displays.RETURN)
                .action((slot, event) -> returnToPreviousMenu())
                .uniqueSlot(UniqueSlot.RETURN)
                .clickSound(Sounds.RETURN_MENU)
        ));
    }

    public Slot createSlot(int index, DItem item) {
        return Slot.of(index, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(item.getMaterial())
                        .displayName(MiniMessage.miniMessage().deserialize(item.getId()))
                        .lore(ItemLore.lore()
                                .addLines(List.of(
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Name ").append(item.getDisplayName()),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Lore:")
                                ))
                                .addLines(item.getLore().lines())
                                .addLines(List.of(
                                        Component.empty(),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Rarity ").append(item.getRarity().getComponent())
                                )).build()
                        )
                ))
                .action((slot, event) -> {
                    this.menu.setDItem(item);
                    this.menu.openInventory(false);
                })
        );
    }

    @Override
    protected Slot getNextSlot() {
        return Slot.of(52, builder -> builder
                .display(Displays.NEXT)
                .action((slot, event) -> rotateNext(slot, event, 9)));
    }

    @Override
    protected Slot getPreviousSlot() {
        return Slot.of(7, builder -> builder
                .display(Displays.PREVIOUS)
                .action((slot, event) -> rotatePrevious(slot, event, 9)));
    }
}