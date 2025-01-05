package net.qilla.destructible.gui;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.player.PlayType;
import net.qilla.destructible.util.FormatUtil;
import net.qilla.destructible.util.RandomUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BlockMenu extends DestructibleMenu {
    private static final GUISize SIZE = GUISize.SIX;
    private static final Component TITLE = MiniMessage.miniMessage().deserialize("Blocks");

    private final List<Integer> blockSlots = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    );
    private final List<DBlock> blockList;
    private int shiftIndex = 0;

    private final Slot returnItem = Slots.BACK_ITEM
            .index(49)
            .clickAction(action -> super.returnToPreviousMenu())
            .build();

    private final Slot shiftUpItem = Slots.UP_ITEM
            .index(7)
            .clickAction(action -> {
                getDPlayer().playSound(Sound.ENTITY_BREEZE_LAND, SoundCategory.PLAYERS, 0.75f, RandomUtil.between(1.5f, 2.0f), PlayType.PLAYER);
                this.shift(-1);
            })
            .build();

    private final Slot shiftDownItem = Slots.DOWN_ITEM
            .index(52)
            .clickAction(action -> {
                getDPlayer().playSound(Sound.ENTITY_BREEZE_JUMP, SoundCategory.PLAYERS, 0.25f, RandomUtil.between(0.75f, 1.25f), PlayType.PLAYER);
                this.shift(1);
            })
            .build();

    private final Slot menuItem = Slot.builder(slot -> slot
            .index(4)
            .material(Material.CHEST)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Block Modification"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Select any block to view more"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>details or make changes"))
            ))
    ).build();

    public BlockMenu(DPlayer dPlayer) {
        super(dPlayer, SIZE, TITLE);
        this.blockList = Registries.DESTRUCTIBLE_BLOCKS.values().stream().toList();
        this.setSlot(this.menuItem);
        this.setSlot(this.returnItem);
        initBlocks();
    }

    private void initBlocks() {
        if(shiftIndex > 0) this.setSlot(this.shiftUpItem);
        else this.unsetSlot(this.shiftUpItem.getIndex());
        if(shiftIndex + blockSlots.size() < this.blockList.size()) this.setSlot(this.shiftDownItem);
        else this.unsetSlot(this.shiftDownItem.getIndex());

        List<DBlock> shiftedList = new LinkedList<>(blockList).subList(shiftIndex, blockList.size());

        Iterator<Integer> iterator = blockSlots.iterator();
        shiftedList.iterator().forEachRemaining(block -> {
            if(iterator.hasNext()) {
                Slot slot = Slot.builder(builder -> builder
                        .index(iterator.next())
                        .material(block.getBlockMaterial())
                        .displayName(MiniMessage.miniMessage().deserialize(block.getId()))
                        .lore(ItemLore.lore(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Block Strength <white>" + FormatUtil.romanNumeral(block.getBlockStrength())),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Block Durability <white>" + block.getBlockDurability()),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Block Cooldown <white>" + FormatUtil.getTime(block.getBlockCooldown(), true)),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Correct Tools:"),
                                MiniMessage.miniMessage().deserialize("<!italic><white>" + FormatUtil.getList(block.getCorrectTools())),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Item Drops <yellow>" + "Click to view"),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Break Sound <white>" + block.getBreakSound()),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Break Particles <white>" + block.getBreakParticle())
                        )))
                        .clickAction(action -> {
                        })
                ).build();
                setSlot(slot);
            }
            super.getSlotHolder().getRemainingSlots(blockSlots).forEach(slotNum -> super.setSlot(Slots.EMPTY_ITEM.index(slotNum).build()));
            super.getSlotHolder().getRemainingSlots().forEach(slotNum -> super.setSlot(Slots.FILLER_ITEM.index(slotNum).build()));
        });
    }

    private void shift(int amount) {
        shiftIndex += amount;
        super.unsetSlots(blockSlots);
        this.initBlocks();
    }

    @Override
    public void onInteract(InventoryInteractEvent event) {
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
    }
}