package net.qilla.destructible.menugeneral.menu;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.Sounds;
import net.qilla.destructible.menugeneral.StaticMenu;
import net.qilla.destructible.menugeneral.input.SignInput;
import net.qilla.destructible.menugeneral.menu.select.DItemSelectMenu;
import net.qilla.destructible.menugeneral.slot.Slots;
import net.qilla.destructible.menugeneral.slot.Slot;
import net.qilla.destructible.menugeneral.slot.Socket;
import net.qilla.destructible.menugeneral.MenuSize;
import net.qilla.destructible.menugeneral.StaticConfig;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.ItemDrop;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.NumberUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ItemDropCreationMenu extends StaticMenu {

    private boolean fullMenu;
    private final List<ItemDrop> lootpool;
    private final ItemDrop itemDrop;
    private DItem dItem;
    private Integer minAmount;
    private Integer maxAmount;
    private Double chance;

    public ItemDropCreationMenu(@NotNull Destructible plugin, @NotNull DPlayer dPlayer, @NotNull List<ItemDrop> lootpool, @Nullable ItemDrop itemDrop) {
        super(plugin, dPlayer);
        Preconditions.checkNotNull(lootpool, "List cannot be null");
        this.lootpool = lootpool;
        this.itemDrop = itemDrop;

        if(this.itemDrop != null) {
            this.fullMenu = true;
            this.dItem = itemDrop.getDItem();
            this.minAmount = itemDrop.getMinAmount();
            this.maxAmount = itemDrop.getMaxAmount();
            this.chance = itemDrop.getChance() * 100;
            this.populateSettings();
        }
        super.addSocket(this.dItemSocket(), 50);
        super.finalizeMenu();
    }

    private void populateSettings() {
        List<Socket> socketList = new ArrayList<>(List.of(
                this.amountSocket(), this.chanceSocket()
        ));
        Collections.shuffle(socketList);
        socketList.add(buildSocket());

        super.addSocket(socketList, 100);
    }

    private Socket buildSocket() {
        return new Socket(42, Slots.CONFIRM, event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            lootpool.remove(itemDrop);
            lootpool.add(new ItemDrop.Builder()
                    .dItem(dItem)
                    .minAmount(minAmount)
                    .maxAmount(maxAmount)
                    .chance(chance / 100)
                    .build());
            return super.returnMenu();
        });
    }

    private Socket dItemSocket() {
        if(dItem == null) {
            return new Socket(22, Slot.of(builder -> builder
                    .material(Material.HOPPER_MINECART)
                    .displayName(MiniMessage.miniMessage().deserialize("<light_purple>Drop item"))
                    .lore(ItemLore.lore(List.of(MiniMessage.miniMessage().deserialize("<!italic><red>Empty"),
                            Component.empty(),
                            MiniMessage.miniMessage().deserialize("<!italic><yellow>Left click to set an item")
                    )))
                    .clickSound(Sounds.MENU_CLICK_ITEM)
            ), this::setDItem);
        } else {
            return new Socket(22, Slot.of(builder -> builder
                    .material(dItem.getMaterial())
                    .displayName(MiniMessage.miniMessage().deserialize("<light_purple>Drop item"))
                    .lore(ItemLore.lore()
                            .addLines(List.of(
                                    MiniMessage.miniMessage().deserialize("<!italic><gray>Name ").append(dItem.getDisplayName()),
                                    MiniMessage.miniMessage().deserialize("<!italic><gray>Lore:")
                            ))
                            .addLines(dItem.getLore().lines())
                            .addLines(List.of(
                                    Component.empty(),
                                    MiniMessage.miniMessage().deserialize("<!italic><yellow>Left click to set an item")
                            )).build())
                    .clickSound(Sounds.MENU_CLICK_ITEM)
                    .appearSound(Sounds.MENU_ITEM_APPEAR)
            ), this::setDItem);
        }
    }

    private boolean setDItem(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(clickType.isLeftClick()) {
            CompletableFuture<DItem> future = new CompletableFuture<>();
            new DItemSelectMenu(super.getPlugin(), super.getDPlayer(), future).open(true);
            future.thenAccept(dItem -> this.dItem = dItem);
            return true;
        } else return false;
    }

    private Socket amountSocket() {
        if(minAmount == null) this.minAmount = 1;
        if(maxAmount == null) this.maxAmount = 1;
        return new Socket(20, Slot.of(builder -> builder
                .material(Material.BROWN_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<gold>Drop amount"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Drop amount: <white>" + this.minAmount + " <white>- " + this.maxAmount),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to set a minimum amount"),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Right Click to set a maximum amount")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
                .appearSound(Sounds.MENU_ITEM_APPEAR)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                return this.setMinAmount();
            } else if(clickType.isRightClick()) {
                return this.setMaxAmount();
            } else return false;
        });
    }

    private boolean setMinAmount() {
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Minimum amount",
                "that can drop");

        SignInput signInput = new SignInput(super.getPlugin(), super.getDPlayer(), signText);
        signInput.init(result -> {
            Bukkit.getScheduler().runTask(super.getPlugin(), () -> {

                try {
                    this.minAmount = Math.max(1, Integer.parseInt(result));
                    if(this.minAmount > maxAmount) this.maxAmount = minAmount;
                    super.addSocket(this.amountSocket(), 0);
                    getDPlayer().playSound(Sounds.SIGN_INPUT, true);
                } catch(NumberFormatException ignore) {
                }
                super.open(false);
            });
        });
        return true;
    }

    private boolean setMaxAmount() {
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Maximum amount",
                "that can drop");

        SignInput signInput = new SignInput(super.getPlugin(), super.getDPlayer(), signText);
        signInput.init(result -> {
            Bukkit.getScheduler().runTask(super.getPlugin(), () -> {

                try {
                    this.maxAmount = Math.max(1, Integer.parseInt(result));
                    if(this.maxAmount < minAmount) this.minAmount = maxAmount;
                    super.addSocket(this.amountSocket(), 0);
                    getDPlayer().playSound(Sounds.SIGN_INPUT, true);
                } catch(NumberFormatException ignore) {
                }
                super.open(false);
            });
        });
        return true;
    }

    private Socket chanceSocket() {
        if(chance == null) this.chance = 100.0;
        String string = (NumberUtil.decimalTruncation(this.chance, 17)) + "% (1/" + NumberUtil.numberComma((long) Math.ceil(100 / this.chance)) + ")";
        return new Socket(24, Slot.of(builder -> builder
                .material(Material.ENCHANTED_BOOK)
                .displayName(MiniMessage.miniMessage().deserialize("<aqua>Drop Chance"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Drop chance: <white>" + string),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to set a drop chance")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
                .appearSound(Sounds.MENU_ITEM_APPEAR)
        ), this::setChance);
    }

    private boolean setChance(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;

        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Chance that this",
                "item can drop");

        SignInput signInput = new SignInput(super.getPlugin(), super.getDPlayer(), signText);
        signInput.init(result -> {
            Bukkit.getScheduler().runTask(super.getPlugin(), () -> {

                try {
                    this.chance = Math.max(0, Math.min(100, Double.parseDouble(result)));
                    super.addSocket(this.chanceSocket(), 0);
                    getDPlayer().playSound(Sounds.SIGN_INPUT, true);
                } catch(NumberFormatException ignore) {
                }
                super.open(false);
            });
        });
        return true;
    }

    @Override
    public void refreshSockets() {
        if(!fullMenu && dItem != null) {
            super.addSocket(dItemSocket(), 50);
            this.populateSettings();
            this.fullMenu = true;
        } else if(dItem != null) {
            super.addSocket(List.of(
                    dItemSocket(),amountSocket(), chanceSocket(), buildSocket()));
        }
    }

    @Override
    public Socket menuSocket() {
        return new Socket(4, Slot.of(builder -> builder
                .material(Material.PINK_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<light_purple>Item Drop Modification"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Tool menu for modifying properties"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>of existing lootpools")
                )))
        ));
    }

    @Override
    public StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuSize.FIVE)
                .title(Component.text("Drop Settings"))
                .menuIndex(4)
                .returnIndex(40));
    }
}