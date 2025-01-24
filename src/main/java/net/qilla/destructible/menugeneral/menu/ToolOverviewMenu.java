package net.qilla.destructible.menugeneral.menu;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.Sounds;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.menugeneral.*;
import net.qilla.destructible.menugeneral.input.SignInput;
import net.qilla.destructible.menugeneral.slot.*;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.StringUtil;
import net.qilla.destructible.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ToolOverviewMenu {

    /*private static final Collection<DItem> DITEM_COLLECTION = DRegistry.ITEMS.values();

    public ToolOverviewMenu(@NotNull Destructible plugin, @NotNull DPlayer dPlayer) {
        super(plugin, dPlayer, DITEM_COLLECTION);
        super.addSocket(new Socket(0, Slots.CREATE_NEW, event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                new ToolModificationMenu(super.getPlugin(), dPlayer, null).open(true);
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
    public Socket createSocket(int index, DItem item) {

        return new Socket(index, Slot.of(builder -> builder
                .material(item.getMaterial())
                .displayName(MiniMessage.miniMessage().deserialize(item.getId()))
                .lore(ItemLore.lore()
                        .addLines(List.of(
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Name ").append(item.getDisplayName()),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Lore:")
                        ))
                        .addLines(item.getLore().lines())
                        .addLines(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Durability <white>" + item.getDurability()),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Tool Type: <white>"),
                                MiniMessage.miniMessage().deserialize("<!italic><white>" + toolList),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Rarity ").append(item.getRarity().getComponent()),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Last Update <white>" + TimeUtil.timeSince(item.getVersion(), false)),
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to get this item"),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Shift-Left Click to select an amount"),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Middle Click to make modifications")
                        ))
                        .build())
                .clickSound(Sounds.MENU_GET_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isShiftClick() && clickType.isLeftClick()) {
                List<String> signText = List.of(
                        "^^^^^^^^^^^^^^^",
                        "Amount to receive",
                        "");

                super.getItemAmount(signText, item);
                return true;
            } else if(clickType.isLeftClick()) {
                super.getItem(item);
                return true;
            }else if(clickType == ClickType.MIDDLE) {
                new ToolModificationMenu(super.getPlugin(), getDPlayer(), item).open(true);
                return true;
            } else return false;
        }, CooldownType.GET_ITEM);
    }

    private Socket clearItemsSocket() {
        return new Socket(45, Slot.of(builder -> builder
                .material(Material.FIRE_CHARGE)
                .displayName(MiniMessage.miniMessage().deserialize("<red><bold>CLEAR</bold> Custom Tools"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to clear custom tools")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                List<String> signText = List.of(
                        "^^^^^^^^^^^^^^^",
                        "Type CONFIRM",
                        "to clear"
                );

                SignInput signInput = new SignInput(super.getPlugin(), getDPlayer(), signText);
                signInput.init(result -> {
                    Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                        if(result.equals("CONFIRM")) {
                            Bukkit.getScheduler().runTaskAsynchronously(super.getPlugin(), () -> {
                                Bukkit.getScheduler().runTask(super.getPlugin(), this::refreshSockets);
                            });
                            this.refreshSockets();
                            super.getDPlayer().sendMessage("<yellow>Custom tools have been <red><bold>CLEARED</red>!");
                            super.getDPlayer().playSound(Sounds.GENERAL_SUCCESS_2, true);
                        }
                        super.open(false);
                    });
                });
                return true;
            } else return false;
        }, CooldownType.MENU_CLICK);
    }

    private Socket saveItemsSocket() {
        return new Socket(46, Slot.of(builder -> builder
                .material(Material.SLIME_BALL)
                .displayName(MiniMessage.miniMessage().deserialize("<green><bold>SAVE</bold> Custom Tools"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to save custom tool changes")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                List<String> signText = List.of(
                        "^^^^^^^^^^^^^^^",
                        "Type CONFIRM",
                        "to save"
                );

                SignInput signInput = new SignInput(super.getPlugin(), getDPlayer(), signText);
                signInput.init(result -> {
                    Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                        if(result.equals("CONFIRM")) {
                            Bukkit.getScheduler().runTaskAsynchronously(super.getPlugin(), () -> {
                            });
                            super.getDPlayer().sendMessage("<yellow>Custom tools have been <green><bold>SAVED</green>!");
                            super.getDPlayer().playSound(Sounds.GENERAL_SUCCESS, true);
                        }
                        super.open(false);
                    });
                });
                return true;
            } else return false;
        }, CooldownType.MENU_CLICK);
    }

    private Socket reloadItemsSocket() {
        return new Socket(47, Slot.of(builder -> builder
                .material(Material.SNOWBALL)
                .displayName(MiniMessage.miniMessage().deserialize("<aqua><bold>RELOAD</bold> Custom Tools"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to load the config, undoing any unsaved changes.")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                List<String> signText = List.of(
                        "^^^^^^^^^^^^^^^",
                        "Type CONFIRM",
                        "to reload"
                );

                SignInput signInput = new SignInput(super.getPlugin(), getDPlayer(), signText);
                signInput.init(result -> {
                    Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                        if(result.equals("CONFIRM")) {
                            Bukkit.getScheduler().runTaskAsynchronously(super.getPlugin(), () -> {
                                Bukkit.getScheduler().runTask(super.getPlugin(), this::refreshSockets);
                            });
                            super.getDPlayer().sendMessage("<yellow>Custom tools have been <aqua><bold>RELOADED</aqua>!");
                            super.getDPlayer().playSound(Sounds.GENERAL_SUCCESS, true);
                        }
                        super.open(false);
                    });
                });
                return true;
            } else return false;
        }, CooldownType.MENU_CLICK);
    }

    @Override
    public Socket menuSocket() {
        return new Socket(4, Slots.TOOL_OVERVIEW_MENU);
    }

    @Override
    public StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuSize.SIX)
                .title(Component.text("Custom Tool Overview"))
                .menuIndex(4)
                .returnIndex(49));
    }

    @Override
    public DynamicConfig dynamicConfig() {
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
    }*/
}