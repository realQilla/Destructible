package net.qilla.destructible.menugeneral.menu.select;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.data.registry.DRegistryMaster;
import net.qilla.destructible.data.Sounds;
import net.qilla.destructible.menugeneral.*;
import net.qilla.destructible.menugeneral.slot.*;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.ComponentUtil;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DItemSelectMenu extends SearchMenu<DItem> {

    private static final Collection<DItem> DITEMS = DRegistry.ITEMS.values();
    private final CompletableFuture<DItem> future;

    public DItemSelectMenu(@NotNull Destructible plugin, @NotNull DPlayer dPlayer, @NotNull CompletableFuture<DItem> future) {
        super(plugin, dPlayer, DITEMS);
        Preconditions.checkNotNull(future, "Future cannot be null");
        this.future = future;
        super.populateModular();
        super.finalizeMenu();
    }

    @Override
    public Socket createSocket(int index, DItem item) {
        return new Socket(index, Slot.of(builder -> builder
                .material(item.getMaterial())
                .displayName(MiniMessage.miniMessage().deserialize(item.getId()))
                .lore(ItemLore.lore()
                        .addLines(List.of(
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Name ").append(item.getDisplayName()),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Lore:")
                        ))
                        .addLines(item.getLore().lines())
                        .addLines(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Rarity ").append(item.getRarity().getComponent()),
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left click to select custom item")
                        )).build()
                )
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            future.complete(item);
            return this.returnMenu();
        }, CooldownType.MENU_CLICK);
    }

    @Override
    public String getString(DItem item) {
        return ComponentUtil.cleanComponent(item.getDisplayName());
    }

    @Override
    public Socket menuSocket() {
        return new Socket(4, Slot.of(builder -> builder
                .material(Material.DIAMOND_PICKAXE)
                .displayName(MiniMessage.miniMessage().deserialize("<aqua>Search"))
        ));
    }

    @Override
    public StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuSize.SIX)
                .title(Component.text("Custom Item Search"))
                .menuIndex(4)
                .returnIndex(49));
    }

    @Override
    public DynamicConfig dynamicConfig() {
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
    public SearchConfig searchConfig() {
        return SearchConfig.of(builder -> builder
                .searchIndex(47)
                .resetSearchIndex(46)
        );
    }
}