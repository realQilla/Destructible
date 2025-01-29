package net.qilla.destructible.menugeneral.menu;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.menugeneral.DSlots;
import net.qilla.destructible.menugeneral.menu.select.DItemSelectMenu;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.ItemDrop;
import net.qilla.qlibrary.data.PlayerData;
import net.qilla.qlibrary.menu.MenuScale;
import net.qilla.qlibrary.menu.QStaticMenu;
import net.qilla.qlibrary.menu.StaticConfig;
import net.qilla.qlibrary.menu.input.SignInput;
import net.qilla.qlibrary.menu.socket.QSlot;
import net.qilla.qlibrary.menu.socket.QSocket;
import net.qilla.qlibrary.menu.socket.Socket;
import net.qilla.qlibrary.player.CooldownType;
import net.qilla.qlibrary.player.EnhancedPlayer;
import net.qilla.qlibrary.util.sound.QSounds;
import net.qilla.qlibrary.util.sound.QSounds.Menu;
import net.qilla.qlibrary.util.tools.NumberUtil;
import net.qilla.qlibrary.util.tools.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ItemDropCreationMenu extends QStaticMenu {

    private boolean lockedMenu = true;
    private final List<ItemDrop> lootpool;
    private final ItemDrop itemDrop;
    private DItem dItem;
    private boolean fortuneAffected = true;
    private int minAmount = 1;
    private int maxAmount = 1;
    private double chance = 100;

    public ItemDropCreationMenu(@NotNull Plugin plugin, @NotNull PlayerData<?> playerData, @NotNull List<ItemDrop> lootpool, @NotNull ItemDrop itemDrop) {
        super(plugin, playerData);
        Preconditions.checkNotNull(lootpool, "List cannot be null");
        this.lootpool = lootpool;
        this.itemDrop = itemDrop;


        this.lockedMenu = true;
        this.dItem = itemDrop.getDItem();
        this.fortuneAffected = itemDrop.isFortuneAffected();
        this.minAmount = itemDrop.getMinAmount();
        this.maxAmount = itemDrop.getMaxAmount();
        this.chance = itemDrop.getChance() * 100;

        super.addSocket(this.getSettingsSockets(), 50);
        super.finalizeMenu();
    }

    public ItemDropCreationMenu(@NotNull Plugin plugin, @NotNull PlayerData<?> playerData, @NotNull List<ItemDrop> lootpool) {
        super(plugin, playerData);
        Preconditions.checkNotNull(lootpool, "List cannot be null");
        this.lootpool = lootpool;
        this.itemDrop = null;

        super.addSocket(this.emptyItemSocket(), 100);
        super.finalizeMenu();
    }

    private List<Socket> getSettingsSockets() {
        List<Socket> socketList = new ArrayList<>(List.of(
                itemSocket(), amountSocket(), chanceSocket(), fortuneAffectedSocket()
        ));
        Collections.shuffle(socketList);
        socketList.add(buildSocket());

        return socketList;
    }

    private Socket buildSocket() {
        return new QSocket(42, DSlots.CONFIRM, event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            if(itemDrop != null) lootpool.remove(itemDrop);
            lootpool.add(new ItemDrop.Builder()
                    .dItem(dItem)
                    .fortuneAffected(fortuneAffected)
                    .minAmount(minAmount)
                    .maxAmount(maxAmount)
                    .chance(chance / 100)
                    .build());
            return super.returnMenu();
        }, CooldownType.MENU_CLICK);
    }

    private Socket emptyItemSocket() {
        return new QSocket(22, QSlot.of(builder -> builder
                .material(Material.HOPPER_MINECART)
                .displayName(MiniMessage.miniMessage().deserialize("<blue>Custom Item"))
                .lore(ItemLore.lore(List.of(MiniMessage.miniMessage().deserialize("<!italic><red>Empty"),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to set a custom item")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), this::setItem, CooldownType.MENU_CLICK);
    }

    private Socket itemSocket() {
        return new QSocket(22, QSlot.of(builder -> builder
                .material(dItem.getMaterial())
                .displayName(MiniMessage.miniMessage().deserialize("<light_purple>Drop item"))
                .lore(ItemLore.lore()
                        .addLines(List.of(
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Name <white>").append(dItem.getDisplayName()),
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Lore:")
                        ))
                        .addLines(dItem.getLore().lines())
                        .addLines(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to set a custom item")
                        )).build())
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), this::setItem, CooldownType.MENU_CLICK);

    }

    private boolean setItem(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(clickType.isLeftClick()) {
            CompletableFuture<DItem> future = new CompletableFuture<>();
            new DItemSelectMenu(super.getPlugin(), super.getPlayerData(), future).open(true);
            future.thenAccept(dItem -> {
                if(dItem == null) return;
                this.dItem = dItem;
                lockedMenu = false;
                super.addSocket(getSettingsSockets());
            });
            return true;
        } else return false;
    }

    private Socket fortuneAffectedSocket() {
        return new QSocket(31, QSlot.of(builder -> builder
                .material(Material.ENCHANTED_BOOK)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_purple>Fortune Affected"))
                .lore(ItemLore.lore()
                        .addLines(List.of(
                                MiniMessage.miniMessage().deserialize("<!italic><gray>Value <white>" + StringUtil.toName(String.valueOf(fortuneAffected))),
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to toggle")
                        )).build())
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;

            this.fortuneAffected = !this.fortuneAffected;
            super.addSocket(this.fortuneAffectedSocket());
            return true;
        }, CooldownType.MENU_CLICK);
    }

    private Socket amountSocket() {
        return new QSocket(20, QSlot.of(builder -> builder
                .material(Material.BROWN_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<gold>Drop amount"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Drop amount <white>" + this.minAmount + " <white>- " + this.maxAmount),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to set a <green><bold>MINIMUM</green> amount"),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>② <key:key.mouse.right></gold> to set a <red><bold>MAXIMUM</red> amount")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                return this.setMinAmount();
            } else if(clickType.isRightClick()) {
                return this.setMaxAmount();
            } else return false;
        }, CooldownType.MENU_CLICK);
    }

    private boolean setMinAmount() {
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Minimum amount",
                "that can drop");

        SignInput signInput = new SignInput(super.getPlugin(), super.getPlayerData(), signText);
        signInput.init(result -> {
            Bukkit.getScheduler().runTask(super.getPlugin(), () -> {

                try {
                    this.minAmount = Math.max(1, Integer.parseInt(result));
                    if(this.minAmount > maxAmount) this.maxAmount = minAmount;
                    super.addSocket(this.amountSocket());
                    super.getPlayer().playSound(QSounds.Menu.SIGN_INPUT, true);
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

        SignInput signInput = new SignInput(super.getPlugin(), super.getPlayerData(), signText);
        signInput.init(result -> {
            Bukkit.getScheduler().runTask(super.getPlugin(), () -> {

                try {
                    this.maxAmount = Math.max(1, Integer.parseInt(result));
                    if(this.maxAmount < minAmount) this.minAmount = maxAmount;
                    super.addSocket(this.amountSocket());
                    super.getPlayer().playSound(QSounds.Menu.SIGN_INPUT, true);
                } catch(NumberFormatException ignore) {
                }
                super.open(false);
            });
        });
        return true;
    }

    private Socket chanceSocket() {
        String string = (NumberUtil.decimalTruncation(this.chance, 17)) + "% (1/" + NumberUtil.numberComma((long) Math.ceil(100 / this.chance)) + ")";
        return new QSocket(24, QSlot.of(builder -> builder
                .material(Material.HONEYCOMB)
                .displayName(MiniMessage.miniMessage().deserialize("<aqua>Drop Chance"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Drop chance <white>" + string),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to set a drop chance")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), this::setChance, CooldownType.MENU_CLICK);
    }

    private boolean setChance(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;

        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Chance that this",
                "item can drop");

        SignInput signInput = new SignInput(super.getPlugin(), super.getPlayerData(), signText);
        signInput.init(result -> {
            Bukkit.getScheduler().runTask(super.getPlugin(), () -> {

                try {
                    this.chance = Math.max(0, Math.min(100, Double.parseDouble(result)));
                    super.addSocket(this.chanceSocket());
                    super.getPlayer().playSound(QSounds.Menu.SIGN_INPUT, true);
                } catch(NumberFormatException ignore) {
                }
                super.open(false);
            });
        });
        return true;
    }

    @Override
    public void refreshSockets() {
        if(!lockedMenu) super.addSocket(getSettingsSockets());
    }

    @Override
    public @NotNull Socket menuSocket() {
        return new QSocket(4, DSlots.ITEM_DROP_MODIFICATION_MENU);
    }

    @Override
    public @NotNull StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuScale.FIVE)
                .title(Component.text("Item Drop Modification"))
                .menuIndex(4)
                .returnIndex(40));
    }
}