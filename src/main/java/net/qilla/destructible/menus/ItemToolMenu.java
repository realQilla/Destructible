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
import net.qilla.destructible.util.RandomUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ItemToolMenu extends DestructibleMenu {
    private static final GUISize SIZE = GUISize.SIX;
    private static final Component TITLE = MiniMessage.miniMessage().deserialize("Tools");

    private final List<Integer> toolSlots = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    );

    private final List<DTool> toolList;
    private int shiftIndex = 0;

    private final Slot menuItem = Slot.builder(slot -> slot
            .index(4)
            .material(Material.GOLDEN_PICKAXE)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Tool Modification"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Select any tool to view more details or make changes"))))
    ).build();

    private final Slot shiftUpItem = Slots.UP_ITEM
            .index(7)
            .clickAction(action -> this.shift(-9))
            .build();

    private final Slot shiftDownItem = Slots.DOWN_ITEM
            .index(52)
            .clickAction(action -> this.shift(9))
            .build();

    private final Slot backItem = Slots.BACK_ITEM
            .index(49)
            .clickAction(action -> super.returnToPreviousMenu())
            .build();

    public ItemToolMenu(DPlayer dPlayer) {
        super(dPlayer, SIZE, TITLE);

        this.toolList = Registries.DESTRUCTIBLE_ITEMS.values().stream().filter(DTool.class::isInstance).map(DTool.class::cast).toList();

        this.setSlot(this.menuItem);
        this.setSlot(this.backItem);
        initTools();
    }

    private void initTools() {
        if(shiftIndex > 0) this.setSlot(this.shiftUpItem);
        else this.unsetSlot(this.shiftUpItem.getIndex());
        if(shiftIndex + toolSlots.size() < this.toolList.size()) this.setSlot(this.shiftDownItem);
        else this.unsetSlot(this.shiftDownItem.getIndex());

        List<DTool> shiftedList = new LinkedList<>(toolList).subList(shiftIndex, toolList.size());

        Iterator<Integer> iterator = toolSlots.iterator();
        shiftedList.iterator().forEachRemaining(tool -> {
            if(iterator.hasNext()) {
                Slot slot = Slot.builder(builder -> builder
                        .index(iterator.next())
                        .material(tool.getMaterial())
                        .displayName(MiniMessage.miniMessage().deserialize(tool.getId()))
                        .lore(ItemLore.lore()
                                .addLines(List.of(
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Name ").append(tool.getDisplayName()),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Lore:")
                                ))
                                .addLines(tool.getLore().lines())
                                .addLines(List.of(
                                        Component.empty(),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Tool Strength <white>" + FormatUtil.romanNumeral(tool.getToolStrength())),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Breaking Efficiency <white>" + tool.getBreakingEfficiency()),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Tool Durability <white>" + tool.getToolDurability()),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Tool Type: <white>"),
                                        MiniMessage.miniMessage().deserialize("<!italic><white>" + FormatUtil.getList(tool.getToolType())),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Rarity ").append(tool.getRarity().getComponent()),
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

                                        dPlayer.give(DItemStack.of(tool, value));
                                        dPlayer.sendMessage(MiniMessage.miniMessage().deserialize("<green>You received ").append(tool.getDisplayName()).append(MiniMessage.miniMessage().deserialize("<green>!")));
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
            super.getSlotHolder().getRemainingSlots(toolSlots).forEach(slotNum -> super.setSlot(Slots.EMPTY_ITEM.index(slotNum).build()));
            super.getSlotHolder().getRemainingSlots().forEach(slotNum -> super.setSlot(Slots.FILLER_ITEM.index(slotNum).build()));
        });
    }

    private void shift(int amount) {
        shiftIndex += amount;
        super.unsetSlots(toolSlots);
        this.initTools();
    }

    @Override
    public void onInteract(InventoryInteractEvent event) {

    }

    @Override
    public void onOpen(InventoryOpenEvent event) {

    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }
}