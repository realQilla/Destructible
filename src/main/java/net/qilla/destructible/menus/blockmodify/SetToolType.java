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
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class SetToolType extends ModularMenu<ToolType> {

    private final Set<ToolType> toolTypes;

    public SetToolType(DPlayer dPlayer, BlockMenuModify menu) {
        super(dPlayer, Arrays.stream(ToolType.values()).toList());

        this.toolTypes = menu.getToolTypes();
        super.populateModular();
    }

    @Override
    public Slot createSocket(int index, ToolType toolType) {
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
                    super.updateModular();
                })
                .clickSound(Sounds.CLICK_MENU_ITEM)
        );
    }

    @Override
    public List<Integer> modularIndexes() {
        return List.of(
                9, 10, 11, 12, 13, 14, 15, 16, 17,
                18, 19, 20, 21, 22, 23, 24, 25, 26
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
        return Component.text("Tool Types");
    }

    @Override
    public MenuSize menuSize() {
        return MenuSize.SIX;
    }

    @Override
    public Slot menuSlot() {
        return Slot.of(4, Display.of(builder -> builder
                .material(Material.BLACK_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_gray>Tool Types"))
        ));
    }
}