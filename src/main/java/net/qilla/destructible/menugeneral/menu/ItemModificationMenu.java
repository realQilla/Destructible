package net.qilla.destructible.menugeneral.menu;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.data.Sounds;
import net.qilla.destructible.menugeneral.MenuSize;
import net.qilla.destructible.menugeneral.StaticConfig;
import net.qilla.destructible.menugeneral.StaticMenu;
import net.qilla.destructible.menugeneral.input.ChatInput;
import net.qilla.destructible.menugeneral.input.SignInput;
import net.qilla.destructible.menugeneral.menu.select.ItemSelectMenu;
import net.qilla.destructible.menugeneral.menu.select.RaritySelectMenu;
import net.qilla.destructible.menugeneral.slot.Slot;
import net.qilla.destructible.menugeneral.slot.Slots;
import net.qilla.destructible.menugeneral.slot.Socket;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.Rarity;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ItemModificationMenu extends StaticMenu {

    private static final Map<String, DItem> DITEM_MAP = DRegistry.ITEMS;

    private boolean lockedMenu = true;
    private String originalId = null;
    private String id = UUID.randomUUID().toString();
    private Material material = Material.STONE;
    private Component displayName = MiniMessage.miniMessage().deserialize("<red>Unnamed Item");
    private ItemLore lore = ItemLore.lore().build();
    private int loreCycle = 0;
    private Integer stackSize = 64;
    private Rarity rarity = Rarity.NONE;
    private Boolean resource = true;

    public ItemModificationMenu(@NotNull Destructible plugin, @NotNull DPlayer dPlayer, @NotNull DItem dItem) {
        super(plugin, dPlayer);
        Preconditions.checkNotNull(dItem, "DItem cannot be null");

        this.lockedMenu = false;
        this.originalId = dItem.getId();
        this.id = dItem.getId();
        this.material = dItem.getMaterial();
        this.displayName = dItem.getDisplayName();
        this.lore = dItem.getLore();
        this.stackSize = dItem.getStackSize();
        this.rarity = dItem.getRarity();
        this.resource = dItem.isResource();

        this.addSocket(getSettingsSockets(), 25);
        super.addSocket(removeSocket());
        super.finalizeMenu();
    }

    public ItemModificationMenu(@NotNull Destructible plugin, @NotNull DPlayer dPlayer) {
        super(plugin, dPlayer);

        super.addSocket(emptyMaterialSocket(), 50);
        super.finalizeMenu();
    }

    private List<Socket> getSettingsSockets() {
        List<Socket> socketList = new ArrayList<>(List.of(
                idSocket(), materialSocket(), displayNameSocket(), loreSocket(),
                stackSizeSocket(), raritySocket(), resourceSocket()
        ));

        Collections.shuffle(socketList);
        socketList.add(buildSocket());
        return socketList;
    }

    public Socket buildSocket() {
        return new Socket(38, Slots.CONFIRM, this::build, CooldownType.MENU_CLICK);
    }

    private boolean build(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;

        DItem newDItem = DItem.of(builder -> builder
                .id(id)
                .material(material)
                .displayName(displayName)
                .lore(lore)
                .stackSize(stackSize)
                .rarity(rarity)
                .resource(resource));

        if(originalId != null) DITEM_MAP.computeIfPresent(originalId, (id, dItem) -> null);
        super.getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>" + newDItem.getId() + " has been successfully registered!"));
        DITEM_MAP.put(newDItem.getId(), newDItem);
        return super.returnMenu();
    }

    public Socket removeSocket() {
        return new Socket(44, Slot.of(builder -> builder
                .material(Material.BARRIER)
                .displayName(MiniMessage.miniMessage().deserialize("<red>Remove!"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Left Click to permanently"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>delete this item")
                )))
        ), this::remove, CooldownType.MENU_CLICK);
    }

    private boolean remove(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;

        getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>" + originalId + " has been successfully unregistered."));
        DITEM_MAP.remove(originalId);
        getDPlayer().playSound(Sounds.RESET, true);
        return super.returnMenu();
    }

    public Socket emptyMaterialSocket() {
        return new Socket(13, Slot.of(builder -> builder
                .material(Material.HOPPER_MINECART)
                .displayName(MiniMessage.miniMessage().deserialize("<blue>Item Material"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <red>Empty"),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left click with either an item or nothing to set a material")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
                .appearSound(Sounds.MENU_ITEM_APPEAR)
        ), this::clickMaterial, CooldownType.MENU_CLICK);
    }

    public Socket materialSocket() {
        return new Socket(13, Slot.of(builder -> builder
                .material(material)
                .displayName(MiniMessage.miniMessage().deserialize("<blue>Item Material"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + StringUtil.toName(material.toString())),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left click with either an item or nothing to set a material")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
                .appearSound(Sounds.MENU_ITEM_APPEAR)
        ), this::clickMaterial, CooldownType.MENU_CLICK);
    }

    private boolean clickMaterial(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;

        Material cursorMaterial = event.getCursor().getType();

        if(cursorMaterial.isEmpty()) {
            CompletableFuture<Material> future = new CompletableFuture<>();
            new ItemSelectMenu(super.getPlugin(), super.getDPlayer(), future).open(true);
            future.thenAccept(material -> {
                if(material != null) this.material = material;
                lockedMenu = false;
                super.addSocket(getSettingsSockets());
            });
        } else {
            if(!cursorMaterial.isItem()) {
                getDPlayer().playSound(Sounds.GENERAL_ERROR, true);
                return false;
            }
            this.material = cursorMaterial;
            lockedMenu = false;
            getDPlayer().getCraftPlayer().setItemOnCursor(null);
            super.addSocket(getSettingsSockets());
        }
        return true;
    }

    public Socket idSocket() {
        return new Socket(22, Slot.of(builder -> builder
                .material(Material.OAK_SIGN)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_green>Item ID"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + id),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to modify")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
                .appearSound(Sounds.MENU_ITEM_APPEAR)
        ), this::inputID, CooldownType.MENU_CLICK);
    }

    private boolean inputID(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Unique identifier",
                "for this item");

        new SignInput(super.getPlugin(), super.getDPlayer(), signText).init(result -> {
            Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                if(!result.isEmpty()) {
                    if(DITEM_MAP.containsKey(result)) {
                        super.getDPlayer().sendMessage("<red>Item ID already exists.");
                        super.getDPlayer().playSound(Sounds.GENERAL_ERROR, true);
                    } else {
                        id = result;
                        super.addSocket(this.idSocket());
                        getDPlayer().playSound(Sounds.SIGN_INPUT, true);
                    }
                }
                super.open(false);
            });
        });
        return true;
    }

    public Socket displayNameSocket() {
        return new Socket(10, Slot.of(builder -> builder
                .material(Material.GOLDEN_APPLE)
                .displayName(MiniMessage.miniMessage().deserialize("<gold>Item Name"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>").append(displayName),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to modify")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
                .appearSound(Sounds.MENU_ITEM_APPEAR)
        ), this::inputDisplayName, CooldownType.MENU_CLICK);
    }

    private boolean inputDisplayName(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;
        String chatText = "<yellow>Type the name of the item, using the <white><hover:show_text:'https://docs.advntr.dev/minimessage/format'><click:open_url:'https://docs.advntr.dev/minimessage/format'>MiniMessage</white> format. <gold>Shift-Click <bold><insert:'" + MiniMessage.miniMessage().serialize(displayName) + "'>HERE</insert></gold> get the previous name. You may cancel by typing RETURN.";

        new ChatInput(super.getPlugin(), super.getDPlayer(), MiniMessage.miniMessage().deserialize(chatText)).init(result -> {
            Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                if(!result.equalsIgnoreCase("return") && !result.isEmpty()) {
                    displayName = MiniMessage.miniMessage().deserialize(result);
                    super.addSocket(this.displayNameSocket());
                    getDPlayer().playSound(Sounds.SIGN_INPUT, true);
                }
                super.open(false);
            });
        });
        return true;
    }

    public Socket loreSocket() {
        return new Socket(11, Slot.of(builder -> builder
                .material(Material.LIME_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<green>Item Lore"))
                .lore(getLore())
                .clickSound(Sounds.MENU_CLICK_ITEM)
                .appearSound(Sounds.MENU_ITEM_APPEAR)
        ), this::modifyLore, CooldownType.MENU_CLICK);
    }

    private ItemLore getLore() {
        if(loreCycle > lore.lines().size()) loreCycle = 0;
        else if(loreCycle < 0) loreCycle = lore.lines().size();

        ItemLore.Builder loreBuilder = ItemLore.lore();
        loreBuilder.addLine(MiniMessage.miniMessage().deserialize("<!italic><gray>Current value: <white>"));
        List<Component> loreList = new ArrayList<>();

        if(!this.lore.lines().isEmpty()) loreList.addAll(this.lore.lines());
        loreList.add(MiniMessage.miniMessage().deserialize("<!italic><gold>New Line"));

        Component curLine = MiniMessage.miniMessage().deserialize("<!italic><white>»</white></!italic> ").append(loreList.get(loreCycle)).append(MiniMessage.miniMessage().deserialize(" <!italic><white>«</!italic>"));
        loreList.set(loreCycle, curLine);

        loreBuilder.addLines(loreList);
        loreBuilder.addLines(List.of(
                Component.empty(),
                MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to cycle down"),
                MiniMessage.miniMessage().deserialize("<!italic><yellow>Right Click to cycle up"),
                MiniMessage.miniMessage().deserialize("<!italic><yellow>Middle Click to modify")
        ));
        return loreBuilder.build();
    }

    private boolean modifyLore(InventoryClickEvent event) {
        ClickType clickType = event.getClick();

        if(clickType == ClickType.MIDDLE) {
            String chatText = "<yellow>Type the item's lore for line <gold>" + (loreCycle + 1) +"</gold> using the <gold><hover:show_text:'https://docs.advntr.dev/minimessage/format'><click:open_url:'https://docs.advntr.dev/minimessage/format'>MiniMessage</gold> format. You may cancel by typing RETURN.";

            new ChatInput(super.getPlugin(), super.getDPlayer(),  MiniMessage.miniMessage().deserialize(chatText)).init(result -> {
                Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                    if(!result.equalsIgnoreCase("return") && !result.isEmpty()) {
                        applyLine(MiniMessage.miniMessage().deserialize(result));
                        super.addSocket(this.loreSocket());
                        getDPlayer().playSound(Sounds.SIGN_INPUT, true);
                    }
                    super.open(false);
                });
            });
            return true;
        } else if(clickType.isLeftClick()) {
            loreCycle++;
            super.addSocket(this.loreSocket());
            return true;
        } else if(clickType.isRightClick()) {
            loreCycle--;
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

    public Socket stackSizeSocket() {
        return new Socket(16, Slot.of(builder -> builder
                .material(Material.SNOWBALL)
                .amount(stackSize)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_aqua>Maximum stack size"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + stackSize),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to modify")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
                .appearSound(Sounds.MENU_ITEM_APPEAR)
        ), this::inputStackSize, CooldownType.MENU_CLICK);
    }

    private boolean inputStackSize(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Item's maximum",
                "stack size");

        new SignInput(super.getPlugin(), super.getDPlayer(), signText).init(result -> {
            Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                if(!result.isEmpty()) {
                    this.stackSize = Math.max(1, Math.min(99, Integer.parseInt(result)));
                    super.addSocket(this.stackSizeSocket());
                    getDPlayer().playSound(Sounds.SIGN_INPUT, true);
                }
                super.open(false);
            });
        });
        return true;
    }

    public Socket raritySocket() {
        return new Socket(15, Slot.of(builder -> builder
                .material(Material.LAPIS_LAZULI)
                .displayName(MiniMessage.miniMessage().deserialize("<aqua>Rarity"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value ").append(rarity.getComponent()),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to modify")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
                .appearSound(Sounds.MENU_ITEM_APPEAR)
        ), event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;

            CompletableFuture<Rarity> future = new CompletableFuture<>();
            new RaritySelectMenu(super.getPlugin(), super.getDPlayer(), future).open(true);
            future.thenAccept(rarity -> this.rarity = rarity);
            return true;
        }, CooldownType.MENU_CLICK);
    }

    public Socket resourceSocket() {
        return new Socket(19, Slot.of(builder -> builder
                .material(Material.DIAMOND)
                .displayName(MiniMessage.miniMessage().deserialize("<light_purple>Resource"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Toggle for if an item should should lose its"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>unique properties, ability to be placed, etc."),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + StringUtil.toName(resource.toString())),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left Click to modify")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
                .appearSound(Sounds.MENU_ITEM_APPEAR)
        ), event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            resource = !resource;
            super.addSocket(this.resourceSocket());
            return true;
        }, CooldownType.MENU_CLICK);
    }

    @Override
    public void inventoryClickEvent(InventoryClickEvent event) {
        if(event.getClickedInventory().getHolder() instanceof StaticMenu) event.setCancelled(true);
        super.handleClick(event);
    }

    @Override
    public void refreshSockets() {
        if(!lockedMenu) super.addSocket(getSettingsSockets());
    }

    @Override
    public Socket menuSocket() {
        return new Socket(4, Slots.ITEM_MODIFICATION_MENU);
    }

    @Override
    public StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuSize.FIVE)
                .title(Component.text("Item Modification"))
                .menuIndex(4)
                .returnIndex(40));
    }
}
