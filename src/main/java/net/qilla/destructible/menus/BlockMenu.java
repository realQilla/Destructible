package net.qilla.destructible.menus;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.block.DBlock;
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

public class BlockMenu extends ModularMenu {
    private static final GUISize SIZE = GUISize.SIX;
    private static final Component TITLE = MiniMessage.miniMessage().deserialize("Blocks");
    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    );

    public BlockMenu(DPlayer dPlayer) {
        super(dPlayer, SIZE, TITLE,
                Registries.DESTRUCTIBLE_BLOCKS.values().stream().map(dBlock -> DItemStack.of(createDItem(dBlock), 1)).toList(),
                MODULAR_SLOTS);

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
                .index(index))
                .material(Registry.MATERIAL.get(itemStack.getData(DataComponentTypes.ITEM_MODEL)))
                .displayName(itemStack.getData(DataComponentTypes.ITEM_NAME))
                .lore(itemStack.getData(DataComponentTypes.LORE))
                .clickAction((slotInfo, clickType) -> {
                    if(clickType.isRightClick())
                    new BlockMenuDrops(getDPlayer(), Registries.DESTRUCTIBLE_BLOCKS.get(item.getDItem().getId())).openInventory();
                })
        .build();
    }

    private static DItem createDItem(DBlock dBlock) {
        return new DItem.Builder()
                .id(dBlock.getId())
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
                .build();
    }

    @Override
    public void onInteract(InventoryInteractEvent event) {
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
    }
}