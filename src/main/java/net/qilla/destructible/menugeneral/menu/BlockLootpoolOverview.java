package net.qilla.destructible.menugeneral.menu;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.menugeneral.DSlots;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.ItemDrop;
import net.qilla.qlibrary.data.PlayerData;
import net.qilla.qlibrary.menu.DynamicConfig;
import net.qilla.qlibrary.menu.MenuScale;
import net.qilla.qlibrary.menu.QDynamicMenu;
import net.qilla.qlibrary.menu.StaticConfig;
import net.qilla.qlibrary.menu.socket.QSlot;
import net.qilla.qlibrary.menu.socket.QSocket;
import net.qilla.qlibrary.menu.socket.Socket;
import net.qilla.qlibrary.player.EnhancedPlayer;
import net.qilla.qlibrary.util.tools.NumberUtil;
import net.qilla.qlibrary.util.tools.StringUtil;
import net.qilla.qlibrary.util.tools.TimeUtil;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class BlockLootpoolOverview extends QDynamicMenu<ItemDrop> {

    public BlockLootpoolOverview(@NotNull Plugin plugin, @NotNull PlayerData<?> playerData, @NotNull DBlock dBlock) {
        super(plugin, playerData, dBlock.getLootpool().stream()
                .sorted((Comparator.comparingDouble(ItemDrop::getChance).reversed()))
                .toList());

        Preconditions.checkNotNull(dBlock, "DBlock cannot be null");
        super.addSocket(new QSocket(31, QSlot.of(consumer -> consumer
                .material(dBlock.getMaterial())
                .displayName(Component.text(dBlock.getId()))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Strength <white>" + NumberUtil.romanNumeral(dBlock.getStrength())),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Durability <white>" + dBlock.getDurability()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Cooldown <white>" + TimeUtil.getTime(dBlock.getCooldown(), true)),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Correct Tools:"),
                        MiniMessage.miniMessage().deserialize("<!italic><white>" + StringUtil.toNameList(dBlock.getCorrectTools().stream().toList(), ", ")),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Item Drops <yellow>" + "[Currently viewing]"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Break Sound <white>" + dBlock.getBreakSound()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Break Particles <white>" + dBlock.getBreakParticle())
                )))
                .glow(true)
        )));
        super.populateModular();
        super.finalizeMenu();
    }

    @Override
    public @NotNull Socket createSocket(int index, ItemDrop item) {
        DItem dItem = item.getDItem();
        return new QSocket(index, QSlot.of(builder -> builder
                .material(dItem.getMaterial())
                .amount(item.getMinAmount())
                .displayName(dItem.getDisplayName())
                .lore(ItemLore.lore().addLines(dItem.getLore().lines())
                        .addLines(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Drop Amount <white>" +
                                        item.getMinAmount() + " - " + item.getMaxAmount()),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Fortune Affected <white>" + StringUtil.toName(String.valueOf(item.isFortuneAffected()))),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Drop Chance <white>" +
                                        NumberUtil.decimalTruncation(item.getChance() * 100, 17) + "% (1/" + NumberUtil.numberComma((long) Math.ceil(1 / item.getChance())) + ")")
                        )).build()
                )
        ));
    }

    @Override
    public @NotNull Socket menuSocket() {
        return new QSocket(4, DSlots.LOOTPOOL_OVERVIEW_MENU);
    }

    @Override
    public @NotNull StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuScale.SIX)
                .title(Component.text("Lootpool Overview"))
                .menuIndex(4)
                .returnIndex(49));
    }

    @Override
    public @NotNull DynamicConfig dynamicConfig() {
        return DynamicConfig.of(
                builder -> builder
                        .dynamicSlots(List.of(9, 10, 19, 28, 37, 38, 39, 30, 21, 12, 13, 14, 23, 32, 41, 42, 43, 34, 25, 16, 17))
                        .nextIndex(52)
                        .previousIndex(46)
                        .shiftAmount(1)
        );
    }
}