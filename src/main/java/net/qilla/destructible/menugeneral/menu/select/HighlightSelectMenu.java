package net.qilla.destructible.menugeneral.menu.select;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.registry.DPlayerDataRegistry;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.menugeneral.DSlots;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.player.BlockHighlight;
import net.qilla.destructible.player.DPlayer;
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
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HighlightSelectMenu extends QSearchMenu<String> implements RegistrySubscriber {

    private static final Map<String, DBlock> DBLOCKS = DRegistry.BLOCKS;
    private final Set<String> highlights;

    public HighlightSelectMenu(@NotNull Plugin plugin, @NotNull PlayerData<?> playerData, @NotNull Set<String> highlights) {
        super(plugin, playerData, List.copyOf(DRegistry.LOADED_BLOCKS_GROUPED.keySet()));
        Preconditions.checkNotNull(highlights, "Set cannot be null");
        this.highlights = highlights;
        super.populateModular();
        super.finalizeMenu();
        DRegistry.LOADED_BLOCKS_GROUPED.subscribe(this);
    }

    @Override
    public void onUpdate() {
        super.updateItemPopulation(List.copyOf(DRegistry.LOADED_BLOCKS_GROUPED.keySet()));
    }

    @Override
    public @Nullable Socket createSocket(int index, String item) {
        DBlock dBlock = DBLOCKS.get(item);
        if(dBlock == null) return null;
        String toolList = dBlock.getCorrectTools().isEmpty() ? "<red>None" : StringUtil.toNameList(dBlock.getCorrectTools().stream().toList(), ", ");
        String visible = highlights.contains(item) ? "<green><bold>VISIBLE" : "<red><bold>NOT VISIBLE";

        return new QSocket(index, QSlot.of(builder -> builder
                .material(dBlock.getMaterial())
                .displayName(Component.text(dBlock.getID()))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Currently " + visible),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Strength <white>" + NumberUtil.romanNumeral(dBlock.getStrength())),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Durability <white>" + dBlock.getDurability()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Cooldown <white>" + TimeUtil.getTime(dBlock.getCooldown(), true)),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Correct Tools:"),
                        MiniMessage.miniMessage().deserialize("<!italic><white>" + toolList),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Item Drops <white>" + dBlock.getLootpool().size()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Break Sound <white>" + dBlock.getBreakSound()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Break Particle <white>" + StringUtil.toName(dBlock.getBreakParticle().toString())),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to toggle visibility")
                )))
                .glow(highlights.contains(item))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
        ), event -> {

            BlockHighlight blockHighlight = DPlayerDataRegistry.getInstance().getData(super.getPlayer()).getBlockEdit().getBlockHighlight();
            DRegistry.BLOCK_EDITORS.put(super.getPlayer().getUniqueId(), (DPlayer) super.getPlayer());
            if(highlights.contains(item)) {
                highlights.remove(item);
                blockHighlight.removeVisibleDBlock(item);
                blockHighlight.removeHighlights(item);
            } else {
                highlights.add(item);
                blockHighlight.addVisibleDBlock(item);
                blockHighlight.createHighlights(item);
            }
            super.refreshSockets();
            return true;
        }, CooldownType.MENU_CLICK);
    }

    @Override
    public @NotNull String getString(String item) {
        return item;
    }

    @Override
    public @NotNull Socket menuSocket() {
        return new QSocket(4, DSlots.HIGHLIGHT_SELECTION_MENU);
    }

    @Override
    public @NotNull StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuScale.SIX)
                .title(Component.text("Block Highlight Search"))
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
        DRegistry.LOADED_BLOCKS_GROUPED.unsubscribe(this);
    }
}