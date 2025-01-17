package net.qilla.destructible.menugeneral.menu;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Sounds;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.menugeneral.*;
import net.qilla.destructible.menugeneral.input.SignInput;
import net.qilla.destructible.menugeneral.slot.Slot;
import net.qilla.destructible.menugeneral.slot.Slots;
import net.qilla.destructible.menugeneral.slot.Socket;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.DItemStack;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.ComponentUtil;
import net.qilla.destructible.util.DestructibleUtil;
import net.qilla.destructible.util.FormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ItemOverviewMenu extends SearchMenu<DItem> {

    List<Material> materials = Registry.MATERIAL.stream().filter(material -> material.isItem() && !material.isBlock()).toList();

    public ItemOverviewMenu(DPlayer dPlayer) {
        super(dPlayer, Registries.getDestructibleItem(DItem.class));

        super.addSocket(new Socket(1, Slot.of(builder -> builder
                .material(Material.IRON_PICKAXE)
                .displayName(MiniMessage.miniMessage().deserialize("<gold>Destructible Tools"))
                .lore(ItemLore.lore(List.of(MiniMessage.miniMessage().deserialize("<!italic><gray>Left Click to view destructible tools"))))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        ), event -> {
            if(event.getClick().isLeftClick()) {
                new ToolOverviewMenu(this.getDPlayer()).open(true);
                return true;
            } else return false;
        }));
        super.addSocket(new Socket(2, Slot.of(builder -> builder
                .material(Material.NETHERITE_AXE)
                .displayName(MiniMessage.miniMessage().deserialize("<gold>Destructible Weapons"))
                .lore(ItemLore.lore(List.of(MiniMessage.miniMessage().deserialize("<!italic><gray>Left Click to view destructible weapons"))))
                .clickSound(Sounds.MENU_CLICK_ITEM)
        )));
        super.addSocket(new Socket(6, Slots.CREATE_NEW, event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                new ItemModificationMenu(super.getDPlayer(), null).open(true);
                return true;
            } else return false;
        }));
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
                        .addLines(DestructibleUtil.getLore(item).lines())
                        .addLines(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to get this item"),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Shift-Left Click to select an amount"),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Middle Click to make modifications")
                        )).build()
                )
                .clickSound(Sounds.MENU_GET_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                return this.getItem(event, item);
            } else if(clickType == ClickType.MIDDLE) {
                new ItemModificationMenu(getDPlayer(), item).open(true);
                return true;
            } else return false;
        });
    }

    private boolean getItem(InventoryClickEvent event, DItem dItem) {
        if(getDPlayer().getCooldown().has(CooldownType.GET_ITEM)) return false;
        getDPlayer().getCooldown().set(CooldownType.GET_ITEM);

        ClickType clickType = event.getClick();

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

                        getDPlayer().give(DItemStack.of(dItem, value));
                        getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>You received ").append(ComponentUtil.getItem(dItem, value)).append(MiniMessage.miniMessage().deserialize("!")));
                        getDPlayer().playSound(Sounds.SIGN_INPUT, true);
                    } catch(NumberFormatException ignored) {
                    }
                    super.open(false);
                });
            });
            return true;
        } else if(clickType.isLeftClick()) {
            getDPlayer().give(DItemStack.of(dItem, 1));
            getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>You received ").append(ComponentUtil.getItem(dItem, 1)).append(MiniMessage.miniMessage().deserialize("!")));
            return true;
        } else return false;
    }

    @Override
    public Socket menuSocket() {
        return new Socket(4, Slots.ITEM_MENU);
    }

    @Override
    public StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuSize.SIX)
                .title(Component.text("Item Overview"))
                .menuIndex(4)
                .returnIndex(49));
    }

    @Override
    public DynamicConfig dynamicConfig() {
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

    @Override
    public String getString(DItem item) {
        return item.getId();
    }

    @Override
    public SearchConfig searchConfig() {
        return SearchConfig.of(builder -> builder
                .searchIndex(47)
                .resetSearchIndex(46)
        );
    }
}