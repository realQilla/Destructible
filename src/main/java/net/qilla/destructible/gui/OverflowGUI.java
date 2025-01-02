package net.qilla.destructible.gui;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.DItemStack;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class OverflowGUI extends DestructibleGUI {
    private static final GUISize SIZE = GUISize.SIX;
    private static final Component TITLE = MiniMessage.miniMessage().deserialize("Overflow Items");

    private static final List<Integer> overflowedSlots = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            44, 45, 46, 47, 48, 49, 50, 51, 52
    );

    private final Slot menuItem = Slot.builder(slot -> slot
                    .index(4)
                    .material(Material.BROWN_BUNDLE)
                    .displayName(MiniMessage.miniMessage().deserialize("<gold>Overflowed Items"))
            )
            .build();

    public OverflowGUI(DPlayer dPlayer) {
        super(dPlayer, SIZE, TITLE);
        slotHolder.registerSlot(menuItem);
        setSlot(menuItem);
        initOverflowItems();
    }

    private void initOverflowItems() {
        List<DItemStack> overflowItems = dPlayer.getOverflow().getItems();
        Iterator<Integer> iterator = overflowedSlots.iterator();
        overflowItems.iterator().forEachRemaining(item -> {
            ItemStack itemStack = item.getItemStack();
            if (iterator.hasNext()) {
                Slot slot = Slot.builder(builder -> builder
                                .index(iterator.next())
                                .material(itemStack.getType())
                                .amount(item.getAmount())
                                .displayName(item.getDItem().getDisplayName().append(MiniMessage.miniMessage().deserialize("<white> x" + item.getAmount())))
                                .lore(ItemLore.lore()
                                        .addLines(item.getDItem().getLore().lines())
                                        .build()))
                        .clickAction(action -> giveOverflow(item))
                        .build();
                slotHolder.registerSlot(slot);
                setSlot(slot);
            }
        });
    }

    private void giveOverflow(DItemStack item) {
        if(dPlayer.getSpace(item.getItemStack()) <= 0) {
            dPlayer.sendMessage(MiniMessage.miniMessage().deserialize("<red>You don't have enough space in your inventory."));
            dPlayer.playSound(dPlayer.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1f);
            return;
        }
        dPlayer.getOverflow().take(item.getDItem());
        dPlayer.playSound(dPlayer.getLocation(), Sound.ENTITY_HORSE_SADDLE, 0.5f, 2f);
        unsetSlots(overflowedSlots);
        initOverflowItems();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return super.inventory;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
    }

    @Override
    public void onClick(InventoryInteractEvent event) {
        event.setCancelled(true);
    }
}