package net.qilla.destructible.menus.blockmodify;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.command.Sounds;
import net.qilla.destructible.menus.MenuSize;
import net.qilla.destructible.menus.ModularMenu;
import net.qilla.destructible.menus.slot.Display;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.UniqueSlot;
import net.qilla.destructible.mining.item.ToolType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.FormatUtil;
import org.bukkit.Material;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class SetToolType extends ModularMenu<ToolType> {

    private static final MenuSize SIZE = MenuSize.FOUR;
    private static final Component TITLE = Component.text("Tool Types");
    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26
    );

    private final BlockMenuModify menu;
    private final Set<ToolType> toolTypes;

    public SetToolType(DPlayer dPlayer, BlockMenuModify menu) {
        super(dPlayer, SIZE, TITLE, MODULAR_SLOTS,
                Arrays.stream(ToolType.values())
                        .toList());

        this.menu = menu;
        this.toolTypes = menu.getToolTypes();
        this.populateMenu();
        super.populateModular();
    }

    @Override
    protected void populateMenu() {
        super.register(Slot.of(4, Display.of(builder -> builder
                .material(Material.BLACK_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_gray>Tool Types"))
        )));
        super.register(Slot.of(31, builder -> builder
                .display(Displays.RETURN)
                .action((slot, event) -> {
                    menu.register(menu.correctToolsSlot());
                    super.returnToPreviousMenu();
                })
                .uniqueSlot(UniqueSlot.RETURN)
                .clickSound(Sounds.RETURN_MENU)
        ));

        populateModular();
    }

    @Override
    protected Slot createSlot(int index, ToolType toolType) {
        boolean contains = toolTypes.contains(toolType);

        return Slot.of(index, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(contains ? Material.OAK_HANGING_SIGN : Material.OAK_SIGN)
                        .displayName(MiniMessage.miniMessage().deserialize(FormatUtil.toName(toolType.toString())))
                        .lore(ItemLore.lore(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize(contains ? "<!italic><green><bold>SELECTED" : "<!italic><red><bold>NOT SELECTED")
                        )))
                ))
                .action((slot, event) -> {
                    if(toolTypes.contains(toolType)) {
                        toolTypes.remove(toolType);
                    } else {
                        toolTypes.add(toolType);
                    }
                    updateModular();
                })
                .clickSound(Sounds.CLICK_MENU_ITEM)
        );
    }

    @Override
    protected Slot getNextSlot() {
        return Slot.of(34, builder -> builder
                .display(Displays.NEXT)
                .action((slot, event) -> rotateNext(slot, event, 9)));
    }

    @Override
    protected Slot getPreviousSlot() {
        return Slot.of(27, builder -> builder
                .display(Displays.PREVIOUS)
                .action((slot, event) -> rotatePrevious(slot, event, 9)));
    }
}