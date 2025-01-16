package net.qilla.destructible.menugeneral.menu;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Sounds;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.menugeneral.*;
import net.qilla.destructible.menugeneral.input.SignInput;
import net.qilla.destructible.menugeneral.slot.*;
import net.qilla.destructible.mining.item.DItemStack;
import net.qilla.destructible.mining.item.DTool;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.FormatUtil;
import net.qilla.destructible.util.ComponentUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import java.util.*;

public class ToolOverviewMenu extends DynamicMenu<DTool> {

    public ToolOverviewMenu(DPlayer dPlayer) {
        super(dPlayer, Registries.getDestructibleItem(DTool.class));
        super.populateModular();
    }

    @Override
    public Socket createSocket(int index, DTool item) {
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
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Tool Strength <white>" + FormatUtil.romanNumeral(item.getToolStrength())),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Breaking Efficiency <white>" + item.getBreakingEfficiency()),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Tool Durability <white>" + item.getToolDurability()),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Tool Type: <white>"),
                                MiniMessage.miniMessage().deserialize("<!italic><white>" + FormatUtil.toNameList(item.getToolType())),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Rarity ").append(item.getRarity().getComponent()),
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to get this item"),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Shift-Left Click to select an amount")
                        ))
                        .build())
                .clickSound(Sounds.MENU_GET_ITEM)
        ), event -> this.getItem(event, item));
    }

    private boolean getItem(InventoryClickEvent event, DTool dTool) {
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

                        getDPlayer().give(DItemStack.of(dTool, value));
                        getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>You received ").append(ComponentUtil.getItem(dTool, value)).append(MiniMessage.miniMessage().deserialize("!")));
                    } catch(NumberFormatException ignored) {
                    }
                    super.open(false);
                });
            });
            return true;
        } else if(clickType.isLeftClick()) {
            getDPlayer().give(DItemStack.of(dTool, 1));
            getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>You received ").append(ComponentUtil.getItem(dTool, 1)).append(MiniMessage.miniMessage().deserialize("!")));
            return true;
        } else return false;
    }

    @Override
    public Socket menuSocket() {
        return new Socket(4, Slots.TOOL_MENU);
    }

    @Override
    public StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuSize.SIX)
                .title(Component.text("Tool Overview"))
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
    }
}