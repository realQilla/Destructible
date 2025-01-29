package net.qilla.destructible.menugeneral.menu;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.files.CustomItemsFile;
import net.qilla.destructible.menugeneral.DSlots;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.DItemFactory;
import net.qilla.destructible.player.DPlayerData;
import net.qilla.destructible.util.ComponentUtil;
import net.qilla.qlibrary.data.PlayerData;
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
import net.qilla.qlibrary.util.tools.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

public class ItemOverviewMenu extends QDynamicMenu<DItem> {

    private static final Collection<DItem> DITEM_COLLECTION = DRegistry.ITEMS.values();

    public ItemOverviewMenu(@NotNull Destructible plugin, @NotNull PlayerData<?> playerData) {
        super(plugin, playerData, DITEM_COLLECTION);

        super.addSocket(new QSocket(46, QSlot.of(builder -> builder
                .material(Material.IRON_INGOT)
                .displayName(MiniMessage.miniMessage().deserialize("<green>Create New Item"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to open the item creation menu")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                new ItemModificationMenu(super.getPlugin(), super.getPlayerData()).open(true);
                return true;
            } else return false;
        }, CooldownType.OPEN_MENU));
        super.addSocket(new QSocket(47, QSlot.of(builder -> builder
                .material(Material.IRON_PICKAXE)
                .displayName(MiniMessage.miniMessage().deserialize("<blue>Create New Tool"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to open the tool creation menu")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                new ToolModificationMenu(super.getPlugin(), super.getPlayerData()).open(true);
                return true;
            } else return false;
        }, CooldownType.OPEN_MENU));
        super.addSocket(this.clearItemsSocket());
        super.addSocket(this.saveItemsSocket());
        super.addSocket(this.reloadItemsSocket());
        super.populateModular();
        super.finalizeMenu();
    }

    @Override
    public @Nullable Socket createSocket(int index, DItem item) {
        return new QSocket(index, QSlot.of(builder -> builder
                .material(item.getMaterial())
                .displayName(MiniMessage.miniMessage().deserialize(item.getId()))
                .lore(ItemLore.lore()
                        .addLines(List.of(
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Name ").append(item.getDisplayName()),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Lore:")
                        ))
                        .addLines(ComponentUtil.getLore(item).lines())
                        .addLine(MiniMessage.miniMessage().deserialize("<!italic><gray>Last Update <white>" + TimeUtil.timeSince(item.getVersion(), false)))
                        .addLines(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to make modifications"),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>② <key:key.mouse.right></gold> to get this item"),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>③ <key:key.sneak> + <key:key.mouse.right></gold> to select an amount")
                        )).build()
                )
                .clickSound(QSounds.Menu.MENU_GET_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                if(!item.getStaticAttributes().isEmpty()) new ToolModificationMenu(super.getPlugin(), super.getPlayerData(), item).open(true);
                else new ItemModificationMenu(super.getPlugin(), super.getPlayerData(), item).open(true);
                return true;
            } else if(clickType.isRightClick()) {
                DPlayerData playerData = (DPlayerData) super.getPlayerData();

                if(clickType.isShiftClick()) {
                    List<String> signText = List.of(
                            "^^^^^^^^^^^^^^^",
                            "Amount to receive",
                            "");

                    SignInput signInput = new SignInput(super.getPlugin(), super.getPlayerData(), signText);
                    signInput.init(result -> {
                        Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                            try {
                                int amount = Integer.parseInt(result);

                                if(playerData.hasCooldown(CooldownType.GET_ITEM)) return;
                                playerData.getPlayer().give(DItemFactory.of(item, amount));
                                playerData.setCooldown(CooldownType.GET_ITEM);
                            } catch(NumberFormatException ignore) {
                            }

                            super.open(false);
                        });
                    });
                } else {
                    if(playerData.hasCooldown(CooldownType.GET_ITEM)) return false;
                    playerData.getPlayer().give(DItemFactory.of(item, 1));
                    playerData.setCooldown(CooldownType.GET_ITEM);
                }
                return true;
            } else return false;
        }, CooldownType.GET_ITEM);
    }

    private Socket saveItemsSocket() {
        return new QSocket(0, DSlots.SAVED_CHANGES, event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                List<String> signText = List.of(
                        "^^^^^^^^^^^^^^^",
                        "Type CONFIRM",
                        "to save"
                );

                SignInput signInput = new SignInput(super.getPlugin(), super.getPlayerData(), signText);
                signInput.init(result -> {
                    Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                        if(result.equals("CONFIRM")) {
                            Bukkit.getScheduler().runTaskAsynchronously(super.getPlugin(), () -> CustomItemsFile.getInstance().save());
                            super.getPlayer().sendMessage("<yellow>Custom items have been <green><bold>SAVED</green>!");
                            super.getPlayer().playSound(QSounds.General.GENERAL_SUCCESS, true);
                        }
                        super.open(false);
                    });
                });
                return true;
            } else return false;
        }, CooldownType.MENU_CLICK);
    }

    private Socket reloadItemsSocket() {
        return new QSocket(1, DSlots.RELOADED_CHANGES, event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                List<String> signText = List.of(
                        "^^^^^^^^^^^^^^^",
                        "Type CONFIRM",
                        "to reload"
                );

                SignInput signInput = new SignInput(super.getPlugin(), super.getPlayerData(), signText);
                signInput.init(result -> {
                    Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                        if(result.equals("CONFIRM")) {
                            Bukkit.getScheduler().runTaskAsynchronously(super.getPlugin(), () -> {
                                CustomItemsFile.getInstance().load();
                                Bukkit.getScheduler().runTask(super.getPlugin(), this::refreshSockets);
                            });
                            super.getPlayer().sendMessage("<yellow>Custom items have been <aqua><bold>RELOADED</aqua>!");
                            super.getPlayer().playSound(QSounds.General.GENERAL_SUCCESS, true);
                        }
                        super.open(false);
                    });
                });
                return true;
            } else return false;
        }, CooldownType.MENU_CLICK);
    }

    private Socket clearItemsSocket() {
        return new QSocket(2, DSlots.CLEAR_SAVED, event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                List<String> signText = List.of(
                        "^^^^^^^^^^^^^^^",
                        "Type CONFIRM",
                        "to clear"
                );

                SignInput signInput = new SignInput(super.getPlugin(), super.getPlayerData(), signText);
                signInput.init(result -> {
                    Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                        if(result.equals("CONFIRM")) {
                            Bukkit.getScheduler().runTaskAsynchronously(super.getPlugin(), () -> {
                                CustomItemsFile.getInstance().clear();
                                Bukkit.getScheduler().runTask(super.getPlugin(), this::refreshSockets);
                            });
                            super.getPlayer().sendMessage("<yellow>All custom items have been <red><bold>CLEARED</red>!");
                            super.getPlayer().playSound(QSounds.General.GENERAL_SUCCESS_2, true);
                        }
                        super.open(false);
                    });
                });
                return true;
            } else return false;
        }, CooldownType.MENU_CLICK);
    }

    @Override
    public @NotNull Socket menuSocket() {
        return new QSocket(4, DSlots.ITEM_MENU);
    }

    @Override
    public @NotNull StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuScale.SIX)
                .title(Component.text("Item Overview"))
                .menuIndex(4)
                .returnIndex(49));
    }

    @Override
    public @NotNull DynamicConfig dynamicConfig() {
        return DynamicConfig.of(
                builder -> builder
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