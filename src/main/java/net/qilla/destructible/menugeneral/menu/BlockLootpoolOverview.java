package net.qilla.destructible.menugeneral.menu;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.menugeneral.DynamicMenu;
import net.qilla.destructible.menugeneral.slot.Slot;
import net.qilla.destructible.menugeneral.slot.Socket;
import net.qilla.destructible.menugeneral.DynamicConfig;
import net.qilla.destructible.menugeneral.MenuSize;
import net.qilla.destructible.menugeneral.StaticConfig;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.ItemDrop;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.NumberUtil;
import net.qilla.destructible.util.StringUtil;
import net.qilla.destructible.util.TimeUtil;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class BlockLootpoolOverview extends DynamicMenu<ItemDrop> {

    public BlockLootpoolOverview(@NotNull Destructible plugin, @NotNull DPlayer dPlayer, @NotNull DBlock dBlock) {
        super(plugin, dPlayer, dBlock.getLootpool().stream()
                .sorted((Comparator.comparingDouble(ItemDrop::getChance).reversed()))
                .toList());
        Preconditions.checkNotNull(dBlock, "DBlock cannot be null");
        super.addSocket(new Socket(31, Slot.of(consumer -> consumer
                .material(dBlock.getMaterial())
                .displayName(Component.text(dBlock.getId()))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Strength <white>" + NumberUtil.romanNumeral(dBlock.getStrength())),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Durability <white>" + dBlock.getDurability()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Cooldown <white>" + TimeUtil.getTime(dBlock.getCooldown(), true)),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Correct Tools:"),
                        MiniMessage.miniMessage().deserialize("<!italic><white>" + StringUtil.toNameList(dBlock.getCorrectTools().stream().toList())),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Item Drops <yellow>" + "[Currently viewing]"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Break Sound <white>" + dBlock.getBreakSound()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Break Particles <white>" + dBlock.getBreakParticle())
                )))
                .glow(true)
        )), 0);
        super.populateModular();
        super.finalizeMenu();
    }

    @Override
    public Socket createSocket(int index, ItemDrop item) {
        DItem dItem = item.getDItem();
        return new Socket(index, Slot.of(builder -> builder
                .material(dItem.getMaterial())
                .amount(item.getMinAmount())
                .displayName(dItem.getDisplayName())
                .lore(ItemLore.lore().addLines(dItem.getLore().lines())
                        .addLines(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Drop Amount <white>" +
                                        item.getMinAmount() + " - " + item.getMaxAmount()),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Drop Chance <white>" +
                                        NumberUtil.decimalTruncation(item.getChance() * 100, 17) + "% (1/" + NumberUtil.numberComma((long) Math.ceil(1 / item.getChance())) + ")")
                        )).build()
                )
        ));
    }

    @Override
    public Socket menuSocket() {
        return new Socket(4, Slot.of(builder -> builder
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

    @Override
    public StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuSize.SIX)
                .title(Component.text("Lootpool Overview"))
                .menuIndex(4)
                .returnIndex(49));
    }

    @Override
    public DynamicConfig dynamicConfig() {
        return DynamicConfig.of(
                builder -> builder
                        .dynamicSlots(List.of(9, 10, 19, 28, 37, 38, 39, 30, 21, 12, 13, 14, 23, 32, 41, 42, 43, 34, 25, 16, 17))
                        .nextIndex(52)
                        .previousIndex(46)
                        .shiftAmount(1)
        );
    }
}