package net.qilla.destructible.menugeneral.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.Sounds;
import net.qilla.destructible.menugeneral.MenuSize;
import net.qilla.destructible.menugeneral.StaticConfig;
import net.qilla.destructible.menugeneral.StaticMenu;
import net.qilla.destructible.menugeneral.slot.Slot;
import net.qilla.destructible.menugeneral.slot.Slots;
import net.qilla.destructible.menugeneral.slot.Socket;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

public class BlockMenu extends StaticMenu {

    public BlockMenu(@NotNull Destructible plugin, @NotNull DPlayer dPlayer) {
        super(plugin, dPlayer);
        super.addSocket(new Socket(21, Slot.of(builder -> builder
                .material(Material.COMMAND_BLOCK_MINECART)
                .displayName(MiniMessage.miniMessage().deserialize("<gold>World Loading"))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                new WorldLoadingMenu(super.getPlugin(), dPlayer).open(true);
                return true;
            } else return false;
        }), 0);

        super.addSocket(new Socket(23, Slot.of(builder -> builder
                .material(Material.CHEST_MINECART)
                .displayName(MiniMessage.miniMessage().deserialize("<yellow>Custom Block Overview"))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                new BlockOverviewMenu(super.getPlugin(), super.getDPlayer()).open(true);
                return true;
            } else return false;
        }), 0);
    }

    @Override
    public void refreshSockets() {
    }

    @Override
    public Socket menuSocket() {
        return new Socket(4, Slots.BLOCK_MENU);
    }

    @Override
    public StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuSize.FIVE)
                .title(Component.text("Block Settings"))
                .menuIndex(4)
                .returnIndex(40));
    }
}
