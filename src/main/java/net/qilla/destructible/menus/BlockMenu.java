package net.qilla.destructible.menus;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.menus.slot.Display;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.menus.slot.UniqueSlot;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.FormatUtil;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.*;

public class BlockMenu extends ModularMenu<DBlock> {

    private static final MenuSize SIZE = MenuSize.SIX;
    private static final Component TITLE = MiniMessage.miniMessage().deserialize("Destructible Blocks");
    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    );

    public BlockMenu(DPlayer dPlayer) {
        super(dPlayer, SIZE, TITLE, MODULAR_SLOTS, Registries.DESTRUCTIBLE_BLOCKS.values().stream()
                .sorted(Comparator.comparing(DBlock::getId))
                .toList());

        super.register(Slot.of(4, Displays.BLOCK_MENU));
        super.register(Slot.of(46, builder -> builder
                .display(createMenu)
                .action((slot, clickType) -> new BlockMenuModify(super.getDPlayer()).openInventory(true))));
        super.register(Slot.of(49, builder -> builder
                .display(Displays.RETURN)
                .action((slot, clickType) -> returnToPreviousMenu())
                .uniqueSlot(UniqueSlot.RETURN)));

        populateModular();
    }

    private final Display createMenu = Display.of(consumer -> consumer
            .material(Material.SHULKER_SHELL)
            .displayName(MiniMessage.miniMessage().deserialize("<dark_green>Create New"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Click to create a new destructible block"))
            ))
    );

    public Slot createSlot(int index, DBlock dBlock) {
        Display display = Display.of(builder -> builder
                .material(dBlock.getMaterial())
                .displayName(Component.text(dBlock.getId()))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Strength <white>" + FormatUtil.romanNumeral(dBlock.getStrength())),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Durability <white>" + dBlock.getDurability()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Cooldown <white>" + FormatUtil.getTime(dBlock.getCooldown(), true)),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Correct Tools:"),
                        MiniMessage.miniMessage().deserialize("<!italic><white>" + FormatUtil.toNameList(dBlock.getCorrectTools().stream().toList())),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Item Drops <yellow>" + "Right Click to view"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Break Sound <white>" + dBlock.getBreakSound()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Break Particles <white>" + dBlock.getBreakParticle()),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Click to modify block")
                )))
        );
        return Slot.of(index, builder -> builder
                .display(display)
                .action((slot, event) -> blockClickInteraction(slot, event, dBlock)));
    }

    private void blockClickInteraction(Slot slot, InventoryClickEvent event, DBlock dBlock) {
        ClickType clickType = event.getClick();

        if(clickType.isLeftClick()) {
            new BlockMenuModify(getDPlayer(), dBlock).openInventory(true);
        } else if(clickType.isRightClick()) {
            new BlockMenuDrops(getDPlayer(), slot, Registries.DESTRUCTIBLE_BLOCKS.get(dBlock.getId())).openInventory(true);
        }
    }

    @Override
    protected Slot getNextSlot() {
        return Slot.of(52, builder -> builder
                .display(Displays.NEXT)
                .action((slot, event) -> super.rotateNext(slot, event, 9)));
    }

    @Override
    protected Slot getPreviousSlot() {
        return Slot.of(7, builder -> builder
                .display(Displays.PREVIOUS)
                .action((slot, clickType) -> super.rotatePrevious(slot, clickType, 9)));
    }
}