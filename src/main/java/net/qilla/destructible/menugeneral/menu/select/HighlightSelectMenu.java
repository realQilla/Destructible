package net.qilla.destructible.menugeneral.menu.select;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.DRegistry;
import net.qilla.destructible.data.Sounds;
import net.qilla.destructible.menugeneral.*;
import net.qilla.destructible.menugeneral.slot.*;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.player.BlockHighlight;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.NumberUtil;
import net.qilla.destructible.util.StringUtil;
import net.qilla.destructible.util.TimeUtil;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class HighlightSelectMenu extends SearchMenu<String> {

    private final Set<String> highlights;

    public HighlightSelectMenu(@NotNull Destructible plugin, @NotNull DPlayer dPlayer, @NotNull Set<String> highlights) {
        super(plugin, dPlayer, DRegistry.LOADED_DESTRUCTIBLE_BLOCKS_GROUPED.keySet().stream().toList());
        Preconditions.checkNotNull(highlights, "Set cannot be null");
        this.highlights = highlights;
        super.populateModular();
        super.finalizeMenu();
    }

    @Override
    public Socket createSocket(int index, String item) {
        DBlock dBlock = DRegistry.DESTRUCTIBLE_BLOCKS.get(item);
        String toolList = dBlock.getCorrectTools().isEmpty() ? "<red>None" : StringUtil.toNameList(dBlock.getCorrectTools().stream().toList());
        String visible = highlights.contains(item) ? "<green><bold>VISIBLE" : "<red><bold>NOT VISIBLE";

        return new Socket(index, Slot.of(builder -> builder
                .material(dBlock.getMaterial())
                .displayName(Component.text(dBlock.getId()))
                .lore(ItemLore.lore(List.of(
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
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Currently " + visible)
                )))
                .glow(highlights.contains(item))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            BlockHighlight blockHighlight = getDPlayer().getDBlockEdit().getBlockHighlight();
            DRegistry.DESTRUCTIBLE_BLOCK_EDITORS.add(getDPlayer());
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
        });
    }

    @Override
    public String getString(String item) {
        return item;
    }

    @Override
    public Socket menuSocket() {
        return new Socket(4, Slot.of(builder -> builder
                .material(Material.BLUE_ICE)
                .displayName(MiniMessage.miniMessage().deserialize("<aqua>Search"))
        ));
    }

    @Override
    public StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuSize.SIX)
                .title(Component.text("Block Highlight Search"))
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