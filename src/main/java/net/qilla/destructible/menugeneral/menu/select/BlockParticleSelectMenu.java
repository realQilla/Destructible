package net.qilla.destructible.menugeneral.menu.select;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.Sounds;
import net.qilla.destructible.menugeneral.*;
import net.qilla.destructible.menugeneral.slot.*;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.qlibrary.util.tools.StringUtil;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class BlockParticleSelectMenu extends SearchMenu<Material> {

    private static final List<Material> BLOCK_SET = Registry.MATERIAL.stream()
            .filter(Material::isBlock)
            .collect(Collectors.toList());

    private final CompletableFuture<Material> future;

    public BlockParticleSelectMenu(@NotNull Destructible plugin, @NotNull DPlayer dPlayer, @NotNull CompletableFuture<Material> future) {
        super(plugin, dPlayer, BLOCK_SET);
        Preconditions.checkNotNull(future, "Future cannot be null");
        this.future = future;
        super.populateModular();
        super.finalizeMenu();
    }

    @Override
    public Socket createSocket(int index, Material item) {
        return new Socket(index, Slot.of(builder -> builder
                .material(item)
                .displayName(MiniMessage.miniMessage().deserialize(StringUtil.toName(item.toString())))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><key:key.mouse.left> to select particle")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            future.complete(item);
            return this.returnMenu();
        }, CooldownType.MENU_CLICK);
    }

    @Override
    public String getString(Material item) {
        return StringUtil.toName(item.toString());
    }

    @Override
    public Socket menuSocket() {
        return new Socket(4, Slot.of((builder -> builder
                .material(Material.HEART_OF_THE_SEA)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_purple>Search"))
        )));
    }

    @Override
    public StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuSize.SIX)
                .title(Component.text("Particle Search"))
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