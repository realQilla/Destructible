package net.qilla.destructible.menus.blockmodify;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.command.Sounds;
import net.qilla.destructible.menus.DestructibleMenu;
import net.qilla.destructible.menus.MenuSize;
import net.qilla.destructible.menus.input.SignInput;
import net.qilla.destructible.menus.slot.Display;
import net.qilla.destructible.menus.slot.Displays;
import net.qilla.destructible.menus.slot.Slot;
import net.qilla.destructible.menus.slot.UniqueSlot;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.ItemDrop;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.FormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModifyItemDrop extends DestructibleMenu {

    private static final MenuSize SIZE = MenuSize.FIVE;
    private static final Component TITLE = Component.text("Item Drop Modification");

    private final SetLootpool menu;
    private boolean fullMenu;
    private final ItemDrop itemDrop;
    private DItem dItem;
    private Integer minAmount;
    private Integer maxAmount;
    private Double chance;

    public ModifyItemDrop(DPlayer dPlayer, SetLootpool menu, ItemDrop itemDrop) {
        super(dPlayer, SIZE, TITLE);
        Preconditions.checkNotNull(itemDrop, "ItemDrop cannot be null");

        this.menu = menu;
        this.itemDrop = itemDrop;
        this.dItem = itemDrop.getDItem();
        this.minAmount = itemDrop.getMinAmount();
        this.maxAmount = itemDrop.getMaxAmount();
        this.chance = itemDrop.getChance() * 100;
        this.fullMenu = true;

        this.populateMenu();
        super.register(this.itemSlot());
        populateSettings();
    }

    public ModifyItemDrop(DPlayer dPlayer, SetLootpool menu) {
        super(dPlayer, SIZE, TITLE);
        this.menu = menu;
        this.itemDrop = null;
        super.register(Slot.of(22, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(Material.HOPPER_MINECART)
                        .displayName(MiniMessage.miniMessage().deserialize("<gray>Click with block"))
                        .lore(ItemLore.lore(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Right click to search for an item")
                        )))
                ))
                .action(((slot, event) -> {
                    if(event.getClick().isRightClick()) {
                        new SetDItem(getDPlayer(), this).openInventory(false);
                    }
                }))
                .clickSound(Sounds.CLICK_MENU_ITEM)
                .appearSound(Sounds.ITEM_APPEAR)
        ), 3);

        this.populateMenu();
    }

    @Override
    protected void populateMenu() {
        super.register(Slot.of(4, Display.of(builder -> builder
                .material(Material.PINK_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<light_purple>Item Drop Modification"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Create new item drops for"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>existing lootpools")
                ))))));
        super.register(Slot.of(40, builder -> builder
                .display(Displays.RETURN)
                .action((slot, event) -> {
                    returnToPreviousMenu();
                })
                .uniqueSlot(UniqueSlot.RETURN)
                .clickSound(Sounds.RETURN_MENU)
        ));
    }

    private void populateSettings() {
        List<Slot> list = new ArrayList<>(List.of(this.amountSlot(), this.chanceSlot()));
        Collections.shuffle(list);
        list.add(this.buildSlot());

        super.register(list, 1);
    }

    private Slot buildSlot() {
        return Slot.of(42, builder -> builder
                .display(Displays.CONFIRM)
                .action((slot, event) -> {
                    ClickType clickType = event.getClick();
                    if(clickType.isLeftClick()) {
                        menu.removeItemDrop(itemDrop);
                        menu.addItemDrop(new ItemDrop.Builder()
                                .dItem(dItem)
                                .minAmount(minAmount)
                                .maxAmount(Math.max(minAmount, maxAmount))
                                .chance(chance / 100)
                                .build());
                        menu.updateModular();
                        menu.returnToPreviousMenu();
                    }
                })
                .appearSound(Sounds.ITEM_APPEAR)
                .clickSound(Sounds.SUCCESS)
        );
    }

    private Slot itemSlot() {
        return Slot.of(22, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(dItem.getMaterial())
                        .displayName(MiniMessage.miniMessage().deserialize("<gold>Drop amount"))
                        .lore(ItemLore.lore()
                                .addLines(List.of(
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Name ").append(dItem.getDisplayName()),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Lore:")
                                ))
                                .addLines(dItem.getLore().lines())
                                .addLines(List.of(
                                        Component.empty(),
                                        MiniMessage.miniMessage().deserialize("<!italic><gray>Rarity ").append(dItem.getRarity().getComponent())
                                )).build()
                        )
                ))
                .action(((slot, event) -> new SetDItem(getDPlayer(), this).openInventory(true)))
                .clickSound(Sounds.CLICK_MENU_ITEM)
                .appearSound(Sounds.ITEM_APPEAR)
        );
    }

    private Slot amountSlot() {
        if(minAmount == null) this.minAmount = 1;
        if(maxAmount == null) this.maxAmount = 1;
        String string = "<!italic><gray>Drop amount: <white>" + this.minAmount + " <white>- " + this.maxAmount;

        return Slot.of(20, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(Material.BROWN_BUNDLE)
                        .displayName(MiniMessage.miniMessage().deserialize("<gold>Drop amount"))
                        .lore(ItemLore.lore(List.of(
                                MiniMessage.miniMessage().deserialize(string),
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to set a minimum amount"),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Right Click to set a maximum amount")
                        )))
                ))
                .action(((slot, event) -> {
                    ClickType clickType = event.getClick();

                    if(clickType.isLeftClick()) setMinAmount();
                    else if(clickType.isRightClick()) setMaxAmount();
                }))
                .clickSound(Sounds.CLICK_MENU_ITEM)
                .appearSound(Sounds.ITEM_APPEAR) );
    }

    private Slot chanceSlot() {
        if(chance == null) this.chance = 100.0;
        String string = (FormatUtil.decimalTruncation(this.chance, 17)) + "% (1/" + FormatUtil.numberComma((long) Math.ceil(100 / this.chance)) + ")";

        return Slot.of(24, builder -> builder
                .display(Display.of(builder2 -> builder2
                        .material(Material.ENCHANTED_BOOK)
                        .displayName(MiniMessage.miniMessage().deserialize("<aqua>Drop Chance"))
                        .lore(ItemLore.lore(List.of(
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Drop chance: <white>" + string + ")"),
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to set a drop chance")
                        )))
                ))
                .action(((slot, event) -> setChance()))
                .clickSound(Sounds.CLICK_MENU_ITEM)
                .appearSound(Sounds.ITEM_APPEAR)
        );
    }

    private void setMinAmount() {
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Minimum amount",
                "that can drop");

        SignInput signInput = new SignInput(super.getDPlayer(), signText);
        signInput.init(result -> {
            Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {

                try {
                    this.minAmount = Math.max(1, Integer.parseInt(result));
                    super.register(this.amountSlot());
                    getDPlayer().playSound(Sounds.SIGN_INPUT, true);
                } catch(NumberFormatException ignore) {
                }
                super.openInventory(false);
            });
        });
    }

    private void setMaxAmount() {
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Maximum amount",
                "that can drop");

        SignInput signInput = new SignInput(super.getDPlayer(), signText);
        signInput.init(result -> {
            Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {

                try {
                    this.maxAmount = Math.max(1, Integer.parseInt(result));
                    super.register(this.amountSlot());
                    getDPlayer().playSound(Sounds.SIGN_INPUT, true);
                } catch(NumberFormatException ignore) {
                }
                super.openInventory(false);
            });
        });
    }

    private void setChance() {
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Chance that this",
                "item can drop");

        SignInput signInput = new SignInput(super.getDPlayer(), signText);
        signInput.init(result -> {
            Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {

                try {
                    this.chance = Math.max(0, Math.min(100, Double.parseDouble(result)));
                    super.register(this.chanceSlot());
                    getDPlayer().playSound(Sounds.SIGN_INPUT, true);
                } catch(NumberFormatException ignore) {
                }
                super.openInventory(false);
            });
        });
    }

    public void setDItem(DItem dItem) {
        this.dItem = dItem;
        this.register(itemSlot());
        if(!fullMenu) {
            populateSettings();
            fullMenu = true;
        }
    }
}