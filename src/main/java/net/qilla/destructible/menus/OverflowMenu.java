package net.qilla.destructible.menus;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.command.Sounds;
import net.qilla.destructible.menus.input.SignInput;
import net.qilla.destructible.menus.slot.Display;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.UniqueSlot;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.DItemStack;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.player.Overflow;
import net.qilla.destructible.util.ComponentUtil;
import org.bukkit.*;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class OverflowMenu extends ModularMenu<Map.Entry<DItem, Integer>> {
    private static final MenuSize SIZE = MenuSize.SIX;
    private static final Component TITLE = MiniMessage.miniMessage().deserialize("Overflow Stash");
    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    );

    public OverflowMenu(DPlayer dPlayer) {
        super(dPlayer, SIZE, TITLE, MODULAR_SLOTS,
                dPlayer.getOverflow().getOverflow());

        this.populateMenu();
        super.populateModular();
    }

    @Override
    protected void populateMenu() {
        super.register(Slot.of(4, Displays.OVERFLOW_MENU));
        super.register(Slot.of(45, builder -> builder
                .display(Display.of(consumer -> consumer
                        .material(Material.BARRIER)
                        .displayName(MiniMessage.miniMessage().deserialize("<red>Remove <bold>ALL</bold>!"))
                        .lore(ItemLore.lore(List.of(
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Left Click to clear your entire"),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>overflow stash")
                                ))
                        )
                ))
                .action(this::clearOverflow)
        ));
        super.register(Slot.of(49, builder -> builder
                .display(Displays.RETURN)
                .action((slot, event) -> {
                    if(event.getClick().isLeftClick()) {
                        returnToPreviousMenu();
                    }
                })
                .uniqueSlot(UniqueSlot.RETURN)
                .clickSound(Sounds.RETURN_MENU)
        ));
    }

    @Override
    protected Slot createSlot(int index, Map.Entry<DItem, Integer> item) {
        return Slot.of(index, builder -> builder
                .display(Display.of(consumer -> consumer
                        .material(item.getKey().getMaterial())
                        .amount(item.getValue())
                        .displayName(item.getKey().getDisplayName().append(MiniMessage.miniMessage().deserialize("<white> x" + item.getValue())))
                        .lore(ItemLore.lore().addLine(MiniMessage.miniMessage().deserialize("<!italic><gold><bold>STASHED<gold>"))
                                .addLines(item.getKey().getLore().lines())
                                .addLines(List.of(
                                        Component.empty(),
                                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to claim"),
                                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Shift-Right Click to remove")
                                )).build()
                        )
                ))
                .action((slot, event) -> claimOverflow(slot, event, item))
                .clickSound(Sounds.GET_ITEM)
        );
    }

    private void claimOverflow(Slot slot, InventoryClickEvent event, Map.Entry<DItem, Integer> item) {
        ClickType clickType = event.getClick();
        Overflow overflow = super.getDPlayer().getOverflow();
        DItem dItem = item.getKey();

        if(!overflow.contains(dItem)) {
            super.getDPlayer().sendMessage("<red>This item is no longer in your overflow stash!");
            getDPlayer().playSound(Sounds.ERROR, true);
            return;
        }

        if(clickType.isShiftClick() && clickType.isRightClick()) {
            overflow.remove(dItem);
            super.getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>You have <red><bold>REMOVED</red> ").append(dItem.getDisplayName().asComponent()).append(MiniMessage.miniMessage().deserialize(" from your stash!")));
            getDPlayer().playSound(Sounds.ITEM_DELETE, true);
        } else if(clickType.isLeftClick()) {
            if(super.getDPlayer().getSpace(DItemStack.of(dItem, item.getValue())) <= 0) {
                super.getDPlayer().sendMessage("<red>You do not have enough space in your inventory!");
                getDPlayer().playSound(Sounds.ERROR, true);
                return;
            }

            ItemStack takenItemStack = overflow.take(dItem);

            if(takenItemStack == null) {
                super.getDPlayer().sendMessage("<red>There was an error claiming this item!");
                getDPlayer().playSound(Sounds.ERROR, true);
                return;
            }

            super.getDPlayer().give(takenItemStack);
            super.getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>You claimed ").append(ComponentUtil.getItem(takenItemStack)).append(MiniMessage.miniMessage().deserialize("!")));
            getDPlayer().playSound(Sounds.CLAIM_ITEM, true);
        }
        super.updateModular();
    }

    public void clearOverflow(Slot slot, InventoryClickEvent event) {
        ClickType clickType = event.getClick();

        if(clickType.isLeftClick()) {
            if(getDPlayer().getOverflow().isEmpty()) {
                getDPlayer().sendMessage("<red>Your overflow stash is already empty!");
                getDPlayer().playSound(Sounds.ERROR, true);
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
                        getDPlayer().playSound(Sounds.RESET, true);
                        getDPlayer().sendMessage("<green>You have <red><bold>REMOVED</red> your overflow stash!");
                    }
                    super.resetIndex();
                    super.openInventory(false);
                    this.updateModular();
                });
            });
        }
    }

    @Override
    protected Slot getNextSlot() {
        return Slot.of(52, builder -> builder
                .display(Displays.NEXT)
                .action((slot, event) -> super.rotateNext(slot, event, 9)));
    }

    @Override
    protected Slot getPreviousSlot() {
        return Slot.of(7, builder -> builder
                .display(Displays.PREVIOUS)
                .action((slot, clickType) -> super.rotatePrevious(slot, clickType, 9)));
    }
}