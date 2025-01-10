package net.qilla.destructible.menus;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.menus.slot.Display;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.menus.slot.UniqueSlot;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.ItemDrop;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.FormatUtil;

import java.util.*;

public class BlockMenuDrops extends ModularMenu<ItemDrop> {
    private static final MenuSize SIZE = MenuSize.SIX;
    private static final Component TITLE = MiniMessage.miniMessage().deserialize("Item Drops");
    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 19, 28, 37, 38, 39, 30, 21, 12, 13, 14, 23, 32, 41, 42, 43, 34, 25, 16, 17
    );

    private final DBlock dBlock;

    public BlockMenuDrops(DPlayer dPlayer, DBlock dBlock) {
        super(dPlayer, SIZE, TITLE, MODULAR_SLOTS,
                dBlock.getLootpool().stream()
                        .sorted((Comparator.comparingDouble(ItemDrop::getChance).reversed()))
                        .toList());
        Preconditions.checkNotNull(dBlock, "DBlock cannot be null.");
        this.dBlock = dBlock;

        this.populateMenu();
        super.populateModular();
    }

    @Override
    protected void populateMenu() {
        super.register(Slot.of(49, builder -> builder
                .display(Displays.RETURN)
                .action((slot, clickType) -> returnToPreviousMenu())
                .uniqueSlot(UniqueSlot.RETURN)));
        super.register(Slot.of(4, Display.of(consumer -> consumer
                .material(dBlock.getMaterial())
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Strength <white>" + FormatUtil.romanNumeral(dBlock.getStrength())),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Durability <white>" + dBlock.getDurability()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Cooldown <white>" + FormatUtil.getTime(dBlock.getCooldown(), true)),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Correct Tools:"),
                        MiniMessage.miniMessage().deserialize("<!italic><white>" + FormatUtil.toNameList(dBlock.getCorrectTools().stream().toList())),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Item Drops <yellow>" + "[Currently viewing]"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Break Sound <white>" + dBlock.getBreakSound()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Break Particles <white>" + dBlock.getBreakParticle())
                )))
                .glow(true))));
    }

    @Override
    protected Slot createSlot(int index, ItemDrop itemDrop) {
        DItem dItem = itemDrop.getDItem();

        Display display = Display.of(builder -> builder
                .material(dItem.getMaterial())
                .amount(itemDrop.getMinAmount())
                .displayName(dItem.getDisplayName())
                .lore(ItemLore.lore().addLines(dItem.getLore().lines())
                        .addLines(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Drop Amount <white>" +
                                        itemDrop.getMinAmount() + " - " + itemDrop.getMaxAmount()),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Drop Chance <white>" +
                                        FormatUtil.decimalTruncation(itemDrop.getChance() * 100, 17) + "% (1/" + FormatUtil.numberComma((long) Math.ceil(1 / itemDrop.getChance())) + ")")
                        )).build()
                )
        );
        return Slot.of(index, display);
    }

    @Override
    protected Slot getNextSlot() {
        return Slot.of(52, builder -> builder
                .display(Displays.NEXT)
                .action((slot, event) -> super.rotateNext(slot, event, 1)));
    }

    @Override
    protected Slot getPreviousSlot() {
        return Slot.of(46, builder -> builder
                .display(Displays.PREVIOUS)
                .action((slot, clickType) -> super.rotatePrevious(slot, clickType, 1)));
    }
}