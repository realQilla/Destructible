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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SingleToolTypeSelectionMenu extends DynamicMenu<ToolType> {

    private static final List<ToolType> TOOL_TYPES = Arrays.stream(ToolType.values())
            .collect(Collectors.toList());

    private final CompletableFuture<ToolType> future;

    public SingleToolTypeSelectionMenu(@NotNull Destructible plugin, @NotNull DPlayer dPlayer, @NotNull CompletableFuture<ToolType> future) {
        super(plugin, dPlayer, TOOL_TYPES);
        Preconditions.checkNotNull(future, "Future cannot be null");
        this.future = future;
        super.populateModular();
        super.finalizeMenu();
    }

    @Override
    public Socket createSocket(int index, ToolType item) {
        return new Socket(index, Slot.of(builder -> builder
                .material(item.getRepresentation())
                .displayName(MiniMessage.miniMessage().deserialize(StringUtil.toName(item.toString())))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left click to select")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                future.complete(item);
                return this.returnMenu();
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