package net.qilla.destructible.menugeneral.menu;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.menugeneral.DSlots;
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
import net.qilla.qlibrary.player.CooldownType;
import net.qilla.qlibrary.util.sound.QSounds;
import net.qilla.qlibrary.util.sound.QSounds.Menu;
import net.qilla.qlibrary.util.tools.NumberUtil;
import net.qilla.qlibrary.util.tools.StringUtil;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LootpoolModificationMenu extends QDynamicMenu<ItemDrop> {

    private final List<ItemDrop> lootpool;

    public LootpoolModificationMenu(@NotNull Plugin plugin, @NotNull PlayerData playerData, @NotNull List<ItemDrop> lootpool) {
        super(plugin, playerData, lootpool);
        Preconditions.checkNotNull(lootpool, "List cannot be null");
        this.lootpool = lootpool;
        super.addSocket(new QSocket(47, DSlots.MODIFICATION_CREATE, event -> {
            new ItemDropCreationMenu(super.getPlugin(), super.getPlayerData(), lootpool).open(true);
            return true;
        }, CooldownType.OPEN_MENU), 0);
        super.populateModular();
        super.finalizeMenu();
    }

    @Override
    public @Nullable Socket createSocket(int index, ItemDrop item) {
        DItem dItem = item.getDItem();
        return new QSocket(index, QSlot.of(builder -> builder
                .material(dItem.getMaterial())
                .amount(item.getMinAmount())
                .displayName(Component.text(dItem.getId()))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Fortune Affected <white>" + StringUtil.toName(String.valueOf(item.isFortuneAffected()))),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Amount <white>" + item.getMinAmount() + " - " + item.getMaxAmount()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Drop Chance <white>" +
                                NumberUtil.decimalTruncation(item.getChance() * 100, 17) + "% (1/" + NumberUtil.numberComma((long) Math.ceil(1 / item.getChance())) + ")"),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to make modifications"),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>② <key:key.mouse.right></gold> to remove this item drop"))
                ))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                new ItemDropCreationMenu(super.getPlugin(), super.getPlayerData(), lootpool, item).open(true);
                return true;
            } else if(clickType.isRightClick()) {
                lootpool.remove(item);
                super.refreshSockets();
                return true;
            } else return false;
        }, CooldownType.OPEN_MENU);
    }

    @Override
    public @NotNull Socket menuSocket() {
        return new QSocket(4, DSlots.LOOTPOOL_MODIFICATION_MENU);
    }

    @Override
    public @NotNull StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuScale.SIX)
                .title(Component.text("Lootpool Settings"))
                .menuIndex(4)
                .returnIndex(49));
    }

    @Override
    public @NotNull DynamicConfig dynamicConfig() {
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