package net.qilla.destructible.menus;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.command.Sounds;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.menus.input.SignInput;
import net.qilla.destructible.menus.slot.Display;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.DItemStack;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.ComponentUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import java.util.*;

public class ItemMenu extends ModularMenu<DItem> {
    private static final MenuSize SIZE = MenuSize.SIX;
    private static final Component TITLE = MiniMessage.miniMessage().deserialize("Destructible Items");
    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    );

    public ItemMenu(DPlayer dPlayer) {
        super(dPlayer, SIZE, TITLE, MODULAR_SLOTS,
                Registries.getDestructibleItem(DItem.class));
        this.populateMenu();
        super.populateModular();
    }

    @Override
    protected void populateMenu() {
        super.register(Slot.of(4, Displays.ITEM_MENU));
        super.register(Slot.of(47, builder -> builder
                .display(Display.of(consumer -> consumer
                        .material(Material.IRON_PICKAXE)
                        .displayName(MiniMessage.miniMessage().deserialize("<gold>Destructible Tools"))
                        .lore(ItemLore.lore(List.of(MiniMessage.miniMessage().deserialize("<!italic><gray>Left Click to view destructible tools"))))
                ))
                .action((slot, event) -> {
                    if(event.getClick().isLeftClick()) {
                        new ItemToolMenu(super.getDPlayer()).openInventory(true);
                    }
                })
                .clickSound(Sounds.CLICK_MENU_ITEM)
        ));
        super.register(Slot.of(46, Display.of(consumer -> consumer
                .material(Material.NETHERITE_AXE)
                .displayName(MiniMessage.miniMessage().deserialize("<gold>Destructible Weapons"))
                .lore(ItemLore.lore(List.of(MiniMessage.miniMessage().deserialize("<!italic><gray>Left Click to view destructible weapons"))))
        )));
        super.register(Slot.of(49, builder -> builder
                .display(Displays.RETURN)
                .action((slot, event) -> {
                    if(event.getClick().isLeftClick()) {
                        returnToPreviousMenu();
                    }
                })
                .clickSound(Sounds.RETURN_MENU)
        ));
    }

    public Slot createSlot(int index, DItem dItem) {
        return Slot.of(index, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(dItem.getMaterial())
                        .displayName(MiniMessage.miniMessage().deserialize(dItem.getId()))
                        .lore(ItemLore.lore()
                                .addLines(List.of(
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Name ").append(dItem.getDisplayName()),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Lore:")
                                ))
                                .addLines(dItem.getLore().lines())
                                .addLines(List.of(
                                        Component.empty(),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Rarity ").append(dItem.getRarity().getComponent()),
                                        Component.empty(),
                                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to get this item"),
                                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Shift-Left Click to select an amount")
                                )).build()
                        )
                ))
                .action((slot, event) -> getItem(slot, event, dItem))
                .clickSound(Sounds.GET_ITEM)
        );
    }

    private void getItem(Slot slot, InventoryClickEvent event, DItem dItem) {
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

                        getDPlayer().give(DItemStack.of(dItem, value));
                        getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>You received ").append(ComponentUtil.getItem(dItem, value)).append(MiniMessage.miniMessage().deserialize("!")));
                        getDPlayer().playSound(Sounds.SIGN_INPUT, true);
                    } catch(NumberFormatException ignored) {
                    }
                    super.openInventory(false);
                });
            });
        } else if(clickType.isLeftClick()) {
            getDPlayer().give(DItemStack.of(dItem, 1));
            getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>You received ").append(ComponentUtil.getItem(dItem, 1)).append(MiniMessage.miniMessage().deserialize("!")));}
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
                .action((slot, event) -> super.rotatePrevious(slot, event, 9)));
    }
}