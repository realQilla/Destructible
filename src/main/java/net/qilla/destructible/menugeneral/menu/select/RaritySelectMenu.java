package net.qilla.destructible.menugeneral.menu.select;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Sounds;
import net.qilla.destructible.menugeneral.*;
import net.qilla.destructible.menugeneral.slot.*;
import net.qilla.destructible.mining.item.Rarity;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RaritySelectMenu extends DynamicMenu<Rarity> {

    private final CompletableFuture<Rarity> future;

    public RaritySelectMenu(DPlayer dPlayer, CompletableFuture<Rarity> future) {
        super(dPlayer, Arrays.stream(Rarity.values()).toList());
        this.future = future;
        super.populateModular();
        super.finalizeMenu();
    }

    @Override
    public Socket createSocket(int index, Rarity item) {
        return new Socket(index, Slot.of(builder -> builder
                .material(item.getMaterial())
                .displayName(item.getComponent())
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to select rarity")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            future.complete(item);
            return this.returnMenu();
        });
    }

    @Override
    public Socket menuSocket() {
        return new Socket(4, Slot.of(builder -> builder
                .material(Material.LAPIS_LAZULI)
                .displayName(MiniMessage.miniMessage().deserialize("<light_purple>Rarity Settings"))
        ));
    }

    @Override
    public StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuSize.THREE)
                .title(Component.text("Rarity Settings"))
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