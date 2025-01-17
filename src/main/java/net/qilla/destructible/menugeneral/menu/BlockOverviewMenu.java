package net.qilla.destructible.menugeneral.menu;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Sounds;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.menugeneral.DynamicMenu;
import net.qilla.destructible.menugeneral.slot.Slot;
import net.qilla.destructible.menugeneral.slot.Slots;
import net.qilla.destructible.menugeneral.slot.Socket;
import net.qilla.destructible.menugeneral.DynamicConfig;
import net.qilla.destructible.menugeneral.MenuSize;
import net.qilla.destructible.menugeneral.StaticConfig;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.FormatUtil;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class BlockOverviewMenu extends DynamicMenu<DBlock> {

    public BlockOverviewMenu(@NotNull DPlayer dPlayer) {
        super(dPlayer, Registries.DESTRUCTIBLE_BLOCKS.values());
        super.addSocket(new Socket(46, Slots.CREATE_NEW, event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                new BlockModificationMenu(super.getDPlayer(), null).open(true);
                return true;
            } else return false;
        }), 0);
        super.populateModular();
        super.finalizeMenu();
    }

    @Override
    public Socket createSocket(int index, DBlock item) {
        return new Socket(index, Slot.of(builder -> builder
                .material(item.getMaterial())
                .displayName(Component.text(item.getId()))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Strength <white>" + FormatUtil.romanNumeral(item.getStrength())),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Durability <white>" + item.getDurability()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Cooldown <white>" + FormatUtil.getTime(item.getCooldown(), true)),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Correct Tools:"),
                        MiniMessage.miniMessage().deserialize("<!italic><white>" + FormatUtil.toNameList(item.getCorrectTools().stream().toList())),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Item Drops <white>" + item.getLootpool().size()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Break Sound <white>" + item.getBreakSound()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Break Particle <white>" + FormatUtil.toName(item.getBreakParticle().toString())),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to view possible drops"),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Middle Click to make modifications")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> blockClickInteraction(event, item));
    }

    private boolean blockClickInteraction(@NotNull InventoryClickEvent event, @NotNull DBlock dBlock) {
        ClickType clickType = event.getClick();

        if(clickType.isLeftClick()) {
            new BlockLootpoolOverview(getDPlayer(), dBlock).open(true);
            return true;
        } else if(clickType == ClickType.MIDDLE) {
            new BlockModificationMenu(getDPlayer(), dBlock).open(true);
            return true;
        } else return false;
    }

    @Override
    public Socket menuSocket() {
        return new Socket(4, Slots.BLOCK_MENU);
    }

    @Override
    public StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuSize.SIX)
                .title(Component.text("Block Overview"))
                .menuIndex(4)
                .returnIndex(49));
    }

    @Override
    public DynamicConfig dynamicConfig() {
        return DynamicConfig.of(
                builder -> builder
                        .dynamicSlots(List.of(
                                9, 10, 11, 12, 13, 14, 15, 16, 17,
                                18, 19, 20, 21, 22, 23, 24, 25, 26,
                                27, 28, 29, 30, 31, 32, 33, 34, 35,
                                36, 37, 38, 39, 40, 41, 42, 43, 44
                        ))
                        .nextIndex(52)
                        .previousIndex(7)
                        .shiftAmount(9)
        );
    }
}