package net.qilla.destructible.menugeneral.menu.select;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.SoundSettings;
import net.qilla.destructible.data.Sounds;
import net.qilla.destructible.menugeneral.*;
import net.qilla.destructible.menugeneral.slot.*;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.player.PlayType;
import net.qilla.qlibrary.util.tools.StringUtil;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SoundSelectMenu extends SearchMenu<Sound> {

    private static final List<Sound> SOUND_SET = Registry.SOUNDS.stream()
            .collect(Collectors.toList());
    private final CompletableFuture<Sound> future;

    public SoundSelectMenu(@NotNull Destructible plugin, @NotNull DPlayer dPlayer, @NotNull CompletableFuture<Sound> future) {
        super(plugin, dPlayer, SOUND_SET);
        Preconditions.checkNotNull(future, "Future cannot be null");
        this.future = future;
        super.populateModular();
        super.finalizeMenu();
    }

    @Override
    public Socket createSocket(int index, Sound item) {
        return new Socket(index, Slot.of(builder -> builder
                .material(Material.MUSIC_DISC_RELIC)
                .displayName(MiniMessage.miniMessage().deserialize(item.toString()))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><key:key.mouse.left> to select this sound"),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><key:key.mouse.right> to listen")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                future.complete(item);
                return this.returnMenu();
            } else if(clickType.isRightClick()) {
                getDPlayer().getCraftPlayer().stopAllSounds();
                getDPlayer().playSound(SoundSettings.of(item, 0.5f, 1f, SoundCategory.PLAYERS, PlayType.PLAYER), true);
            }
            return false;
        }, CooldownType.MENU_CLICK);
    }

    @Override
    public String getString(Sound item) {
        return StringUtil.toName(item.toString());
    }

    @Override
    public Socket menuSocket() {
        return new Socket(4, Slot.of(builder -> builder
                .material(Material.NAUTILUS_SHELL)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_aqua>Search"))
        ));
    }

    @Override
    public StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuSize.SIX)
                .title(Component.text("Sound Search"))
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