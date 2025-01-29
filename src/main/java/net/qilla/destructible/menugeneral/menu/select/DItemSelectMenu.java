package net.qilla.destructible.menugeneral.menu.select;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.menugeneral.DSlots;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.util.ComponentUtil;
import net.qilla.qlibrary.data.PlayerData;
import net.qilla.qlibrary.menu.*;
import net.qilla.qlibrary.menu.socket.QSlot;
import net.qilla.qlibrary.menu.socket.QSocket;
import net.qilla.qlibrary.menu.socket.Socket;
import net.qilla.qlibrary.player.CooldownType;
import net.qilla.qlibrary.util.sound.QSounds;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DItemSelectMenu extends QSearchMenu<DItem> {

    private static final Collection<DItem> DITEMS = DRegistry.ITEMS.values();
    private final CompletableFuture<DItem> future;

    public DItemSelectMenu(@NotNull Plugin plugin, @NotNull PlayerData playerData, @NotNull CompletableFuture<DItem> future) {
        super(plugin, playerData, DITEMS);
        Preconditions.checkNotNull(future, "Future cannot be null");
        this.future = future;
        super.populateModular();
        super.finalizeMenu();
    }

    @Override
    public @Nullable Socket createSocket(int index, DItem item) {
        return new QSocket(index, QSlot.of(builder -> builder
                .material(item.getMaterial())
                .displayName(item.getDisplayName())
                .lore(ItemLore.lore()
                        .addLines(List.of(
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Item ID <white>" + item.getId()),
                                Component.empty()
                        ))
                        .addLines(ComponentUtil.getLore(item).lines())
                        .addLines(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to select this item")
                        )).build()
                )
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            future.complete(item);
            return this.returnMenu();
        }, CooldownType.MENU_CLICK);
    }

    @Override
    public @NotNull String getString(DItem item) {
        return ComponentUtil.cleanComponent(item.getDisplayName());
    }

    @Override
    public @NotNull Socket menuSocket() {
        return new QSocket(4, DSlots.DITEM_SELECTION_MENU);
    }

    @Override
    public @NotNull StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuScale.SIX)
                .title(Component.text("Custom Item Search"))
                .menuIndex(4)
                .returnIndex(49));
    }

    @Override
    public @NotNull DynamicConfig dynamicConfig() {
        return DynamicConfig.of(builder -> builder
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

    @Override
    public @NotNull SearchConfig searchConfig() {
        return SearchConfig.of(builder -> builder
                .searchIndex(47)
                .resetSearchIndex(46)
        );
    }
}