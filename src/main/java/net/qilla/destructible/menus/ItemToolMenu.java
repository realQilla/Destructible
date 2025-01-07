package net.qilla.destructible.menus;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.menus.input.SignInput;
import net.qilla.destructible.mining.item.DItemStack;
import net.qilla.destructible.mining.item.DTool;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.player.PlayType;
import net.qilla.destructible.util.FormatUtil;
import net.qilla.destructible.util.ComponentUtil;
import net.qilla.destructible.util.RandomUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ItemToolMenu extends DestructibleMenu {
    private static final GUISize SIZE = GUISize.SIX;
    private static final Component TITLE = MiniMessage.miniMessage().deserialize("Destructible Tools");
    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    );

    private int shiftIndex = 0;
    private final List<DTool> itemPopulation = Registries.DESTRUCTIBLE_ITEMS.values().stream().filter(DTool.class::isInstance).map(item -> (DTool) item).sorted(Comparator.comparing(DTool::getId)).toList();

    public ItemToolMenu(DPlayer dPlayer) {
        super(dPlayer, SIZE, TITLE);

        populateMenu();
        populateModular();
    }

    private final Slot menuItem = Slot.builder(slot -> slot
            .index(4)
            .material(Material.IRON_PICKAXE)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Tool Modification"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Select any tool to view more details or make changes"))))
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

    public void populateMenu() {
        this.setSlot(this.menuItem);
        this.setSlot(this.returnItem);

        if(shiftIndex > 0) this.setSlot(this.shiftPreviousItem);
        else this.unsetSlot(this.shiftPreviousItem.getIndex());
        if(shiftIndex + MODULAR_SLOTS.size() < itemPopulation.size()) this.setSlot(this.shiftNextItem);
        else this.unsetSlot(this.shiftNextItem.getIndex());
    }

    public void populateModular() {
        List<DTool> shiftedList = new LinkedList<>(itemPopulation).subList(shiftIndex, Math.min(shiftIndex + MODULAR_SLOTS.size(), itemPopulation.size()));

        Iterator<Integer> iterator = MODULAR_SLOTS.iterator();
        shiftedList.iterator().forEachRemaining(item -> {
            if(iterator.hasNext()) {
                setSlot(createSlot(iterator.next(), item));
            }
        });
        super.getSlotHolder().getRemainingSlots(MODULAR_SLOTS).forEach(slotNum -> super.setSlot(Slots.EMPTY_ITEM.index(slotNum).build()));
        super.getSlotHolder().getRemainingSlots().forEach(slotNum -> super.setSlot(Slots.FILLER_ITEM.index(slotNum).build()));
    }

    public Slot createSlot(int index, DTool dTool) {
        return Slot.builder(builder -> builder
                .index(index)
                .material(dTool.getMaterial())
                .displayName(MiniMessage.miniMessage().deserialize(dTool.getId()))
                .lore(ItemLore.lore()
                        .addLines(List.of(
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Name ").append(dTool.getDisplayName()),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Lore:")
                        ))
                        .addLines(dTool.getLore().lines())
                        .addLines(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Tool Strength <white>" + FormatUtil.romanNumeral(dTool.getToolStrength())),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Breaking Efficiency <white>" + dTool.getBreakingEfficiency()),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Tool Durability <white>" + dTool.getToolDurability()),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Tool Type: <white>"),
                                MiniMessage.miniMessage().deserialize("<!italic><white>" + FormatUtil.getList(dTool.getToolType())),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Rarity ").append(dTool.getRarity().getComponent()),
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to get this item"),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Shift-Left Click to select an amount")
                        ))
                        .build())
                .clickAction((slotInfo, clickType) -> this.getItem(slotInfo, clickType, dTool))
        ).build();
    }

    public void getItem(Slot slotInfo, ClickType clickType, DTool dTool) {
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

                        getDPlayer().give(DItemStack.of(dTool, value));
                        getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>You received ").append(ComponentUtil.getItem(dTool, value)).append(MiniMessage.miniMessage().deserialize("!")));
                        getDPlayer().playSound(Sound.ITEM_BUNDLE_DROP_CONTENTS, SoundCategory.PLAYERS, 1, RandomUtil.between(0.5f, 1.5f), PlayType.PLAYER);
                    } catch(NumberFormatException ignored) {
                    }
                    super.reopenInventory();
                });
            });
        } else if(clickType.isLeftClick()) {
            getDPlayer().give(DItemStack.of(dTool, 1));
            getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>You received ").append(ComponentUtil.getItem(dTool, 1)).append(MiniMessage.miniMessage().deserialize("!")));
            getDPlayer().playSound(Sound.ITEM_BUNDLE_REMOVE_ONE, SoundCategory.PLAYERS, 1, RandomUtil.between(0.5f, 1.5f), PlayType.PLAYER);
        }
    }

    public void refresh() {
        super.unsetSlots(MODULAR_SLOTS);
        populateMenu();
        populateModular();
    }

    public void rotateNext(Slot slotInfo, ClickType clickType, int amount) {
        if(clickType.isShiftClick()) this.shiftIndex += MODULAR_SLOTS.size();
        else this.shiftIndex += amount;

        do {
            this.shiftIndex -= amount;
        } while (shiftIndex + MODULAR_SLOTS.size() > itemPopulation.size());
        this.shiftIndex += amount;

        this.refresh();
    }

    public void rotatePrevious(Slot slotInfo, ClickType clickType, int amount) {
        if(clickType.isShiftClick()) this.shiftIndex -= MODULAR_SLOTS.size();
        else this.shiftIndex -= amount;

        if(shiftIndex < 0) shiftIndex = 0;

        this.refresh();
    }

    @Override
    public void onInteract(InventoryInteractEvent event) {
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
    }
}