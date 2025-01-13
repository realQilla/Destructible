package net.qilla.destructible.menus;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.command.Sounds;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.menus.input.SignInput;
import net.qilla.destructible.menus.slot.Display;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.menus.slot.UniqueSlot;
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

public class ItemToolMenu extends ModularMenu<DTool> {

    public ItemToolMenu(DPlayer dPlayer) {
        super(dPlayer, Registries.getDestructibleItem(DTool.class));

        super.populateModular();
    }

    public Slot createSocket(int index, DTool dTool) {
        return Slot.of(index, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(dTool.getMaterial())
                        .displayName(MiniMessage.miniMessage().deserialize(dTool.getId()))
                        .lore(ItemLore.lore()
                                .addLines(List.of(
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Name ").append(dTool.getDisplayName()),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Lore:")
                                ))
                                .addLines(dTool.getLore().lines())
                                .addLines(List.of(
                                        Component.empty(),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Tool Strength <white>" + FormatUtil.romanNumeral(dTool.getToolStrength())),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Breaking Efficiency <white>" + dTool.getBreakingEfficiency()),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Tool Durability <white>" + dTool.getToolDurability()),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Tool Type: <white>"),
                                        MiniMessage.miniMessage().deserialize("<!italic><white>" + FormatUtil.toNameList(dTool.getToolType())),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Rarity ").append(dTool.getRarity().getComponent()),
                                        Component.empty(),
                                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to get this item"),
                                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Shift-Left Click to select an amount")
                                ))
                                .build())
                ))
                .action((slot, event) -> getItem(slot, event, dTool))
                .clickSound(Sounds.CLICK_MENU_ITEM)
        );
    }

    public void getItem(Slot slot, InventoryClickEvent event, DTool dTool) {
        if(getDPlayer().getCooldown().has(CooldownType.GET_ITEM)) return;
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
                    super.openMenu(false);
                });
            });
        } else if(clickType.isLeftClick()) {
            getDPlayer().give(DItemStack.of(dTool, 1));
            getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>You received ").append(ComponentUtil.getItem(dTool, 1)).append(MiniMessage.miniMessage().deserialize("!")));
        }
    }

    @Override
    public List<Integer> modularIndexes() {
        return List.of(
                9, 10, 11, 12, 13, 14, 15, 16, 17,
                18, 19, 20, 21, 22, 23, 24, 25, 26,
                27, 28, 29, 30, 31, 32, 33, 34, 35,
                36, 37, 38, 39, 40, 41, 42, 43, 44
        );
    }

    @Override
    public int returnIndex() {
        return 49;
    }

    @Override
    public int nextIndex() {
        return 52;
    }

    @Override
    public int previousIndex() {
        return 7;
    }

    @Override
    public int rotateAmount() {
        return 9;
    }

    @Override
    public Component tile() {
        return MiniMessage.miniMessage().deserialize("Destructible Tools");
    }

    @Override
    public MenuSize menuSize() {
        return MenuSize.SIX;
    }

    @Override
    public Slot menuSlot() {
        return Slot.of(4, Displays.TOOL_MENU);
    }
}