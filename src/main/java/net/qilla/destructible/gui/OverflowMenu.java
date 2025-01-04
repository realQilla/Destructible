package net.qilla.destructible.gui;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.mining.item.DItemStack;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.player.PlaySound;
import net.qilla.destructible.util.RandomUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.Iterator;
import java.util.List;

public class OverflowMenu extends DestructibleMenu {
    private static final GUISize SIZE = GUISize.SIX;
    private static final Component TITLE = MiniMessage.miniMessage().deserialize("Overflow");

    private final List<Integer> overflowedSlots = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            44, 45, 46, 47, 48, 49, 50, 51, 52
    );

    private final Slot menuItem = Slot.builder(slot -> slot
                    .index(4)
                    .material(Material.BROWN_BUNDLE)
                    .displayName(MiniMessage.miniMessage().deserialize("<gold>Overflowing Items"))
                    .lore(ItemLore.lore(List.of(
                            MiniMessage.miniMessage().deserialize("<!italic><gray>Click to claim any item that wasn't "),
                            MiniMessage.miniMessage().deserialize("<!italic><gray>able to fit in your inventory"))))
            )
            .build();

    private final Slot backItem = Slot.builder(slot -> slot
            .index(53)
            .material(Material.BARRIER)
            .displayName(MiniMessage.miniMessage().deserialize("<red>Return"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Click to return to your"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>previously opened menu"))))
            .clickAction(action -> returnToPreviousMenu())
    ).build();

    public OverflowMenu(DPlayer dPlayer) {
        super(dPlayer, SIZE, TITLE);
        setSlot(menuItem);
        setSlot(backItem);
        initOverflowItems();
    }

    private void initOverflowItems() {
        Iterator<Integer> iterator = overflowedSlots.iterator();
        super.getDPlayer().getOverflow().getItems().iterator().forEachRemaining(item -> {
            if(iterator.hasNext()) {
                Slot slot = Slot.builder(builder -> builder
                                .index(iterator.next())
                                .material(item.getDItem().getMaterial())
                                .amount(item.getAmount())
                                .displayName(item.getDItem().getDisplayName().append(MiniMessage.miniMessage().deserialize("<white> x" + item.getAmount())))
                                .lore(ItemLore.lore().addLine(MiniMessage.miniMessage().deserialize("<!italic><gold><bold>STASHED<gold>"))
                                        .addLines(item.getDItem().getLore().lines())
                                        .addLines(List.of(Component.empty(), item.getDItem().getRarity().getComponent()))
                                        .build()))
                        .clickAction(action -> giveOverflow(item))
                        .build();
                setSlot(slot);
            }
        });
    }

    private void giveOverflow(DItemStack item) {
        if(super.getDPlayer().getSpace(item.getItemStack()) <= 0) {
            super.getDPlayer().sendMessage("<red>You don't have enough space in your inventory.");
            super.getDPlayer().playSound(Sound.ENTITY_VILLAGER_NO, SoundCategory.PLAYERS, 0.5f, RandomUtil.between(0.5f, 1.5f), PlaySound.PLAYER);
            return;
        }
        super.getDPlayer().getOverflow().take(item.getDItem());
        super.getDPlayer().playSound(Sound.ENTITY_HORSE_SADDLE, SoundCategory.PLAYERS, 0.5f, RandomUtil.between(0.5f, 1.0f), PlaySound.PLAYER);
        unsetSlots(overflowedSlots);
        initOverflowItems();
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        super.getDPlayer().playSound(Sound.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, 0.5f, RandomUtil.between(0.5f, 1.0f), PlaySound.PLAYER);
    }

    @Override
    public void onInteract(InventoryInteractEvent event) {
        event.setCancelled(true);
    }
}