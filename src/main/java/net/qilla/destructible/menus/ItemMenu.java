package net.qilla.destructible.menus;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.menus.input.SignInput;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.DItemStack;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.player.PlayType;
import net.qilla.destructible.util.RandomUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ItemMenu extends DestructibleMenu {
    private static final GUISize SIZE = GUISize.SIX;
    private static final Component TITLE = MiniMessage.miniMessage().deserialize("Items");

    private final List<Integer> itemSlots = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    );

    private final List<DItem> itemList;
    private int shiftIndex = 0;

    private final Slot menuItem = Slot.builder(slot -> slot
            .index(4)
            .material(Material.LAPIS_LAZULI)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Item Modification"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Select any item to view more"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>details or make changes"))
            ))
    ).build();

    private final Slot returnItem = Slots.BACK_ITEM
            .index(49)
            .clickAction(action -> super.returnToPreviousMenu())
            .build();

    private final Slot shiftUpItem = Slots.UP_ITEM
            .index(7)
            .clickAction(action -> this.shift(-9))
            .build();

    private final Slot shiftDownItem = Slots.DOWN_ITEM
            .index(52)
            .clickAction(action -> this.shift(9))
            .build();

    private final Slot toolMenuItem = Slot.builder(slot -> slot
            .index(47)
            .material(Material.GOLDEN_PICKAXE)
            .displayName(MiniMessage.miniMessage().deserialize("<gold>Destructible Tools"))
            .lore(ItemLore.lore(List.of(MiniMessage.miniMessage().deserialize("<!italic><gray>Click to view destructible tools"))))
            .clickAction(action -> {
                new ItemToolMenu(super.getDPlayer()).openInventory();
            })
    ).build();

    private final Slot weaponMenuItem = Slot.builder(slot -> slot
            .index(46)
            .material(Material.GOLDEN_SWORD)
            .displayName(MiniMessage.miniMessage().deserialize("<gold>Destructible Weapons"))
            .lore(ItemLore.lore(List.of(MiniMessage.miniMessage().deserialize("<!italic><gray>Click to view destructible weapons"))))
            .clickAction(action -> {
            })
    ).build();

    public ItemMenu(DPlayer dPlayer) {
        super(dPlayer, SIZE, TITLE);
        this.itemList = Registries.DESTRUCTIBLE_ITEMS.values().stream().filter(item -> item.getClass().equals(DItem.class)).toList();
        this.setSlot(this.menuItem);
        this.setSlot(this.returnItem);
        this.setSlot(this.toolMenuItem);
        this.setSlot(this.weaponMenuItem);
        initItems();
    }

    private void initItems() {
        if(shiftIndex > 0) this.setSlot(this.shiftUpItem);
        else this.unsetSlot(this.shiftUpItem.getIndex());
        if(shiftIndex + itemSlots.size() < this.itemList.size()) this.setSlot(this.shiftDownItem);
        else this.unsetSlot(this.shiftDownItem.getIndex());

        List<DItem> shiftedList = new LinkedList<>(itemList).subList(shiftIndex, itemList.size());

        Iterator<Integer> iterator = itemSlots.iterator();
        shiftedList.iterator().forEachRemaining(item -> {
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

                            if(dPlayer.getCooldown().has(CooldownType.GET_ITEM)) return;
                            dPlayer.getCooldown().set(CooldownType.GET_ITEM);

                            SignInput signInput = new SignInput(dPlayer);
                            signInput.init(List.of(
                                    "^^^^^^^^^^^^^^^",
                                    "Amount to receive",
                                    ""), result -> {
                                Bukkit.getScheduler().runTask(dPlayer.getPlugin(), () -> {
                                    try {
                                        int value = Integer.parseInt(result);

                                        dPlayer.give(DItemStack.of(item, value));
                                        dPlayer.sendMessage(MiniMessage.miniMessage().deserialize("<green>You received ").append(item.getDisplayName()).append(MiniMessage.miniMessage().deserialize("<green>!")));
                                        dPlayer.playSound(Sound.ITEM_BUNDLE_REMOVE_ONE, SoundCategory.PLAYERS, 1, RandomUtil.between(0.5f, 1.5f), PlayType.PLAYER);
                                    } catch(NumberFormatException ignored) {
                                    }
                                    super.reopenInventory();
                                    shift(0);
                                });
                            });
                        })
                ).build();
                setSlot(slot);
            }
            super.getSlotHolder().getRemainingSlots(itemSlots).forEach(slotNum -> super.setSlot(Slots.EMPTY_ITEM.index(slotNum).build()));
            super.getSlotHolder().getRemainingSlots().forEach(slotNum -> super.setSlot(Slots.FILLER_ITEM.index(slotNum).build()));
        });
    }

    private void shift(int amount) {
        shiftIndex += amount;
        super.unsetSlots(itemSlots);
        this.initItems();
    }

    @Override
    public void onInteract(InventoryInteractEvent event) {
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
    }
}