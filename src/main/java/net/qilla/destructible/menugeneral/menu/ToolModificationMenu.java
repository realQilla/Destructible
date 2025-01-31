package net.qilla.destructible.menugeneral.menu;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.menugeneral.DSlots;
import net.qilla.destructible.menugeneral.menu.select.AttributeSelectMenu;
import net.qilla.destructible.menugeneral.menu.select.ItemSelectMenu;
import net.qilla.destructible.menugeneral.menu.select.RaritySelectMenu;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.Rarity;
import net.qilla.destructible.mining.item.attributes.Attribute;
import net.qilla.qlibrary.data.PlayerData;
import net.qilla.qlibrary.menu.MenuScale;
import net.qilla.qlibrary.menu.QStaticMenu;
import net.qilla.qlibrary.menu.StaticConfig;
import net.qilla.qlibrary.menu.StaticMenu;
import net.qilla.qlibrary.menu.input.ChatInput;
import net.qilla.qlibrary.menu.input.SignInput;
import net.qilla.qlibrary.menu.socket.*;
import net.qilla.qlibrary.player.CooldownType;
import net.qilla.qlibrary.util.sound.QSounds;
import net.qilla.qlibrary.util.tools.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ToolModificationMenu extends QStaticMenu {

    private static final Map<String, DItem> DITEM_MAP = DRegistry.ITEMS;

    private boolean lockedMenu = true;

    private String originalId = null;
    private String id = StringUtil.uniqueIdentifier(8);
    private Material material = Material.STONE;
    private Component displayName = MiniMessage.miniMessage().deserialize("<red>Unnamed Item");
    private ItemLore lore = ItemLore.lore().build();
    private int loreCycle = 0;
    private Rarity rarity = Rarity.NONE;
    private final Set<Attribute<?>> staticAttributes = new HashSet<>();

    public ToolModificationMenu(@NotNull Plugin plugin, @NotNull PlayerData<?> playerData, @NotNull DItem dItem) {
        super(plugin, playerData);
        Preconditions.checkNotNull(dItem, "DItem cannot be null");

        this.lockedMenu = false;
        this.originalId = dItem.getID();
        this.id = dItem.getID();
        this.material = dItem.getMaterial();
        this.displayName = dItem.getDisplayName();
        this.lore = dItem.getLore();
        this.rarity = dItem.getRarity();
        this.staticAttributes.addAll(dItem.getStaticAttributes().getAll());

        super.addSocket(getSettingsSockets(), 25);
        super.addSocket(removeSocket());
        super.finalizeMenu();
    }

    public ToolModificationMenu(@NotNull Plugin plugin, @NotNull PlayerData<?> playerData) {
        super(plugin, playerData);

        super.addSocket(emptyMaterialSocket(), 250);
        super.finalizeMenu();
    }

    private List<Socket> getSettingsSockets() {
        List<Socket> socketList = new ArrayList<>(List.of(
                idSocket(), materialSocket(), displayNameSocket(), loreSocket(),
                raritySocket(), staticAttributesSocket()
        ));

        Collections.shuffle(socketList);
        socketList.add(buildSocket());

        return socketList;
    }

    public Socket buildSocket() {
        return new QSocket(38, DSlots.CONFIRM, this::build, CooldownType.MENU_CLICK);
    }

    private boolean build(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;
        DItem newDItem = DItem.of(builder -> builder
                .id(id)
                .material(material)
                .displayName(displayName)
                .lore(lore)
                .rarity(rarity)
                .staticAttributes(staticAttributes)
        );

        if(originalId != null) {
            DITEM_MAP.computeIfPresent(originalId, (id, dItem) -> null);
            super.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>" + originalId + " has been successfully replaced by " + id + "!"));
        } else super.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>" + newDItem.getID() + " has been successfully registered!"));
        DITEM_MAP.put(newDItem.getID(), newDItem);
        return super.returnMenu();
    }

    public Socket removeSocket() {
        return new QSocket(44, DSlots.MODIFICATION_REMOVE, this::remove, CooldownType.MENU_CLICK);
    }

    private boolean remove(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;

        super.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>" + originalId + " has been successfully unregistered."));
        DITEM_MAP.remove(originalId);
        super.getPlayer().playSound(QSounds.Menu.RESET, true);
        return super.returnMenu();
    }

    public Socket emptyMaterialSocket() {
        return new QSocket(13, QSlot.of(builder -> builder
                .material(Material.HOPPER_MINECART)
                .displayName(MiniMessage.miniMessage().deserialize("<blue>Item Material"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <red>Empty"),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to set a material or use an item to quickset")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), this::clickMaterial, CooldownType.MENU_CLICK);
    }

    public Socket materialSocket() {
        return new QSocket(13, QSlot.of(builder -> builder
                .material(material)
                .displayName(MiniMessage.miniMessage().deserialize("<blue>Item Material"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + StringUtil.toName(material.toString())),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to set a material or use an item to quickset")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), this::clickMaterial, CooldownType.MENU_CLICK);
    }

    private boolean clickMaterial(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;

        Material cursorMaterial = event.getCursor().getType();

        if(cursorMaterial.isEmpty()) {
            CompletableFuture<Material> future = new CompletableFuture<>();
            new ItemSelectMenu(super.getPlugin(), super.getPlayerData(), future).open(true);
            future.thenAccept(material -> {
                if(material == null) return;
                this.material = material;
                lockedMenu = false;
                super.addSocket(getSettingsSockets());
            });
        } else {
            if(!cursorMaterial.isItem()) {
                super.getPlayer().playSound(QSounds.General.GENERAL_ERROR, true);
                return false;
            }
            this.material = cursorMaterial;
            lockedMenu = false;
            super.addSocket(getSettingsSockets());
            super.getPlayer().setItemOnCursor(null);
        }
        return true;
    }

    public Socket idSocket() {
        return new QSocket(22, QSlot.of(builder -> builder
                .material(Material.OAK_SIGN)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_green>Item ID"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + id),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to make modifications")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), this::inputID, CooldownType.MENU_CLICK);
    }

    private boolean inputID(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;
        List<String> signText = List.of("^^^^^^^^^^^^^^^", "Unique identifier", "for this item");
        super.requestSignInput(signText, result -> {
            if(!result.isEmpty()) {
                if(DITEM_MAP.containsKey(result)) {
                    super.getPlayer().sendMessage("<red>Item ID already exists.");
                    super.getPlayer().playSound(QSounds.General.GENERAL_ERROR, true);
                } else {
                    id = result;
                    super.addSocket(this.idSocket());
                    getPlayer().playSound(QSounds.Menu.SIGN_INPUT, true);
                }
            }
            super.open(false);
        });
        return true;
    }

    public Socket displayNameSocket() {
        return new QSocket(10, QSlot.of(builder -> builder
                .material(Material.GOLDEN_APPLE)
                .displayName(MiniMessage.miniMessage().deserialize("<gold>Item Name"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>").append(displayName),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to make modifications")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), this::inputDisplayName, CooldownType.MENU_CLICK);
    }

    private boolean inputDisplayName(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;
        String chatText = "<yellow>Type the name of the item, using the <white><hover:show_text:'https://docs.advntr.dev/minimessage/format'><click:open_url:'https://docs.advntr.dev/minimessage/format'>MiniMessage</white> format. <gold>Shift-Click <bold><insert:'" +
                MiniMessage.miniMessage().serialize(displayName) + "'>HERE</insert></gold> get the previous name. Create a blank line by typing EMPTY, and CANCEL to return.";

        super.requestChatInput(List.of(MiniMessage.miniMessage().deserialize(chatText)), result -> {
            if(!result.equalsIgnoreCase("cancel") && !result.isEmpty()) {
                if(result.equalsIgnoreCase("empty")) {
                    displayName = Component.empty();
                } else displayName = MiniMessage.miniMessage().deserialize(result);

                super.addSocket(this.displayNameSocket());
                super.getPlayer().playSound(QSounds.Menu.SIGN_INPUT, true);
            }
            super.open(false);
        });
        return true;
    }

    public Socket loreSocket() {
        return new QSocket(11, QSlot.of(builder -> builder
                .material(Material.LIME_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<green>Item Lore"))
                .lore(getLore())
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), this::modifyLore, CooldownType.MENU_CLICK);
    }

    private ItemLore getLore() {
        if(loreCycle > lore.lines().size()) loreCycle = 0;
        else if(loreCycle < 0) loreCycle = lore.lines().size();

        ItemLore.Builder loreBuilder = ItemLore.lore();
        loreBuilder.addLine(MiniMessage.miniMessage().deserialize("<!italic><gray>Current value:"));
        List<Component> loreList = new ArrayList<>();

        if(!this.lore.lines().isEmpty()) loreList.addAll(this.lore.lines());
        loreList.add(MiniMessage.miniMessage().deserialize("<!italic><yellow>New Line"));

        Component curLine = MiniMessage.miniMessage().deserialize("<!italic><gold>»</gold> ").append(loreList.get(loreCycle)).append(MiniMessage.miniMessage().deserialize(" <gold>«"));
        loreList.set(loreCycle, curLine);

        loreBuilder.addLines(loreList);
        loreBuilder.addLines(List.of(
                Component.empty(),
                MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to cycle down"),
                MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>② <key:key.mouse.right></gold> to cycle up"),
                MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>③ <key:key.sneak> + <key:key.mouse.left></gold> to make modifications"),
                MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>④ <key:key.sneak> + <key:key.mouse.right></gold> to remove line")
        ));
        return loreBuilder.build();
    }

    private boolean modifyLore(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(clickType.isLeftClick()) {
            if(clickType.isShiftClick()) {
                String chatText = "<yellow>Type the item's lore for line <gold>" + (loreCycle + 1) +
                        "</gold> using the <gold><hover:show_text:'https://docs.advntr.dev/minimessage/format'><click:open_url:'https://docs.advntr.dev/minimessage/format'>MiniMessage</gold> format. Create a blank line by typing EMPTY, and CANCEL to return.";
                super.requestChatInput(List.of(MiniMessage.miniMessage().deserialize(chatText)), result -> {
                    if(!result.equalsIgnoreCase("cancel") && !result.isEmpty()) {
                        applyLine(result.equalsIgnoreCase("empty") ? Component.empty() : MiniMessage.miniMessage().deserialize(result));
                        super.addSocket(this.loreSocket());
                        super.getPlayer().playSound(QSounds.Menu.SIGN_INPUT, true);
                    }
                    super.open(false);
                });
            } else {
                loreCycle++;
                super.addSocket(this.loreSocket());
            }
            return true;
        } else if(clickType.isRightClick()) {
            if(clickType.isShiftClick()) this.removeLine();
            else loreCycle--;

            super.addSocket(this.loreSocket());
            return true;
        } else return false;
    }

    public void applyLine(Component line) {
        List<Component> loreList = new ArrayList<>(lore.lines());
        if(lore.lines().size() <= loreCycle) loreList.add(loreCycle, line);
        else loreList.set(loreCycle, line);
        this.lore = ItemLore.lore().addLines(loreList).build();
    }

    public void removeLine() {
        List<Component> loreList = new ArrayList<>(lore.lines());
        if(lore.lines().size() > loreCycle) {
            loreList.remove(loreCycle);
            this.lore = ItemLore.lore().addLines(loreList).build();
            loreCycle = Math.max(0, loreCycle - 1);
        }
    }

    public Socket raritySocket() {
        return new QSocket(15, QSlot.of(builder -> builder
                .material(Material.LAPIS_LAZULI)
                .displayName(MiniMessage.miniMessage().deserialize("<aqua>Rarity"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value ").append(rarity.getComponent()),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to make modifications")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;

            CompletableFuture<Rarity> future = new CompletableFuture<>();
            new RaritySelectMenu(super.getPlugin(), super.getPlayerData(), future).open(true);
            future.thenAccept(rarity -> this.rarity = rarity);
            return true;
        }, CooldownType.OPEN_MENU);
    }

    public Socket staticAttributesSocket() {
        return new QSocket(16, QSlot.of(builder -> builder
                .material(Material.RED_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<red>Static Attributes"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray><blue>ℹ</blue> Modification option for persistent attributes"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>within this item, meaning these are the item's"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>BASE unchangeable stats."),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to make modifications")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;

            new AttributeSelectMenu(super.getPlugin(), super.getPlayerData(), this.staticAttributes).open(true);
            return true;
        }, CooldownType.OPEN_MENU);
    }

    @Override
    public void playerClickMenu(InventoryClickEvent event) {
        if(event.getClickedInventory().getHolder() instanceof StaticMenu) {
            event.setCancelled(true);
            Socket socket = super.getSockets().get(event.getSlot());
            if(socket != null) socket.onClick(super.getPlayer(), event, super.getPlayerData());
        }
    }

    @Override
    public void refreshSockets() {
        if(!lockedMenu) super.addSocket(getSettingsSockets());
    }

    @Override
    public @NotNull Socket menuSocket() {
        return new QSocket(4, DSlots.ITEM_MODIFICATION_MENU);
    }

    @Override
    public @NotNull StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuScale.FIVE)
                .title(Component.text("Tool Modification"))
                .menuIndex(4)
                .returnIndex(40));
    }
}