package net.qilla.destructible.menugeneral.menu.select;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.DRegistry;
import net.qilla.destructible.data.Sounds;
import net.qilla.destructible.menugeneral.*;
import net.qilla.destructible.menugeneral.slot.*;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.FormatUtil;
import org.bukkit.Material;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DBlockSelectMenu extends SearchMenu<DBlock> {

    private final CompletableFuture<DBlock> future;

    public DBlockSelectMenu(DPlayer dPlayer, CompletableFuture<DBlock> future) {
        super(dPlayer, DRegistry.DESTRUCTIBLE_BLOCKS.values().stream().toList());
        this.future = future;
        super.populateModular();
        super.finalizeMenu();
    }

    @Override
    public Socket createSocket(int index, DBlock item) {
        String toolList = item.getCorrectTools().isEmpty() ? "<red>None" : FormatUtil.toNameList(item.getCorrectTools().stream().toList());


        return new Socket(index, Slot.of(builder -> builder
                .material(item.getMaterial())
                .displayName(Component.text(item.getId()))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Strength <white>" + FormatUtil.romanNumeral(item.getStrength())),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Durability <white>" + item.getDurability()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Cooldown <white>" + FormatUtil.getTime(item.getCooldown(), true)),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Correct Tools:"),
                        MiniMessage.miniMessage().deserialize("<!italic><white>" + toolList),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Item Drops <white>" + item.getLootpool().size()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Break Sound <white>" + item.getBreakSound()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Break Particle <white>" + FormatUtil.toName(item.getBreakParticle().toString())),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to select custom block")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            future.complete(item);
            return this.returnMenu();
        });
    }

    @Override
    public String getString(DBlock item) {
        return item.getId();
    }

    @Override
    public Socket menuSocket() {
        return new Socket(4, Slot.of(builder -> builder
                .material(Material.DIAMOND_BLOCK)
                .displayName(MiniMessage.miniMessage().deserialize("<aqua>Search"))
        ));
    }

    @Override
    public StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuSize.SIX)
                .title(Component.text("Custom Block Search"))
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