package net.qilla.destructible.menugeneral.menu.select;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Sounds;
import net.qilla.destructible.menugeneral.*;
import net.qilla.destructible.menugeneral.slot.*;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.FormatUtil;
import org.bukkit.Material;
import org.bukkit.Registry;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ItemSelectMenu extends SearchMenu<Material> {

    private final CompletableFuture<Material> future;

    public ItemSelectMenu(DPlayer dPlayer, CompletableFuture<Material> future) {
        super(dPlayer, Registry.MATERIAL.stream()
                .filter(Material::isItem)
                .toList());
        this.future = future;
        super.populateModular();
        super.finalizeMenu();
    }

    @Override
    public Socket createSocket(int index, Material item) {
        return new Socket(index, Slot.of(builder -> builder
                .material(item)
                .displayName(MiniMessage.miniMessage().deserialize(FormatUtil.toName(item.toString())))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to select material")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            this.future.complete(item);
            return this.returnMenu();
        });
    }

    @Override
    public String getString(Material item) {
        return item.toString();
    }

    @Override
    public Socket menuSocket() {
        return new Socket(4, Slot.of(builder -> builder
                .material(Material.IRON_INGOT)
                .displayName(MiniMessage.miniMessage().deserialize("<blue>Item Search"))
        ));
    }

    @Override
    public StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuSize.SIX)
                .title(Component.text("Item Search"))
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