package net.qilla.destructible.gui;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.FormatUtil;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class BlockMenu extends DestructibleMenu {
    private static final GUISize SIZE = GUISize.SIX;
    private static final Component TITLE = MiniMessage.miniMessage().deserialize("Blocks");

    private final List<Integer> blockSlots = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35
    );

    private final Slot menuItem = Slot.builder(slot -> slot
            .index(4)
            .material(Material.CHEST)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Block Modification"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Select any block to view more"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>details or make changes"))
            ))
    ).build();

    private final Slot backItem = Slot.builder(slot -> slot
            .index(53)
            .material(Material.BARRIER)
            .displayName(MiniMessage.miniMessage().deserialize("<red>Return"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Click to return to your"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>previously opened menu"))))
            .clickAction(action -> super.returnToPreviousMenu())
    ).build();

    public BlockMenu(DPlayer dPlayer) {
        super(dPlayer, SIZE, TITLE);
        this.setSlot(this.menuItem);
        this.setSlot(this.backItem);
        initBlocks();
    }

    private void initBlocks() {
        Iterator<Integer> iterator = blockSlots.iterator();
        Registries.DESTRUCTIBLE_BLOCKS.forEach((id, block) -> {
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
        });
    }

    @Override
    public void onInteract(InventoryInteractEvent event) {
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
    }
}