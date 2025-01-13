package net.qilla.destructible.menus;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.command.Sounds;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.menus.blockmodify.BlockMenuModify;
import net.qilla.destructible.menus.slot.Display;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.FormatUtil;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class BlockMenu extends ModularMenu<DBlock> {

    public BlockMenu(@NotNull DPlayer dPlayer) {
        super(dPlayer, Registries.DESTRUCTIBLE_BLOCKS.values());

        super.register(Slot.of(46, builder -> builder
                .display(Display.of(consumer -> consumer
                        .material(Material.SHULKER_SHELL)
                        .displayName(MiniMessage.miniMessage().deserialize("<green>Create New"))
                        .lore(ItemLore.lore(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to create a new block")
                        )))
                ))
                .action((slot, event) -> {
                    ClickType clickType = event.getClick();
                    if(clickType.isLeftClick()) {
                        new BlockMenuModify(super.getDPlayer(), this).openMenu(true);
                    }
                })
                .clickSound(Sounds.CLICK_MENU_ITEM)
        ));

        super.populateModular();
    }

    public Slot createSocket(int index, DBlock dBlock) {
        return Slot.of(index, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(dBlock.getMaterial())
                        .displayName(Component.text(dBlock.getId()))
                        .lore(ItemLore.lore(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Block Strength <white>" + FormatUtil.romanNumeral(dBlock.getStrength())),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Block Durability <white>" + dBlock.getDurability()),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Block Cooldown <white>" + FormatUtil.getTime(dBlock.getCooldown(), true)),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Correct Tools:"),
                                MiniMessage.miniMessage().deserialize("<!italic><white>" + FormatUtil.toNameList(dBlock.getCorrectTools().stream().toList())),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Item Drops <white>" + dBlock.getLootpool().size()),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Break Sound <white>" + dBlock.getBreakSound()),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Break Particle <white>" + FormatUtil.toName(dBlock.getBreakParticle().toString())),
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to make modifications"),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Right Click to view possible drops")
                        )))
                ))
                .action((slot, event) -> blockClickInteraction(slot, event, dBlock))
                .clickSound(Sounds.CLICK_MENU_ITEM)
        );
    }

    private void blockClickInteraction(@NotNull Slot slot, @NotNull InventoryClickEvent event, @NotNull DBlock dBlock) {
        ClickType clickType = event.getClick();

        if(clickType.isLeftClick()) {
            new BlockMenuModify(getDPlayer(), this, dBlock).openMenu(true);
        } else if(clickType.isRightClick()) {
            new BlockLootpoolMenu(getDPlayer(), dBlock).openMenu(true);
        }
    }

    @Override
    public List<Integer> modularIndexes() {
        return List.of(
                9, 10, 11, 12, 13, 14, 15, 16, 17,
                18, 19, 20, 21, 22, 23, 24, 25, 26,
                27, 28, 29, 30, 31, 32, 33, 34, 35,
                36, 37, 38, 39, 40, 41, 42, 43, 44
        );
    }

    @Override
    public int returnIndex() {
        return 49;
    }

    @Override
    public int nextIndex() {
        return 52;
    }

    @Override
    public int previousIndex() {
        return 7;
    }

    @Override
    public int rotateAmount() {
        return 9;
    }

    @Override
    public Component tile() {
        return MiniMessage.miniMessage().deserialize("Destructible Blocks");
    }

    @Override
    public MenuSize menuSize() {
        return MenuSize.SIX;
    }

    @Override
    public Slot menuSlot() {
        return Slot.of(4, Displays.BLOCK_MENU);
    }
}