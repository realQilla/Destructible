package net.qilla.destructible.menugeneral.menu.select;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.Sounds;
import net.qilla.destructible.menugeneral.DynamicMenu;
import net.qilla.destructible.menugeneral.slot.*;
import net.qilla.destructible.menugeneral.DynamicConfig;
import net.qilla.destructible.menugeneral.MenuSize;
import net.qilla.destructible.menugeneral.StaticConfig;
import net.qilla.destructible.mining.item.ToolType;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.stream.Collectors;

public class MultiToolTypeSelectionMenu extends DynamicMenu<ToolType> {

    private static final List<ToolType> TOOL_TYPES = Arrays.stream(ToolType.values())
            .collect(Collectors.toList());

    private final Set<ToolType> correctTools;

    public MultiToolTypeSelectionMenu(@NotNull Destructible plugin, @NotNull DPlayer dPlayer, @NotNull Set<ToolType> correctTools) {
        super(plugin, dPlayer, TOOL_TYPES);
        Preconditions.checkNotNull(correctTools, "Set cannot be null");
        this.correctTools = correctTools;
        super.populateModular();
        super.finalizeMenu();
    }

    @Override
    public Socket createSocket(int index, ToolType item) {
        boolean contains = correctTools.contains(item);

        return new Socket(index, Slot.of(builder -> builder
                .material(item.getRepresentation())
                .displayName(MiniMessage.miniMessage().deserialize(StringUtil.toName(item.toString())))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize(contains ? "<!italic><green><bold>SELECTED" : "<!italic><red><bold>NOT SELECTED"),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><key:key.mouse.left> to select this tool")
                )))
                .glow(contains)
                .clickSound(Sounds.MENU_CLICK_ITEM)
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
    public Socket menuSocket() {
        return new Socket(4, Slot.of(builder -> builder
                .material(Material.BLACK_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_gray>Tool Settings"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Set which tooltypes apply to"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>the current function")
                )))
        ));
    }

    @Override
    public StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuSize.THREE)
                .title(Component.text("Tool Settings"))
                .menuIndex(4)
                .returnIndex(22));
    }

    @Override
    public DynamicConfig dynamicConfig() {
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