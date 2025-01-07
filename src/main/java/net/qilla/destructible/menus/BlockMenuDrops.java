package net.qilla.destructible.menus;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DDrop;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.FormatUtil;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BlockMenuDrops extends ModularMenu {
    private static final MenuSize SIZE = MenuSize.SIX;
    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 19, 28, 37, 38, 39, 30, 21, 12, 13, 14, 23, 32, 41, 42, 43, 34, 25, 16, 17
    );

    private final List<DDrop> itemPopulation;
    private final Slot nextSlot;
    private final Slot previousSlot;

    public BlockMenuDrops(DPlayer dPlayer, Slot slotInfo, DBlock dBlock) {
        super(dPlayer, SIZE, MiniMessage.miniMessage().deserialize("Item Drops"), MODULAR_SLOTS, dBlock.getItemDrops().size());
        Preconditions.checkNotNull(dBlock, "DBlock cannot be null.");
        this.itemPopulation = dBlock.getItemDrops().stream().sorted((Comparator.comparingDouble(DDrop::getChance).reversed())).toList();

        net.qilla.destructible.menus.Display display = slotInfo.getDisplay().modify(consumer -> consumer
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
        );

        super.register(Slot.of(4, display));
        super.register(Slot.of(49, Displays.RETURN, (slot, clickType) -> returnToPreviousMenu()));
        this.nextSlot = Slot.of(52, Displays.NEXT, (slot, clickType) -> rotateNext(slot, clickType, 1));
        this.previousSlot = Slot.of(46, Displays.PREVIOUS, (slot, clickType) -> rotatePrevious(slot, clickType, 1));

        if(itemPopulation.size() > MODULAR_SLOTS.size()) this.register(nextSlot);

        populateModular();
    }

    public void populateModular() {
        List<DDrop> shiftedList = new LinkedList<>(itemPopulation).subList(super.getShiftIndex(), Math.min(super.getShiftIndex() + MODULAR_SLOTS.size(), itemPopulation.size()));

        Iterator<Integer> iterator = MODULAR_SLOTS.iterator();
        shiftedList.iterator().forEachRemaining(item -> {
            if(iterator.hasNext()) {
                register(createSlot(iterator.next(), item));
            }
        });

        super.getSocket().getRemaining(MODULAR_SLOTS).forEach(slotNum -> super.register(Slot.of(slotNum, Displays.EMPTY_SLOT)));
        super.getSocket().getRemaining().forEach(slotNum -> super.register(Slot.of(slotNum, Displays.FILLER)));
    }

    public Slot createSlot(int index, DDrop dDrop) {
        DItem dItem = dDrop.getDItem();

        net.qilla.destructible.menus.Display display = net.qilla.destructible.menus.Display.of(builder -> builder
                .material(dItem.getMaterial())
                .displayName(dItem.getDisplayName())
                .lore(ItemLore.lore().addLines(dItem.getLore().lines())
                        .addLines(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Drop Amount <white>" +
                                        dDrop.getMinAmount() + " - " + dDrop.getMaxAmount()),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Drop Chance <white>" +
                                        FormatUtil.decimalTruncation(dDrop.getChance() * 100, 17) + "% (1/" + FormatUtil.numberComma((long) Math.ceil(1 / dDrop.getChance())) + ")")
                        )).build()
                )
        );
        return Slot.of(index, display);
    }

    public void refresh() {
        MODULAR_SLOTS.forEach(super::unregister);
        int shiftIndex = super.getShiftIndex();

        if(shiftIndex > 0) this.register(this.previousSlot);
        else super.unregister(this.previousSlot.getIndex());
        if(shiftIndex + MODULAR_SLOTS.size() < itemPopulation.size()) this.register(this.nextSlot);
        else super.unregister(this.nextSlot.getIndex());

        populateModular();
    }

    @Override
    public void onInteract(InventoryInteractEvent event) {
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
    }
}