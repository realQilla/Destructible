package net.qilla.destructible.menugeneral.menu.select;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.menugeneral.DSlots;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.qlibrary.data.PlayerData;
import net.qilla.qlibrary.menu.*;
import net.qilla.qlibrary.menu.socket.QSlot;
import net.qilla.qlibrary.menu.socket.QSocket;
import net.qilla.qlibrary.menu.socket.Socket;
import net.qilla.qlibrary.player.CooldownType;
import net.qilla.qlibrary.registry.RegistrySubscriber;
import net.qilla.qlibrary.util.sound.QSounds;
import net.qilla.qlibrary.util.tools.NumberUtil;
import net.qilla.qlibrary.util.tools.StringUtil;
import net.qilla.qlibrary.util.tools.TimeUtil;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DBlockSelectMenu extends QSearchMenu<DBlock> implements RegistrySubscriber {

    private final CompletableFuture<DBlock> future;

    public DBlockSelectMenu(@NotNull Plugin plugin, @NotNull PlayerData<?> playerData, @NotNull CompletableFuture<DBlock> future) {
        super(plugin, playerData, List.copyOf(DRegistry.BLOCKS.values()));
        Preconditions.checkNotNull(future, "Future cannot be null");
        this.future = future;
        super.populateModular();
        super.finalizeMenu();
        DRegistry.BLOCKS.subscribe(this);
    }

    @Override
    public void onUpdate() {
        super.updateItemPopulation(List.copyOf(DRegistry.BLOCKS.values()));
    }

    @Override
    public @Nullable Socket createSocket(int index, DBlock item) {
        String toolList = item.getCorrectTools().isEmpty() ? "<red>None" : StringUtil.toNameList(item.getCorrectTools().stream().toList(), ", ");


        return new QSocket(index, QSlot.of(builder -> builder
                .material(item.getMaterial())
                .displayName(Component.text(item.getID()))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Strength <white>" + NumberUtil.romanNumeral(item.getStrength())),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Durability <white>" + item.getDurability()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Cooldown <white>" + TimeUtil.getTime(item.getCooldown(), true)),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Correct Tools:"),
                        MiniMessage.miniMessage().deserialize("<!italic><white>" + toolList),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Item Drops <white>" + item.getLootpool().size()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Break Sound <white>" + item.getBreakSound()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Break Particle <white>" + StringUtil.toName(item.getBreakParticle().toString())),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to select this custom block")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            future.complete(item);
            return this.returnMenu();
        }, CooldownType.MENU_CLICK);
    }

    @Override
    public @NotNull String getString(DBlock item) {
        return item.getID();
    }

    @Override
    public @NotNull Socket menuSocket() {
        return new QSocket(4, DSlots.DBLOCK_SELECTION_MENU);
    }

    @Override
    public @NotNull StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuScale.SIX)
                .title(Component.text("Custom Block Search"))
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

    @Override
    public void shutdown() {
        this.clearSockets();
        super.getInventory().close();
        DRegistry.BLOCKS.unsubscribe(this);
    }
}