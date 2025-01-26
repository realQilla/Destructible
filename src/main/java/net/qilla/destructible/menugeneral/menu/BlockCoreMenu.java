package net.qilla.destructible.menugeneral.menu;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.menugeneral.DSlots;
import net.qilla.qlibrary.data.PlayerData;
import net.qilla.qlibrary.menu.MenuScale;
import net.qilla.qlibrary.menu.QStaticMenu;
import net.qilla.qlibrary.menu.StaticConfig;
import net.qilla.qlibrary.menu.socket.QSlot;
import net.qilla.qlibrary.menu.socket.QSocket;
import net.qilla.qlibrary.menu.socket.Socket;
import net.qilla.qlibrary.player.CooldownType;
import net.qilla.qlibrary.util.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BlockCoreMenu extends QStaticMenu {

    public BlockCoreMenu(@NotNull Destructible plugin, @NotNull PlayerData playerData) {
        super(plugin, playerData);
        super.addSocket(new QSocket(21, QSlot.of(builder -> builder
                .material(Material.COMMAND_BLOCK_MINECART)
                .displayName(MiniMessage.miniMessage().deserialize("<red>World Loading"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to view options on block loading")
                )))
                .clickSound(MenuSound.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                new WorldLoadingMenu(super.getPlugin(), playerData).open(true);
                return true;
            } else return false;
        }, CooldownType.OPEN_MENU), 0);

        super.addSocket(new QSocket(23, QSlot.of(builder -> builder
                .material(Material.CHEST_MINECART)
                .displayName(MiniMessage.miniMessage().deserialize("<blue>Block Overview"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to view or customize all blocks")
                )))
                .clickSound(MenuSound.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                new BlockOverviewMenu(super.getPlugin(), super.getPlayerData()).open(true);
                return true;
            } else return false;
        }, CooldownType.OPEN_MENU), 0);
    }

    @Override
    public void refreshSockets() {
    }

    @Override
    public @NotNull Socket menuSocket() {
        return new QSocket(4, DSlots.BLOCK_MENU);
    }

    @Override
    public @NotNull StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuScale.FIVE)
                .title(Component.text("Block Settings"))
                .menuIndex(4)
                .returnIndex(40));
    }
}
