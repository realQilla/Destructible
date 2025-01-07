package net.qilla.destructible.menus;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.menus.input.SignInput;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.SlotType;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.DItemStack;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.player.Overflow;
import net.qilla.destructible.player.PlayType;
import net.qilla.destructible.util.ComponentUtil;
import net.qilla.destructible.util.RandomUtil;
import org.bukkit.*;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class OverflowMenu extends ModularMenu {
    private static final MenuSize SIZE = MenuSize.SIX;
    private static final Component TITLE = MiniMessage.miniMessage().deserialize("Overflow Stash");
    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    );

    private final Map<DItem, Integer> itemPopulation = getDPlayer().getOverflow().getOverflow();
    private final Slot nextSlot;
    private final Slot previousSlot;

    public OverflowMenu(DPlayer dPlayer) {
        super(dPlayer, SIZE, TITLE, MODULAR_SLOTS, dPlayer.getOverflow().getOverflow().size());
        super.getDPlayer().playSound(Sound.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, 0.5f, RandomUtil.between(0.5f, 1.0f), PlayType.PLAYER);

        super.register(Slot.of(4, Displays.OVERFLOW_MENU));
        super.register(Slot.of(45, removeAllDisplay, this::clearOverflow));
        super.register(Slot.of(49, Displays.RETURN, (slot, clickType) -> returnToPreviousMenu()), SlotType.RETURN);
        this.nextSlot = Slot.of(52, Displays.NEXT, (slot, clickType) -> rotateNext(slot, clickType, 9));
        this.previousSlot = Slot.of(7, Displays.PREVIOUS, (slot, clickType) -> rotatePrevious(slot, clickType, 9));

        if(itemPopulation.size() > MODULAR_SLOTS.size()) this.register(nextSlot);
        populateModular();
    }

    net.qilla.destructible.menus.Display removeAllDisplay = net.qilla.destructible.menus.Display.of(consumer -> consumer
            .material(Material.BARRIER)
            .displayName(MiniMessage.miniMessage().deserialize("<red>Remove <bold>ALL</bold>!"))
            .lore(ItemLore.lore(List.of(
                            MiniMessage.miniMessage().deserialize("<!italic><gray>Click to clear your entire"),
                            MiniMessage.miniMessage().deserialize("<!italic><gray>overflow stash")
                    ))
            )
    );

    @Override
    public void populateModular() {
        List<ItemStack> shiftedList = new LinkedList<>(itemPopulation.entrySet().stream().map(entry -> DItemStack.of(entry.getKey(), entry.getValue())).toList())
                .subList(super.getShiftIndex(), Math.min(super.getShiftIndex() + MODULAR_SLOTS.size(), itemPopulation.size()));

        Iterator<Integer> iterator = MODULAR_SLOTS.iterator();
        shiftedList.iterator().forEachRemaining(item -> {
            if(iterator.hasNext()) {
                register(createSlot(iterator.next(), item));
            }
        });

        super.getSocket().getRemaining(MODULAR_SLOTS).forEach(slotNum -> super.register(Slot.of(slotNum, Displays.EMPTY_SLOT)));
        super.getSocket().getRemaining().forEach(slotNum -> super.register(Slot.of(slotNum, Displays.FILLER)));
    }

    public Slot createSlot(int index, ItemStack itemStack) {
        net.qilla.destructible.menus.Display display = net.qilla.destructible.menus.Display.of(consumer -> consumer
                .material(Registry.MATERIAL.get(itemStack.getData(DataComponentTypes.ITEM_MODEL)))
                .amount(itemStack.getAmount())
                .displayName(itemStack.getData(DataComponentTypes.ITEM_NAME).append(MiniMessage.miniMessage().deserialize("<white> x" + itemStack.getAmount())))
                .lore(ItemLore.lore().addLine(MiniMessage.miniMessage().deserialize("<!italic><gold><bold>STASHED<gold>"))
                        .addLines(itemStack.getData(DataComponentTypes.LORE).lines())
                        .addLines(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to claim"),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Shift-Right Click to remove")
                        )).build()
                )
        );
        return Slot.of(index, display, (slot, clickType) -> claimOverflow(slot, clickType, itemStack));
    }

    private void claimOverflow(Slot slot, ClickType clickType, ItemStack itemStack) {
        Overflow overflow = super.getDPlayer().getOverflow();
        Optional<DItem> optional = DItemStack.getDItem(itemStack);

        Preconditions.checkArgument(optional.isPresent(), "Item must be a DItem");

        DItem dItem = optional.get();

        if(!overflow.contains(dItem)) {
            super.getDPlayer().sendMessage("<red>This item is no longer in your overflow stash!");
            super.getDPlayer().playSound(Sound.ENTITY_VILLAGER_NO, SoundCategory.PLAYERS, 0.5f, RandomUtil.between(0.5f, 1.5f), PlayType.PLAYER);
            return;
        }

        if(clickType.isShiftClick() && clickType.isRightClick()) {
            overflow.remove(dItem);
            super.getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>You have <red><bold>REMOVED</red> ").append(dItem.getDisplayName().asComponent()).append(MiniMessage.miniMessage().deserialize(" from your stash!")));
            super.getDPlayer().playSound(Sound.BLOCK_LAVA_POP, SoundCategory.PLAYERS, 0.5f, RandomUtil.between(0.5f, 1.0f), PlayType.PLAYER);
        } else if(clickType.isLeftClick()) {
            if(super.getDPlayer().getSpace(itemStack) <= 0) {
                super.getDPlayer().sendMessage("<red>You do not have enough space in your inventory!");
                super.getDPlayer().playSound(Sound.ENTITY_VILLAGER_NO, SoundCategory.PLAYERS, 0.5f, RandomUtil.between(0.5f, 1.5f), PlayType.PLAYER);
                return;
            }

            ItemStack takenItemStack = overflow.take(dItem);

            if(takenItemStack == null) {
                super.getDPlayer().sendMessage("<red>There was an error claiming this item!");
                super.getDPlayer().playSound(Sound.ENTITY_VILLAGER_NO, SoundCategory.PLAYERS, 0.5f, RandomUtil.between(0.5f, 1.5f), PlayType.PLAYER);
                return;
            }

            super.getDPlayer().give(takenItemStack);
            super.getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>You claimed ").append(ComponentUtil.getItem(takenItemStack)).append(MiniMessage.miniMessage().deserialize("!")));
            super.getDPlayer().playSound(Sound.ENTITY_HORSE_SADDLE, SoundCategory.PLAYERS, 0.5f, RandomUtil.between(0.5f, 1.0f), PlayType.PLAYER);
        }
        this.refresh();
    }

    public void clearOverflow(Slot slot, ClickType clickType) {
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
                this.refresh();
            });
        });
    }

    public void refresh() {
        MODULAR_SLOTS.forEach(super::unregister);
        int shiftIndex = super.getShiftIndex();

        if(shiftIndex > 0) this.register(this.previousSlot);
        else super.unregister(this.previousSlot.getIndex());
        if(shiftIndex + MODULAR_SLOTS.size() < itemPopulation.size()) this.register(this.nextSlot);
        else super.unregister(this.nextSlot.getIndex());

        populateModular();
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
    }

    @Override
    public void onInteract(InventoryInteractEvent event) {
    }
}