package net.qilla.destructible.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.menus.slot.SlotType;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class BlockMenuModify extends DestructibleMenu {

    private static final MenuSize SIZE = MenuSize.SIX;
    private static final Component TITLE = MiniMessage.miniMessage().deserialize("Destructible Blocks");

    public BlockMenuModify(DPlayer dPlayer) {
        super(dPlayer, SIZE, TITLE);

        super.register(Slot.of(4, Displays.BLOCK_MODIFICATION_MENU));
        super.register(Slot.of(49, Displays.RETURN, (slot, clickType) -> returnToPreviousMenu()), SlotType.RETURN);
    }

    public BlockMenuModify(DPlayer dPlayer, DBlock dBlock) {
        super(dPlayer, SIZE, TITLE);

        super.register(Slot.of(4, Displays.BLOCK_MODIFICATION_MENU));
        super.register(Slot.of(49, Displays.RETURN, (slot, clickType) -> returnToPreviousMenu()), SlotType.RETURN);
    }

    @Override
    public void onInteract(InventoryInteractEvent event) {
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
    }
}