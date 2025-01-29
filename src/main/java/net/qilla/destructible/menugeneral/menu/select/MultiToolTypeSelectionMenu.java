package net.qilla.destructible.menugeneral.menu.select;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.menugeneral.DSlots;
import net.qilla.destructible.mining.item.ToolType;
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
import net.qilla.qlibrary.util.tools.StringUtil;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class MultiToolTypeSelectionMenu extends QDynamicMenu<ToolType> {

    private static final List<ToolType> TOOL_TYPES = Arrays.stream(ToolType.values())
            .collect(Collectors.toList());

    private final Set<ToolType> correctTools;

    public MultiToolTypeSelectionMenu(@NotNull Plugin plugin, @NotNull PlayerData playerData, @NotNull Set<ToolType> correctTools) {
        super(plugin, playerData, TOOL_TYPES);
        Preconditions.checkNotNull(correctTools, "Set cannot be null");
        this.correctTools = correctTools;
        super.populateModular();
        super.finalizeMenu();
    }

    @Override
    public @Nullable Socket createSocket(int index, ToolType item) {
        boolean contains = correctTools.contains(item);

        return new QSocket(index, QSlot.of(builder -> builder
                .material(item.getRepresentation())
                .displayName(MiniMessage.miniMessage().deserialize(StringUtil.toName(item.toString())))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize(contains ? "<!italic><green><bold>SELECTED" : "<!italic><red><bold>NOT SELECTED"),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to toggle this tool")
                )))
                .glow(contains)
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                if(correctTools.contains(item)) correctTools.remove(item);
                else correctTools.add(item);
                super.refreshSockets();
                return true;
            } else return false;
        }, CooldownType.MENU_CLICK);
    }

    @Override
    public @NotNull Socket menuSocket() {
        return new QSocket(4, DSlots.MULTI_TOOL_SELECTION_MENU);
    }

    @Override
    public @NotNull StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuScale.THREE)
                .title(Component.text("Tool Selection"))
                .menuIndex(4)
                .returnIndex(22));
    }

    @Override
    public @NotNull DynamicConfig dynamicConfig() {
        return DynamicConfig.of(builder -> builder
                .dynamicSlots(List.of(
                        10, 11, 12, 13, 14, 15, 16
                ))
                .nextIndex(25)
                .previousIndex(19)
                .shiftAmount(1)
        );
    }
}