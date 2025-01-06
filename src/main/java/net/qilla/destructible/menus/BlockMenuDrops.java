package net.qilla.destructible.menus;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DDrop;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.DItemStack;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.FormatUtil;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import java.util.List;

public class BlockMenuDrops extends ModularMenu {
    private static final GUISize SIZE = GUISize.SIX;
    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 19, 28, 37, 38, 39, 30, 21, 12, 13, 14, 23, 32, 41, 42, 43, 34, 25, 16, 17
    );

    private DBlock dBlock;

    public BlockMenuDrops(DPlayer dPlayer, DBlock dBlock) {
        super(dPlayer, SIZE, MiniMessage.miniMessage().deserialize(dBlock.getId() + " Item Drops"),
                Registries.DESTRUCTIBLE_BLOCKS.get(dBlock.getId()).getItemDrops().stream().map(dDrop -> DItemStack.of(createDItem(dDrop), Math.min(1, dDrop.getMinAmount()))).toList(), MODULAR_SLOTS);

        populateMenu();
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

    @Override
    public void populateMenu() {
        this.setSlot(this.menuItem);
        this.setSlot(this.returnItem);

        if(getShiftIndex() > 0) this.setSlot(this.shiftPreviousItem);
        else this.unsetSlot(this.shiftPreviousItem.getIndex());
        if(getShiftIndex() + getModularSlots().size() < getItemPopulation().size()) this.setSlot(this.shiftNextItem);
        else this.unsetSlot(this.shiftNextItem.getIndex());
    }

    @Override
    public Slot createSlot(int index, DItemStack item) {
        ItemStack itemStack = item.getItemStack();
        return Slot.builder(builder -> builder
                        .index(index)
                        .material(Registry.MATERIAL.get(itemStack.getData(DataComponentTypes.ITEM_MODEL)))
                        .displayName(itemStack.getData(DataComponentTypes.ITEM_NAME))
                        .lore(itemStack.getData(DataComponentTypes.LORE)))
                .build();
    }

    private static DItem createDItem(DDrop dDrop) {
        DItem dItem = dDrop.getDItem();
        return new DItem.Builder()
                .material(dItem.getMaterial())
                .displayName(dItem.getDisplayName())
                .lore(ItemLore.lore().addLines(dItem.getLore().lines())
                        .addLines(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Drop Amount <white>" + dDrop.getMinAmount() + " - " + dDrop.getMaxAmount()),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Drop Chance <white>" + FormatUtil.decimalTruncation(dDrop.getChance() * 100, 2) + "%")
                        )).build())
                .build();
    }

    @Override
    public void onInteract(InventoryInteractEvent event) {
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
    }
}