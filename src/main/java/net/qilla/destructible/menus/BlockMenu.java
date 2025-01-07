package net.qilla.destructible.menus;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.FormatUtil;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BlockMenu extends ModularMenu {

    private static final MenuSize SIZE = MenuSize.SIX;
    private static final Component TITLE = MiniMessage.miniMessage().deserialize("Destructible Blocks");
    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    );

    private static final List<DBlock> ITEM_POPULATION = Registries.DESTRUCTIBLE_BLOCKS.values().stream().sorted(Comparator.comparing(DBlock::getId)).toList();
    private final Slot nextSlot;
    private final Slot previousSlot;

    public BlockMenu(DPlayer dPlayer) {
        super(dPlayer, SIZE, TITLE, MODULAR_SLOTS, ITEM_POPULATION.size());

        super.register(Slot.of(4, Displays.BLOCK_MENU));
        super.register(Slot.of(46, createMenu, (slot, clickType) -> new BlockMenuModify(super.getDPlayer()).openInventory()));
        super.register(Slot.of(49, Displays.RETURN, (slot, clickType) -> returnToPreviousMenu()));
        this.nextSlot = Slot.of(52, Displays.NEXT, (slot, clickType) -> rotateNext(slot, clickType, 9));
        this.previousSlot = Slot.of(7, Displays.PREVIOUS, (slot, clickType) -> rotatePrevious(slot, clickType, 9));

        if(ITEM_POPULATION.size() > MODULAR_SLOTS.size()) this.register(nextSlot);

        populateModular();
    }

    private final net.qilla.destructible.menus.Display createMenu = net.qilla.destructible.menus.Display.of(consumer -> consumer
            .material(Material.SHULKER_SHELL)
            .displayName(MiniMessage.miniMessage().deserialize("<dark_green>Create New"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Click to create a new destructible block"))
            ))
    );

    public void populateModular() {
        List<DBlock> shiftedList = new LinkedList<>(ITEM_POPULATION).subList(super.getShiftIndex(), Math.min(super.getShiftIndex() + MODULAR_SLOTS.size(), ITEM_POPULATION.size()));

        Iterator<Integer> iterator = MODULAR_SLOTS.iterator();
        shiftedList.iterator().forEachRemaining(dBlock -> {
            if(iterator.hasNext()) {
                register(createSlot(iterator.next(), dBlock));
            }
        });

        super.getSocket().getRemaining(MODULAR_SLOTS).forEach(slotNum -> super.register(Slot.of(slotNum, Displays.EMPTY_SLOT)));
        super.getSocket().getRemaining().forEach(slotNum -> super.register(Slot.of(slotNum, Displays.FILLER)));
    }

    public Slot createSlot(int index, DBlock dBlock) {
        net.qilla.destructible.menus.Display display = net.qilla.destructible.menus.Display.of(builder -> builder
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
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Break Particles <white>" + dBlock.getBreakParticle()),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Click to modify block")
                )))
        );
        return Slot.of(index, display, (slot, clickType) -> blockClickInteraction(slot, clickType, dBlock));
    }

    private void blockClickInteraction(Slot slot, ClickType clickType, DBlock dBlock) {
        if(clickType.isLeftClick()) {
            new BlockMenuModify(getDPlayer(), dBlock).openInventory();
        } else if(clickType.isRightClick()) {
            new BlockMenuDrops(getDPlayer(), slot, Registries.DESTRUCTIBLE_BLOCKS.get(dBlock.getId())).openInventory();
        }
    }

    public void refresh() {
        MODULAR_SLOTS.forEach(super::unregister);
        int shiftIndex = super.getShiftIndex();

        if(shiftIndex > 0) this.register(this.previousSlot);
        else super.unregister(this.previousSlot.getIndex());
        if(shiftIndex + MODULAR_SLOTS.size() < ITEM_POPULATION.size()) this.register(this.nextSlot);
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