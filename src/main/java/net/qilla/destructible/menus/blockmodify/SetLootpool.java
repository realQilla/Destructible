package net.qilla.destructible.menus.blockmodify;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.command.Sounds;
import net.qilla.destructible.menus.MenuSize;
import net.qilla.destructible.menus.ModularMenu;
import net.qilla.destructible.menus.slot.Display;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.UniqueSlot;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.ItemDrop;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.FormatUtil;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class SetLootpool extends ModularMenu<ItemDrop> {

    private static final MenuSize SIZE = MenuSize.SIX;
    private static final Component TITLE = Component.text("Destructible Lootpool");

    private static final List<Integer> MODULAR_SLOTS = List.of(
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35
    );

    private final BlockMenuModify menu;

    public SetLootpool(@NotNull DPlayer dPlayer, @NotNull BlockMenuModify menu) {
        super(dPlayer, SIZE, TITLE, MODULAR_SLOTS,
                menu.getLootpool());
        Preconditions.checkNotNull(menu, "Menu cannot be null");
        this.menu = menu;

        this.populateMenu();
        super.populateModular();
    }

    @Override
    protected void populateMenu() {
        super.register(Slot.of(4, Display.of(builder -> builder
                .material(Material.PINK_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<light_purple>Lootpool"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Make lootpool modifications to"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>the selected destructible block")
                )))
        )));
        super.register(Slot.of(49, builder -> builder
                .display(Displays.RETURN)
                .action((slot, event) -> {
                    returnToPreviousMenu();
                })
                .uniqueSlot(UniqueSlot.RETURN)
                .clickSound(Sounds.RETURN_MENU)
        ));
        super.register(Slot.of(47, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(Material.LIME_BUNDLE)
                        .displayName(MiniMessage.miniMessage().deserialize("<green>Add new item drop"))
                ))
                .action((slot, event) -> {
                    new ModifyItemDrop(getDPlayer(), this).openInventory(true);
                })
                .clickSound(Sounds.CLICK_MENU_ITEM)
        ));
        populateModular();
    }

    public Slot createSlot(int index, ItemDrop itemDrop) {
        DItem dItem = itemDrop.getDItem();
        return Slot.of(index, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(dItem.getMaterial())
                        .displayName(Component.text(dItem.getId()))
                        .lore(ItemLore.lore(List.of(
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Amount <white>" + itemDrop.getMinAmount() + " - " + itemDrop.getMaxAmount()),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Drop Chance <white>" +
                                        FormatUtil.decimalTruncation(itemDrop.getChance() * 100, 17) + "% (1/" + FormatUtil.numberComma((long) Math.ceil(1 / itemDrop.getChance())) + ")"),
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left click to modify"),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Right click to remove"))
                        ))
                ))
                .action((slot, event) -> {
                    ClickType clickType = event.getClick();
                    if(clickType.isLeftClick()) {
                        new ModifyItemDrop(getDPlayer(), this, itemDrop).openInventory(true);
                    } else if(clickType.isRightClick()) {
                        menu.getLootpool().remove(itemDrop);
                    }
                })
                .clickSound(Sounds.CLICK_MENU_ITEM)
        );
    }

    @Override
    protected Slot getNextSlot() {
        return Slot.of(52, builder -> builder
                .display(Displays.NEXT)
                .action((slot, event) -> rotateNext(slot, event, 9)));
    }

    @Override
    protected Slot getPreviousSlot() {
        return Slot.of(7, builder -> builder
                .display(Displays.PREVIOUS)
                .action((slot, event) -> rotatePrevious(slot, event, 9)));
    }

    public void addItemDrop(ItemDrop itemDrop) {
        menu.getLootpool().add(itemDrop);
        this.updateModular();
    }

    public void removeItemDrop(ItemDrop itemDrop) {
        super.getItemPopulation().remove(itemDrop);
        this.updateModular();
    }
}