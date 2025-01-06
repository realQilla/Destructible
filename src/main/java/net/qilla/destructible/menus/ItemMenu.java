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
import net.qilla.destructible.util.ComponentUtil;
import net.qilla.destructible.util.RandomUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import java.util.List;

public class ItemMenu extends ModularMenu {
    private static final GUISize SIZE = GUISize.SIX;
    private static final Component TITLE = MiniMessage.miniMessage().deserialize("Items");
    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    );

    public ItemMenu(DPlayer dPlayer) {
        super(dPlayer, SIZE, TITLE,
                Registries.DESTRUCTIBLE_ITEMS.values().stream().filter(item -> item.getClass().equals(DItem.class)).map(item -> DItemStack.of(item, 1)).toList(),
                MODULAR_SLOTS);

        populateMenu();
    }

    private final Slot menuItem = Slot.builder(slot -> slot
            .index(4)
            .material(Material.QUARTZ)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Item Modification"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Select any item to view more"),
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

    private final Slot toolMenuItem = Slot.builder(slot -> slot
            .index(47)
            .material(Material.IRON_PICKAXE)
            .displayName(MiniMessage.miniMessage().deserialize("<gold>Destructible Tools"))
            .lore(ItemLore.lore(List.of(MiniMessage.miniMessage().deserialize("<!italic><gray>Click to view destructible tools"))))
            .clickAction((action, clickType) -> new ItemToolMenu(super.getDPlayer()).openInventory())
    ).build();

    private final Slot weaponMenuItem = Slot.builder(slot -> slot
            .index(46)
            .material(Material.NETHERITE_AXE)
            .displayName(MiniMessage.miniMessage().deserialize("<gold>Destructible Weapons"))
            .lore(ItemLore.lore(List.of(MiniMessage.miniMessage().deserialize("<!italic><gray>Left-Click to view destructible weapons"))))
            .clickAction((action, clickType) -> {})
    ).build();

    private final Slot returnItem = Slots.RETURN_ITEM
            .index(49)
            .clickAction((action, clickType) -> super.returnToPreviousMenu())
            .build();

    @Override
    public void populateMenu() {
        this.setSlot(this.menuItem);
        this.setSlot(this.returnItem);
        this.setSlot(this.toolMenuItem);
        this.setSlot(this.weaponMenuItem);

        if(getShiftIndex() > 0) this.setSlot(this.shiftPreviousItem);
        else this.unsetSlot(this.shiftPreviousItem.getIndex());
        if(getShiftIndex() + getModularSlots().size() < getItemPopulation().size()) this.setSlot(this.shiftNextItem);
        else this.unsetSlot(this.shiftNextItem.getIndex());
    }

    @Override
    public Slot createSlot(int index, DItemStack item) {
        DItem dItem = item.getDItem();
        return Slot.builder(builder -> builder
                .index(index)
                .material(dItem.getMaterial())
                .displayName(MiniMessage.miniMessage().deserialize(dItem.getId()))
                .lore(ItemLore.lore()
                        .addLines(List.of(
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Name ").append(dItem.getDisplayName()),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Lore:")
                        ))
                        .addLines(dItem.getLore().lines())
                        .addLines(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Rarity ").append(dItem.getRarity().getComponent()),
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to get this item"),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Shift-Left Click to select an amount")
                        ))
                        .build())
                .clickAction((slotInfo, clickType) -> getItem(slotInfo, clickType, dItem))
        ).build();
    }

    private void getItem(Slot slotInfo, ClickType clickType, DItem item) {
        if(getDPlayer().getCooldown().has(CooldownType.GET_ITEM)) return;
        getDPlayer().getCooldown().set(CooldownType.GET_ITEM);

        if(clickType.isShiftClick() && clickType.isLeftClick()) {
            List<String> signText = List.of(
                    "^^^^^^^^^^^^^^^",
                    "Amount to receive",
                    "");

            SignInput signInput = new SignInput(getDPlayer(), signText);
            signInput.init(result -> {
                Bukkit.getScheduler().runTask(getDPlayer().getPlugin(), () -> {
                    try {
                        int value = Integer.parseInt(result);

                        getDPlayer().give(DItemStack.of(item, value));
                        getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>You received ").append(ComponentUtil.getItem(item, value)).append(MiniMessage.miniMessage().deserialize("!")));
                        getDPlayer().playSound(Sound.ITEM_BUNDLE_DROP_CONTENTS, SoundCategory.PLAYERS, 1, RandomUtil.between(0.5f, 1.5f), PlayType.PLAYER);
                    } catch(NumberFormatException ignored) {
                    }
                    super.reopenInventory();
                });
            });
        } else if(clickType.isLeftClick()) {
            getDPlayer().give(DItemStack.of(item, 1));
            getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>You received ").append(ComponentUtil.getItem(item, 1)).append(MiniMessage.miniMessage().deserialize("!")));
            getDPlayer().playSound(Sound.ITEM_BUNDLE_REMOVE_ONE, SoundCategory.PLAYERS, 1, RandomUtil.between(0.5f, 1.5f), PlayType.PLAYER);
        }
    }

    @Override
    public void onInteract(InventoryInteractEvent event) {
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
    }
}