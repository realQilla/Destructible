package net.qilla.destructible.menus;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.menus.input.SignInput;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.DItemStack;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.player.Overflow;
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

import java.util.*;

public class OverflowMenu extends DestructibleMenu {
    private static final GUISize SIZE = GUISize.SIX;
    private static final Component TITLE = MiniMessage.miniMessage().deserialize("Overflow Stash");

    private final List<Integer> overflowSlots = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    );

    private final Map<DItem, DItemStack> overflowItemList;
    private int shiftIndex = 0;

    private final Slot menuItem = Slot.builder(slot -> slot
            .index(4)
            .material(Material.BROWN_BUNDLE)
            .displayName(MiniMessage.miniMessage().deserialize("<gold>Overflowing Items"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Left Click to claim any items that were not "),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>able to fit into your inventory"))))
    ).build();

    private final Slot clearItem = Slot.builder(slot -> slot
            .index(45)
            .material(Material.BARRIER)
            .displayName(MiniMessage.miniMessage().deserialize("<red>Remove <bold>ALL</bold>!"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Click to clear your entire"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>overflow stash")))
            )
            .clickAction(this::clearOverflow)
    ).build();

    private final Slot shiftUpItem = Slots.UP_ITEM
            .index(7)
            .clickAction(this::shiftUp)
            .build();

    private final Slot shiftDownItem = Slots.DOWN_ITEM
            .index(52)
            .clickAction(this::shiftDown)
            .build();

    private final Slot backItem = Slots.BACK_ITEM
            .index(49)
            .clickAction((action, clickType) -> super.returnToPreviousMenu())
            .build();

    public OverflowMenu(DPlayer dPlayer) {
        super(dPlayer, SIZE, TITLE);
        this.overflowItemList = super.getDPlayer().getOverflow().getItems();
        setSlot(menuItem);
        setSlot(clearItem);
        setSlot(backItem);
        initOverflowItems();
    }

    private void initOverflowItems() {
        if (shiftIndex < 0) shiftIndex = 0;

        if (shiftIndex > 0) this.setSlot(this.shiftUpItem);
        else this.unsetSlot(this.shiftUpItem.getIndex());

        if (shiftIndex + overflowSlots.size() < this.overflowItemList.size()) this.setSlot(this.shiftDownItem);
        else this.unsetSlot(this.shiftDownItem.getIndex());

        List<DItemStack> shiftedList = new LinkedList<>(overflowItemList.values()).subList(shiftIndex, overflowItemList.size());

        Iterator<Integer> iterator = overflowSlots.iterator();
        shiftedList.iterator().forEachRemaining(item -> {
            if(iterator.hasNext()) {
                Slot slot = Slot.builder(builder -> builder
                                .index(iterator.next())
                                .material(item.getDItem().getMaterial())
                                .amount(item.getAmount())
                                .displayName(item.getDItem().getDisplayName().append(MiniMessage.miniMessage().deserialize("<white> x" + item.getAmount())))
                                .lore(ItemLore.lore().addLine(MiniMessage.miniMessage().deserialize("<!italic><gold><bold>STASHED<gold>"))
                                        .addLines(item.getDItem().getLore().lines())
                                        .addLines(List.of(
                                                Component.empty(), item.getDItem().getRarity().getComponent(),
                                                Component.empty(),
                                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to claim"),
                                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Shift-Right Click to remove")
                                        ))
                                        .build()))
                        .clickAction((slotInfo, clickType) -> this.getStash(item, clickType))
                        .build();
                setSlot(slot);
            }
        });
        super.getSlotHolder().getRemainingSlots(overflowSlots).forEach(slotNum -> super.setSlot(Slots.EMPTY_ITEM.index(slotNum).build()));
        super.getSlotHolder().getRemainingSlots().forEach(slotNum -> super.setSlot(Slots.FILLER_ITEM.index(slotNum).build()));
    }

    public void shiftUp(Slot slotInfo, ClickType clickType) {
        if(clickType.isShiftClick()) this.shiftIndex += -36;
         else this.shiftIndex += -9;
        super.unsetSlots(overflowSlots);
        initOverflowItems();
    }

    public void shiftDown(Slot slotInfo, ClickType clickType) {
        if(clickType.isShiftClick()) this.shiftIndex += 36;
        else this.shiftIndex += 9;
        super.unsetSlots(overflowSlots);
        initOverflowItems();
    }

    private void getStash(DItemStack item, ClickType clickType) {
        Overflow overflow = super.getDPlayer().getOverflow();

        if(clickType.isShiftClick() && clickType.isRightClick()) {
            overflow.clear(item.getDItem());
            super.getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>You have <red><bold>REMOVED</red> ").append(item.getDItem().getDisplayName().asComponent()).append(MiniMessage.miniMessage().deserialize(" from your stash!")));
            super.getDPlayer().playSound(Sound.BLOCK_LAVA_POP, SoundCategory.PLAYERS, 0.5f, RandomUtil.between(0.5f, 1.0f), PlayType.PLAYER);
        } else if(clickType.isLeftClick()) {
            if(super.getDPlayer().getSpace(item.getItemStack()) <= 0) {
                super.getDPlayer().sendMessage("<red>You do not have enough space in your inventory!");
                super.getDPlayer().playSound(Sound.ENTITY_VILLAGER_NO, SoundCategory.PLAYERS, 0.5f, RandomUtil.between(0.5f, 1.5f), PlayType.PLAYER);
                return;
            }
            DItemStack dItemStack = overflow.take(item.getDItem());

            if(dItemStack == null) {
                super.getDPlayer().sendMessage("<red>There was a problem claiming this item!");
                return;
            }
            super.getDPlayer().give(dItemStack.clone());
            super.getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>You claimed <white>").append(ComponentUtil.getItem(dItemStack)).append(MiniMessage.miniMessage().deserialize("!")));
            super.getDPlayer().playSound(Sound.ENTITY_HORSE_SADDLE, SoundCategory.PLAYERS, 0.5f, RandomUtil.between(0.5f, 1.0f), PlayType.PLAYER);
        }
        super.unsetSlots(overflowSlots);
        initOverflowItems();
    }

    public void clearOverflow(Slot slotInfo, ClickType clickType) {
        if(getDPlayer().getOverflow().isEmpty()) {
            getDPlayer().sendMessage("<red>Your overflow stash is already empty!");
            getDPlayer().playSound(SoundSettings.of(Sound.ENTITY_VILLAGER_NO, 0.5f, 1, SoundCategory.PLAYERS, PlayType.PLAYER), true);
            return;
        }

        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Type CONFIRM",
                "to clear stash"
        );

        SignInput signInput = new SignInput(getDPlayer(), signText);
        signInput.init(result -> {
            Bukkit.getScheduler().runTask(getDPlayer().getPlugin(), () -> {
                if(result.equals("CONFIRM")) {
                    getDPlayer().getOverflow().clear();
                    getDPlayer().playSound(Sound.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5f, RandomUtil.between(0.5f, 1.0f), PlayType.PLAYER);
                    getDPlayer().sendMessage("<green>You have <red><bold>REMOVED</red> your overflow stash!");
                }
                super.reopenInventory();
                super.unsetSlots(overflowSlots);
                initOverflowItems();
            });
        });
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        super.getDPlayer().playSound(Sound.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, 0.5f, RandomUtil.between(0.5f, 1.0f), PlayType.PLAYER);
    }

    @Override
    public void onInteract(InventoryInteractEvent event) {
        event.setCancelled(true);
    }
}