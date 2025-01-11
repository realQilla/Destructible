package net.qilla.destructible.menus.blockmodify;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.command.Sounds;
import net.qilla.destructible.menus.MenuSize;
import net.qilla.destructible.menus.SearchMenu;
import net.qilla.destructible.menus.slot.Display;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.UniqueSlot;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.FormatUtil;
import org.bukkit.Material;
import org.bukkit.Registry;
import java.util.List;

public class SetParticle extends SearchMenu<Material> {

    private static final MenuSize SIZE = MenuSize.SIX;
    private static final Component TITLE = Component.text("Block Search");
    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    );

    private final BlockMenuModify menu;

    public SetParticle(DPlayer dPlayer, BlockMenuModify menu) {
        super(dPlayer, SIZE, TITLE, MODULAR_SLOTS,
                Registry.MATERIAL.stream()
                        .filter(Material::isBlock)
                        .toList());

        this.menu = menu;
        this.populateMenu();
        super.populateModular();
    }

    @Override
    protected void populateMenu() {
        super.register(Slot.of(4, Display.of(builder -> builder
                .material(Material.HEART_OF_THE_SEA)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_purple>Particle Search"))
        )));
        super.register(Slot.of(49, builder -> builder
                .display(Displays.RETURN)
                .action((slot, event) -> menu.returnToPreviousMenu())
                .uniqueSlot(UniqueSlot.RETURN)
                .clickSound(Sounds.RETURN_MENU)
        ));
        super.register(Slot.of(47, builder -> builder
                .display(Displays.SEARCH)
                .action((slot, event) -> super.searchFor())
                .clickSound(Sounds.CLICK_MENU_ITEM)
        ));
    }

    @Override
    public Slot createSlot(int index, Material item) {
        return Slot.of(index, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(item)
                        .displayName(MiniMessage.miniMessage().deserialize(FormatUtil.toName(item.toString())))
                        .lore(ItemLore.lore(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to select particle")
                        )))
                ))
                .action((slot, event) -> {
                    this.menu.setBreakParticle(item);
                    this.menu.particleSlot();
                    super.returnToPreviousMenu();
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

    @Override
    public String getString(Material item) {
        return item.toString();
    }

    @Override
    protected Slot getSearchSlot() {
        return Slot.of(47, builder -> builder
                .display(Displays.SEARCH)
                .action((slot, event) -> super.searchFor())
        );
    }

    @Override
    protected Slot getResetSearchSlot() {
        return Slot.of(45, builder -> builder
                .display(Displays.RESET_SEARCH)
                .uniqueSlot(UniqueSlot.RESET_SEARCH)
                .action((slot, event) -> super.resetSearch())
                .appearSound(Sounds.ITEM_APPEAR)
                .clickSound(Sounds.RESET)
        );
    }
}