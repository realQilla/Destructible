package net.qilla.destructible.menus;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.menus.input.SignInput;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.mining.item.DItemStack;
import net.qilla.destructible.mining.item.DTool;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.player.PlayType;
import net.qilla.destructible.util.FormatUtil;
import net.qilla.destructible.util.ComponentUtil;
import net.qilla.destructible.util.RandomUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ItemToolMenu extends ModularMenu {
    private static final MenuSize SIZE = MenuSize.SIX;
    private static final Component TITLE = MiniMessage.miniMessage().deserialize("Destructible Tools");
    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    );

    private static final List<DTool> ITEM_POPULATION = Registries.DESTRUCTIBLE_ITEMS.values().stream().filter(DTool.class::isInstance).map(item -> (DTool) item).sorted(Comparator.comparing(DTool::getId)).toList();
    private final Slot nextSlot;
    private final Slot previousSlot;

    public ItemToolMenu(DPlayer dPlayer) {
        super(dPlayer, SIZE, TITLE, MODULAR_SLOTS, ITEM_POPULATION.size());

        super.register(Slot.of(4, Displays.TOOL_MENU));
        super.register(Slot.of(49, Displays.RETURN, (slot, clickType) -> returnToPreviousMenu()));
        this.nextSlot = Slot.of(52, Displays.NEXT, (slot, clickType) -> rotateNext(slot, clickType, 9));
        this.previousSlot = Slot.of(7, Displays.PREVIOUS, (slot, clickType) -> rotatePrevious(slot, clickType, 9));

        if(ITEM_POPULATION.size() > MODULAR_SLOTS.size()) this.register(nextSlot);

        populateModular();
    }

    public void populateModular() {
        List<DTool> shiftedList = new LinkedList<>(ITEM_POPULATION).subList(super.getShiftIndex(), Math.min(super.getShiftIndex() + MODULAR_SLOTS.size(), ITEM_POPULATION.size()));

        Iterator<Integer> iterator = MODULAR_SLOTS.iterator();
        shiftedList.iterator().forEachRemaining(item -> {
            if(iterator.hasNext()) {
                register(createSlot(iterator.next(), item));
            }
        });

        super.getSocket().getRemaining(MODULAR_SLOTS).forEach(slotNum -> super.register(Slot.of(slotNum, Displays.EMPTY_SLOT)));
        super.getSocket().getRemaining().forEach(slotNum -> super.register(Slot.of(slotNum, Displays.FILLER)));
    }

    public Slot createSlot(int index, DTool dTool) {
        net.qilla.destructible.menus.Display display = net.qilla.destructible.menus.Display.of(builder -> builder
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
        );
        return Slot.of(index, display, (slot, clickType) -> getItem(slot, clickType, dTool));
    }

    public void getItem(Slot slot, ClickType clickType, DTool dTool) {
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