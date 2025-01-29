package net.qilla.destructible.menugeneral.menu;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.DSounds;
import net.qilla.destructible.menugeneral.DSlots;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.ItemStackFactory;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.player.DPlayerData;
import net.qilla.destructible.player.Overflow;
import net.qilla.destructible.player.OverflowEntry;
import net.qilla.destructible.util.ComponentUtil;
import net.qilla.destructible.util.DUtil;
import net.qilla.qlibrary.menu.DynamicConfig;
import net.qilla.qlibrary.menu.MenuScale;
import net.qilla.qlibrary.menu.QDynamicMenu;
import net.qilla.qlibrary.menu.StaticConfig;
import net.qilla.qlibrary.menu.input.SignInput;
import net.qilla.qlibrary.menu.socket.QSlot;
import net.qilla.qlibrary.menu.socket.QSocket;
import net.qilla.qlibrary.menu.socket.Socket;
import net.qilla.qlibrary.player.CooldownType;
import net.qilla.qlibrary.util.sound.QSounds;
import net.qilla.qlibrary.util.sound.QSounds.Menu;
import org.bukkit.*;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OverflowMenu extends QDynamicMenu<Map.Entry<String, OverflowEntry>> {

    private final Overflow overflow;
    private final DPlayer player;

    public OverflowMenu(@NotNull Plugin plugin, @NotNull DPlayerData playerData) {
        super(plugin, playerData, playerData.getOverflow().getOverflow());

        this.overflow = playerData.getOverflow();
        this.player = playerData.getPlayer();

        super.addSocket(new QSocket(53, QSlot.of(builder -> builder
                .material(Material.BARRIER)
                .displayName(MiniMessage.miniMessage().deserialize("<red>Clear Stash"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to remove item in your stash")
                )))
        ), event -> {
            this.clearOverflow(event);
            return true;
        }, CooldownType.MENU_CLICK));
        super.populateModular();
        super.finalizeMenu();
    }

    @Override
    public @Nullable Socket createSocket(int index, Map.Entry<String, OverflowEntry> item) {
        DItem dItem = DUtil.getDItem(item.getValue().getData().getItemID());
        int amount = item.getValue().getAmount();

        return new QSocket(index, QSlot.of(builder -> builder
                .material(dItem.getMaterial())
                .amount(amount)
                .displayName(dItem.getDisplayName().append(MiniMessage.miniMessage().deserialize("<white> x" + amount)))
                .lore(ItemLore.lore().addLine(MiniMessage.miniMessage().deserialize("<!italic><gold><bold>STASHED<gold>"))
                        .addLines(ComponentUtil.getLore(dItem).lines())
                        .addLines(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to claim item"),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>② <key:key.sneak> + <key:key.mouse.right></gold> to remove item")
                        )).build()
                )
        ), event -> this.claimOverflow(event, dItem, amount), CooldownType.MENU_CLICK);
    }

    private boolean claimOverflow(InventoryClickEvent event, DItem dItem, int amount) {
        ClickType clickType = event.getClick();

        if(!overflow.contains(dItem.getId())) {
            super.getPlayer().sendMessage("<red>This item is no longer in your stash!");
            super.getPlayer().playSound(QSounds.General.GENERAL_ERROR, true);
            return false;
        }

        if(clickType.isShiftClick() && clickType.isRightClick()) {
            overflow.remove(dItem.getId());
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have <red><bold>REMOVED</red> ").append(dItem.getDisplayName().asComponent()).append(MiniMessage.miniMessage().deserialize(" from your stash!")));
            player.playSound(QSounds.Menu.ITEM_DELETE, true);
        } else if(clickType.isLeftClick()) {
            if(player.getSpace(ItemStackFactory.of(dItem, amount)) <= 0) {
                player.sendMessage("<red>You do not have enough space in your inventory!");
                player.playSound(QSounds.General.GENERAL_ERROR, true);
                return false;
            }

            Optional<ItemStack> optional = overflow.take(dItem.getId());

            if(optional.isEmpty()) {
                player.sendMessage("<red>There was an error claiming this item!");
                player.playSound(QSounds.General.GENERAL_ERROR, true);
                return false;
            }
            ItemStack takenItemStack = optional.get();

            player.give(takenItemStack.clone());
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>You claimed ").append(ComponentUtil.getItemAmountAndType(takenItemStack)).append(MiniMessage.miniMessage().deserialize("!")));
            player.playSound(QSounds.Menu.MENU_CLAIM_ITEM, true);
        }
        super.refreshSockets();
        return true;
    }

    public boolean clearOverflow(InventoryClickEvent event) {
        ClickType clickType = event.getClick();

        if(clickType.isLeftClick()) {
            if(overflow.isEmpty()) {
                player.sendMessage("<red>Your overflow stash is already empty!");
                player.playSound(QSounds.General.GENERAL_ERROR, true);
                return false;
            }

            List<String> signText = List.of(
                    "^^^^^^^^^^^^^^^",
                    "Type CONFIRM",
                    "to clear stash"
            );

            SignInput signInput = new SignInput(super.getPlugin(), super.getPlayerData(), signText);
            signInput.init(result -> {
                Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                    if(result.equals("CONFIRM")) {
                        overflow.clear();
                        player.playSound(QSounds.Menu.RESET, true);
                        player.sendMessage("<green>You have <red><bold>REMOVED</red> your overflow stash!");

                        super.setShiftIndex(0);
                        this.refreshSockets();
                    }
                    super.open(false);
                });
            });
            return true;
        } else return false;
    }

    @Override
    public @NotNull Socket menuSocket() {
        return new QSocket(4, DSlots.OVERFLOW_MENU);
    }

    @Override
    public @NotNull StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuScale.SIX)
                .title(Component.text("Overflow"))
                .menuIndex(4)
                .returnIndex(49));
    }

    @Override
    public @NotNull DynamicConfig dynamicConfig() {
        return DynamicConfig.of(builder -> builder
                .dynamicSlots(List.of(
                        9, 10, 11, 12, 13, 14, 15, 16, 17,
                        18, 19, 20, 21, 22, 23, 24, 25, 26,
                        27, 28, 29, 30, 31, 32, 33, 34, 35,
                        36, 37, 38, 39, 40, 41, 42, 43, 44
                ))
                .nextIndex(52)
                .previousIndex(7)
                .shiftAmount(9)
        );
    }
}