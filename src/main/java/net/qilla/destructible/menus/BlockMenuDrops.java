package net.qilla.destructible.menus;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DDrop;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.FormatUtil;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BlockMenuDrops extends DestructibleMenu {
    private static final GUISize SIZE = GUISize.SIX;
    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 19, 28, 37, 38, 39, 30, 21, 12, 13, 14, 23, 32, 41, 42, 43, 34, 25, 16, 17
    );

    private int shiftIndex = 0;
    private final DBlock dBlock;
    private final List<DDrop> itemPopulation;

    public BlockMenuDrops(DPlayer dPlayer, Slot slotInfo, DBlock dBlock) {
        super(dPlayer, SIZE, MiniMessage.miniMessage().deserialize("Item Drops"));
        Preconditions.checkNotNull(dBlock, "DBlock cannot be null.");

        this.dBlock = dBlock;
        this.itemPopulation = this.dBlock.getItemDrops().stream().sorted((Comparator.comparingDouble(DDrop::getChance).reversed())).toList();

        this.menuItem = Slot.rebuild(slotInfo, slot -> slot
                .index(4)
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Strength <white>" + FormatUtil.romanNumeral(dBlock.getBlockStrength())),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Durability <white>" + dBlock.getBlockDurability()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Block Cooldown <white>" + FormatUtil.getTime(dBlock.getBlockCooldown(), true)),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Correct Tools:"),
                        MiniMessage.miniMessage().deserialize("<!italic><white>" + FormatUtil.getList(dBlock.getCorrectTools())),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Item Drops <yellow>" + "[Currently viewing]"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Break Sound <white>" + dBlock.getBreakSound()),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Break Particles <white>" + dBlock.getBreakParticle())
                )))
                .glow(true)
                .clickAction(null));

        populateMenu();
        populateModular();
    }

    private final Slot menuItem;

    private final Slot shiftPreviousItem = Slots.PREVIOUS_ITEM
            .index(46)
            .clickAction((slotInfo, clickType) -> rotatePrevious(slotInfo, clickType, 1))
            .build();

    private final Slot shiftNextItem = Slots.NEXT_ITEM
            .index(52)
            .clickAction((slotInfo, clickType) -> rotateNext(slotInfo, clickType, 1))
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
        List<DDrop> shiftedList = new LinkedList<>(itemPopulation).subList(shiftIndex, Math.min(shiftIndex + MODULAR_SLOTS.size(), itemPopulation.size()));

        Iterator<Integer> iterator = MODULAR_SLOTS.iterator();
        shiftedList.iterator().forEachRemaining(item -> {
            if(iterator.hasNext()) {
                setSlot(createSlot(iterator.next(), item));
            }
        });
        super.getSlotHolder().getRemainingSlots(MODULAR_SLOTS).forEach(slotNum -> super.setSlot(Slots.EMPTY_ITEM.index(slotNum).build()));
        super.getSlotHolder().getRemainingSlots().forEach(slotNum -> super.setSlot(Slots.FILLER_ITEM.index(slotNum).build()));
    }

    public Slot createSlot(int index, DDrop dDrop) {

        DItem dItem = dDrop.getDItem();

        return Slot.builder(builder -> builder
                        .index(index)
                        .material(dItem.getMaterial())
                        .displayName(dItem.getDisplayName())
                        .lore(ItemLore.lore().addLines(dItem.getLore().lines())
                                .addLines(List.of(
                                        Component.empty(),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Drop Amount <white>" + dDrop.getMinAmount() + " - " + dDrop.getMaxAmount()),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Drop Chance <white>" + FormatUtil.decimalTruncation(dDrop.getChance() * 100, 17) + "% (1/" + FormatUtil.numberComma((int) (1 / dDrop.getChance())) + ")")
                                )).build()))
                .build();
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

        if(shiftIndex < 0) shiftIndex = 0;

        this.refresh();
    }

    @Override
    public void onInteract(InventoryInteractEvent event) {
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
    }
}