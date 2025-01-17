package net.qilla.destructible.menugeneral.menu;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Sounds;
import net.qilla.destructible.menugeneral.DynamicMenu;
import net.qilla.destructible.menugeneral.slot.*;
import net.qilla.destructible.menugeneral.DynamicConfig;
import net.qilla.destructible.menugeneral.MenuSize;
import net.qilla.destructible.menugeneral.StaticConfig;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.ItemDrop;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.FormatUtil;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class LootpoolSetMenu extends DynamicMenu<ItemDrop> {

    private final List<ItemDrop> lootpool;

    public LootpoolSetMenu(@NotNull DPlayer dPlayer, @NotNull List<ItemDrop> lootpool) {
        super(dPlayer, lootpool);
        this.lootpool = lootpool;

        super.addSocket(new Socket(47, Slot.of(builder -> builder
                .material(Material.LIME_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<green>Add new item drop"))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            new ItemDropCreationMenu(getDPlayer(), lootpool, null).open(true);
            return true;
        }), 0);
        super.populateModular();
        super.finalizeMenu();
    }

    @Override
    public Socket createSocket(int index, ItemDrop item) {
        DItem dItem = item.getDItem();
        return new Socket(index, Slot.of(builder -> builder
                .material(dItem.getMaterial())
                .amount(item.getMinAmount())
                .displayName(Component.text(dItem.getId()))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Amount <white>" + item.getMinAmount() + " - " + item.getMaxAmount()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Drop Chance <white>" +
                                FormatUtil.decimalTruncation(item.getChance() * 100, 17) + "% (1/" + FormatUtil.numberComma((long) Math.ceil(1 / item.getChance())) + ")"),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Middle Click to modify"),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Shift-Right Click to remove"))
                ))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType == ClickType.MIDDLE) {
                new ItemDropCreationMenu(getDPlayer(), lootpool, item).open(true);
                return true;
            } else if(clickType.isShiftClick() && clickType.isLeftClick()) {
                lootpool.remove(item);
                super.refreshSockets();
                return true;
            } else return false;
        });
    }

    @Override
    public Socket menuSocket() {
        return new Socket(4, Slot.of(builder -> builder
                .material(Material.PINK_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<light_purple>Lootpool"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Make lootpool modifications to"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>the selected destructible block")
                )))
        ));
    }

    @Override
    public StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuSize.SIX)
                .title(Component.text("Lootpool Settings"))
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