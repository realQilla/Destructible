package net.qilla.destructible.menus;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.menus.input.SignInput;
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

public class OverflowMenu extends ModularMenu {
    private static final GUISize SIZE = GUISize.SIX;
    private static final Component TITLE = MiniMessage.miniMessage().deserialize("Overflow Stash");
    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    );

    public OverflowMenu(DPlayer dPlayer) {
        super(dPlayer, SIZE, TITLE, dPlayer.getOverflow().getItems(), MODULAR_SLOTS);
        super.getDPlayer().playSound(Sound.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, 0.5f, RandomUtil.between(0.5f, 1.0f), PlayType.PLAYER);

        populateMenu();
    }

    private final Slot menuItem = Slot.builder(slot -> slot
            .index(4)
            .material(Material.BROWN_BUNDLE)
            .displayName(MiniMessage.miniMessage().deserialize("<gold>Overflowing Items"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Left Click to claim any items that were not "),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>able to fit into your inventory"))))
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

    private final Slot removeAllItem = Slot.builder(slot -> slot
            .index(45)
            .material(Material.BARRIER)
            .displayName(MiniMessage.miniMessage().deserialize("<red>Remove <bold>ALL</bold>!"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Click to clear your entire"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>overflow stash")))
            )
            .clickAction(this::clearOverflow)
    ).build();

    @Override
    public void populateMenu() {
        setSlot(menuItem);
        setSlot(removeAllItem);
        setSlot(returnItem);

        if(getShiftIndex() > 0) this.setSlot(this.shiftPreviousItem);
        else this.unsetSlot(this.shiftPreviousItem.getIndex());
        if(getShiftIndex() + getModularSlots().size() < getItemPopulation().size()) this.setSlot(this.shiftNextItem);
        else this.unsetSlot(this.shiftNextItem.getIndex());
    }

    @Override
    public Slot createSlot(int index, DItemStack item) {
        return Slot.builder(builder -> builder
                .index(index)
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
                        )).build()
                )
                .clickAction((slotInfo, clickType) -> claimOverflow(item, clickType))
        ).build();
    }

    private void claimOverflow(DItemStack item, ClickType clickType) {
        Overflow overflow = super.getDPlayer().getOverflow();

        if(!overflow.contains(item.getDItem())) {
            super.getDPlayer().sendMessage("<red>This item is no longer in your overflow stash!");
            super.getDPlayer().playSound(Sound.ENTITY_VILLAGER_NO, SoundCategory.PLAYERS, 0.5f, RandomUtil.between(0.5f, 1.5f), PlayType.PLAYER);
            return;
        }

        if(clickType.isShiftClick() && clickType.isRightClick()) {
            overflow.remove(item.getDItem());
            super.getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>You have <red><bold>REMOVED</red> ").append(item.getDItem().getDisplayName().asComponent()).append(MiniMessage.miniMessage().deserialize(" from your stash!")));
            super.getDPlayer().playSound(Sound.BLOCK_LAVA_POP, SoundCategory.PLAYERS, 0.5f, RandomUtil.between(0.5f, 1.0f), PlayType.PLAYER);
        } else if(clickType.isLeftClick()) {
            if(super.getDPlayer().getSpace(item.getItemStack()) <= 0) {
                super.getDPlayer().sendMessage("<red>You do not have enough space in your inventory!");
                super.getDPlayer().playSound(Sound.ENTITY_VILLAGER_NO, SoundCategory.PLAYERS, 0.5f, RandomUtil.between(0.5f, 1.5f), PlayType.PLAYER);
                return;
            }

            DItemStack dItemStack = overflow.take(item.getDItem());

            super.getDPlayer().give(dItemStack.clone());
            super.getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>You claimed <white>").append(ComponentUtil.getItem(dItemStack)).append(MiniMessage.miniMessage().deserialize("!")));
            super.getDPlayer().playSound(Sound.ENTITY_HORSE_SADDLE, SoundCategory.PLAYERS, 0.5f, RandomUtil.between(0.5f, 1.0f), PlayType.PLAYER);
        }
        super.refresh();
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
                super.refresh();
            });
        });
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
    }

    @Override
    public void onInteract(InventoryInteractEvent event) {
    }
}