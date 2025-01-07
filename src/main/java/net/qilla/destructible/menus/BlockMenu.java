package net.qilla.destructible.menus;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.FormatUtil;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BlockMenu extends DestructibleMenu {

    private static final GUISize SIZE = GUISize.SIX;
    private static final Component TITLE = MiniMessage.miniMessage().deserialize("Destructible Blocks");
    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    );

    private int shiftIndex = 0;
    private final List<DBlock> itemPopulation = Registries.DESTRUCTIBLE_BLOCKS.values().stream().toList();

    public BlockMenu(DPlayer dPlayer) {
        super(dPlayer, SIZE, TITLE);

        populateMenu();
        populateModular();
    }

    private final Slot menuItem = Slot.builder(slot -> slot
            .index(4)
            .material(Material.CHEST)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Block Modification"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Select any block to view more"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>details or make changes"))
            ))
    ).build();

    private final Slot shiftPreviousItem = Slots.PREVIOUS_ITEM
            .index(7)
            .clickAction((slotInfo, clickType) -> rotatePrevious(slotInfo, clickType, 9))
            .build();

    private final Slot shiftNextItem = Slots.NEXT_ITEM
            .index(52)
            .clickAction((slotInfo, clickType) -> rotateNext(slotInfo, clickType, 9))
            .build();

    private final Slot returnItem = Slots.RETURN_ITEM
            .index(49)
            .clickAction((action, clickType) -> super.returnToPreviousMenu())
            .build();

    public void populateMenu() {
        this.setSlot(this.menuItem);
        this.setSlot(this.returnItem);

        if(shiftIndex > 0) this.setSlot(this.shiftPreviousItem);
        else this.unsetSlot(this.shiftPreviousItem.getIndex());
        if(shiftIndex + MODULAR_SLOTS.size() < itemPopulation.size()) this.setSlot(this.shiftNextItem);
        else this.unsetSlot(this.shiftNextItem.getIndex());
    }

    public void populateModular() {
        List<DBlock> shiftedList = new LinkedList<>(itemPopulation).subList(shiftIndex, Math.min(shiftIndex + MODULAR_SLOTS.size(), itemPopulation.size()));

        Iterator<Integer> iterator = MODULAR_SLOTS.iterator();
        shiftedList.iterator().forEachRemaining(item -> {
            if(iterator.hasNext()) {
                setSlot(createSlot(iterator.next(), item));
            }
        });
        super.getSlotHolder().getRemainingSlots(MODULAR_SLOTS).forEach(slotNum -> super.setSlot(Slots.EMPTY_ITEM.index(slotNum).build()));
        super.getSlotHolder().getRemainingSlots().forEach(slotNum -> super.setSlot(Slots.FILLER_ITEM.index(slotNum).build()));
    }

    public Slot createSlot(int index, DBlock dBlock) {
        return Slot.builder(builder -> builder
                        .index(index))
                .material(dBlock.getBlockMaterial())
                .displayName(Component.text(dBlock.getId()))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Strength <white>" + FormatUtil.romanNumeral(dBlock.getBlockStrength())),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Durability <white>" + dBlock.getBlockDurability()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Cooldown <white>" + FormatUtil.getTime(dBlock.getBlockCooldown(), true)),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Correct Tools:"),
                        MiniMessage.miniMessage().deserialize("<!italic><white>" + FormatUtil.getList(dBlock.getCorrectTools())),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Item Drops <yellow>" + "Right Click to view"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Break Sound <white>" + dBlock.getBreakSound()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Break Particles <white>" + dBlock.getBreakParticle())
                )))
                .clickAction((slotInfo, clickType) -> openBlockMenuDrops(slotInfo, clickType, dBlock))
                .build();
    }

    private void openBlockMenuDrops(Slot slotInfo, ClickType clickType, DBlock dBlock) {
        if(clickType.isRightClick()) {
            new BlockMenuDrops(getDPlayer(), Registries.DESTRUCTIBLE_BLOCKS.get(dBlock.getId())).openInventory();
        }
    }

    public void refresh() {
        super.unsetSlots(MODULAR_SLOTS);
        populateMenu();
        populateModular();
    }

    public void rotateNext(Slot slotInfo, ClickType clickType, int amount) {
        if(clickType.isShiftClick()) this.shiftIndex += MODULAR_SLOTS.size();
        else this.shiftIndex += amount;

        do {
            this.shiftIndex -= amount;
        } while (shiftIndex + MODULAR_SLOTS.size() > itemPopulation.size());
        this.shiftIndex += amount;

        this.refresh();
    }

    public void rotatePrevious(Slot slotInfo, ClickType clickType, int amount) {
        if(clickType.isShiftClick()) this.shiftIndex -= MODULAR_SLOTS.size();
        else this.shiftIndex -= amount;

        this.shiftIndex = Math.max(this.shiftIndex, 0);

        this.refresh();
    }

    @Override
    public void onInteract(InventoryInteractEvent event) {
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
    }
}