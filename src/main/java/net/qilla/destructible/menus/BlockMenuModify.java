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

public class BlockMenuModify extends DestructibleMenu {

    private static final GUISize SIZE = GUISize.SIX;
    private static final Component TITLE = MiniMessage.miniMessage().deserialize("Destructible Blocks");

    private int shiftIndex = 0;
    private final List<DBlock> itemPopulation = Registries.DESTRUCTIBLE_BLOCKS.values().stream().toList();

    public BlockMenuModify(DPlayer dPlayer) {
        super(dPlayer, SIZE, TITLE);

        populateMenu();
    }

    public BlockMenuModify(DPlayer dPlayer, DBlock dBlock) {
        super(dPlayer, SIZE, TITLE);

        populateMenu();
    }

    private final Slot menuItem = Slot.builder(slot -> slot
            .index(4)
            .material(Material.CHEST)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Block Creation"))
    ).build();

    private final Slot returnItem = Slots.RETURN_ITEM
            .index(49)
            .clickAction((action, clickType) -> super.returnToPreviousMenu())
            .build();

    public void populateMenu() {
        this.setSlot(this.menuItem);
        this.setSlot(this.returnItem);

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
            new BlockMenuDrops(getDPlayer(), slotInfo, Registries.DESTRUCTIBLE_BLOCKS.get(dBlock.getId())).openInventory();
        }
    }

    @Override
    public void onInteract(InventoryInteractEvent event) {
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
    }
}