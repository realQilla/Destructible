package net.qilla.destructible.menus;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.menus.slot.Display;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.ItemDrop;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.FormatUtil;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class BlockLootpoolMenu extends ModularMenu<ItemDrop> {

    private final DBlock dBlock;

    public BlockLootpoolMenu(@NotNull DPlayer dPlayer, @NotNull DBlock dBlock) {
        super(dPlayer, dBlock.getLootpool().stream()
                        .sorted((Comparator.comparingDouble(ItemDrop::getChance).reversed()))
                        .toList());
        this.dBlock = dBlock;
        super.register(Slot.of(31, Display.of(consumer -> consumer
                .material(dBlock.getMaterial())
                .displayName(Component.text(dBlock.getId()))
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
        )));

        super.populateModular();
    }

    @Override
    public Slot createSocket(int index, ItemDrop itemDrop) {
        DItem dItem = itemDrop.getDItem();
        return Slot.of(index, Display.of(builder -> builder
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
        ));
    }

    @Override
    public List<Integer> modularIndexes() {
        return List.of(9, 10, 19, 28, 37, 38, 39, 30, 21, 12, 13, 14, 23, 32, 41, 42, 43, 34, 25, 16, 17
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
        return MiniMessage.miniMessage().deserialize("Lootpool");
    }

    @Override
    public MenuSize menuSize() {
        return MenuSize.SIX;
    }

    @Override
    public Slot menuSlot() {
        return Slot.of(4, Display.of(builder -> builder
                .material(Material.PINK_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<light_purple>Lootpool"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>All information regarding block"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>lootpool's can be found below,"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>ordered by each item's chance ")
                )))
        ));
    }
}