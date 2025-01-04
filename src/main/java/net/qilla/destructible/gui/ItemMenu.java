package net.qilla.destructible.gui;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.item.DItemStack;
import net.qilla.destructible.mining.item.DTool;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.player.PlaySound;
import net.qilla.destructible.util.RandomUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class ItemMenu extends DestructibleMenu {
    private static final GUISize SIZE = GUISize.SIX;
    private static final Component TITLE = MiniMessage.miniMessage().deserialize("Items");

    private final List<Integer> blockSlots = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35
    );

    private final Slot menuItem = Slot.builder(slot -> slot
            .index(4)
            .material(Material.GRASS_BLOCK)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Item Modification"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Select any item to view more"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>details or make changes"))
            ))
    ).build();

    private final Slot backItem = Slot.builder(slot -> slot
            .index(53)
            .material(Material.BARRIER)
            .displayName(MiniMessage.miniMessage().deserialize("<red>Return"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Click to return to your"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>previously opened menu"))))
            .clickAction(action -> super.returnToPreviousMenu())
    ).build();

    private final Slot toolMenuItem = Slot.builder(slot -> slot
            .index(49)
            .material(Material.GOLDEN_PICKAXE)
            .displayName(MiniMessage.miniMessage().deserialize("<gold>Destructible Tools"))
            .lore(ItemLore.lore(List.of(MiniMessage.miniMessage().deserialize("<!italic><gray>Click to view destructible tools"))))
            .clickAction(action -> {
                new ItemToolMenu(super.getDPlayer()).openInventory();
            })
    ).build();

    public ItemMenu(DPlayer dPlayer) {
        super(dPlayer, SIZE, TITLE);
        this.setSlot(this.menuItem);
        this.setSlot(this.backItem);
        this.setSlot(this.toolMenuItem);
        initItems();
    }

    private void initItems() {
        Iterator<Integer> iterator = blockSlots.iterator();
        Registries.DESTRUCTIBLE_ITEMS.values().stream().filter(item -> !(item instanceof DTool)).forEach(item -> {
            if(iterator.hasNext()) {
                Slot slot = Slot.builder(builder -> builder
                        .index(iterator.next())
                        .material(item.getMaterial())
                        .displayName(MiniMessage.miniMessage().deserialize(item.getId()))
                        .lore(ItemLore.lore()
                                .addLines(List.of(
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Name ").append(item.getDisplayName()),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Lore:")
                                ))
                                .addLines(item.getLore().lines())
                                .addLines(List.of(
                                        Component.empty(),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Rarity ").append(item.getRarity().getComponent()),
                                        Component.empty(),
                                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Click to get this item")
                                ))
                                .build())
                        .clickAction(action -> {
                            DPlayer dPlayer = super.getDPlayer();

                            if (dPlayer.getCooldown().has(CooldownType.GET_ITEM)) return;
                            dPlayer.getCooldown().set(CooldownType.GET_ITEM);
                            dPlayer.give(DItemStack.of(item.getId()));
                            dPlayer.sendMessage(MiniMessage.miniMessage().deserialize("<green>You received ").append(item.getDisplayName()).append(MiniMessage.miniMessage().deserialize("<green>!")));
                            dPlayer.playSound(Sound.ITEM_BUNDLE_REMOVE_ONE, SoundCategory.PLAYERS,1, RandomUtil.between(0.5f, 1.5f), PlaySound.PLAYER);
                        })
                ).build();
                setSlot(slot);
            }
        });
    }

    @Override
    public void onInteract(InventoryInteractEvent event) {
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
    }
}