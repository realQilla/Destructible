package net.qilla.destructible.menugeneral.menu;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Registries;
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
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.FormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ItemModificationMenu extends StaticMenu {

    private boolean fullMenu;
    private final DItem dItem;
    private String id;
    private Material material;
    private Component displayName;
    private ItemLore lore;
    private int loreCycle = 0;
    private Integer stackSize;
    private Rarity rarity;
    private Boolean resource;

    public ItemModificationMenu(@NotNull DPlayer dPlayer, DItem dItem) {
        super(dPlayer);
        this.dItem = dItem;

        if(dItem != null) {
            this.fullMenu = true;
            this.id = dItem.getId();
            this.material = dItem.getMaterial();
            this.displayName = dItem.getDisplayName();
            this.lore = dItem.getLore();
            this.stackSize = dItem.getStackSize();
            this.rarity = dItem.getRarity();
            this.resource = dItem.isResource();
            this.populateSettings();
        }
        super.addSocket(materialSocket(), 50);
        super.finalizeMenu();
    }

    private void populateSettings() {
        List<Socket> socketList = new ArrayList<>(List.of(
                idSocket(), displayNameSocket(), loreSocket(), stackSizeSocket(),
                raritySocket(), resourceSocket()
        ));
        Collections.shuffle(socketList);
        socketList.add(buildSocket());
        socketList.add(removeSocket());

        addSocket(socketList, 25);
    }

    public Socket buildSocket() {
        return new Socket(24, Slots.CONFIRM, this::build);
    }

    private boolean build(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;
        DItem dItem = new DItem.Builder()
                .id(id)
                .material(material)
                .displayName(displayName)
                .lore(lore)
                .stackSize(stackSize)
                .rarity(rarity)
                .resource(resource)
                .build();
        if(this.dItem != null) {
            Registries.DESTRUCTIBLE_ITEMS.remove(this.dItem.getId());
            getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>" + dItem.getId() + " has been successfully replaced by " + id + "!"));
        } else getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>" + dItem.getId() + " has been successfully registered!"));
        Registries.DESTRUCTIBLE_ITEMS.put(dItem.getId(), dItem);
        getDPlayer().getPlugin().getCustomItemsFile().save();
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
        ), this::remove);
    }

    private boolean remove(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;
        if(this.dItem == null) {
            getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<red>This item does not currently exist!"));
            return false;
        }

        getDPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>" + dItem.getId() + " has been successfully unregistered."));
        Registries.DESTRUCTIBLE_ITEMS.remove(dItem.getId());
        getDPlayer().getPlugin().getCustomItemsFile().save();
        getDPlayer().playSound(Sounds.RESET, true);
        return super.returnMenu();
    }

    public Socket materialSocket() {
        if(material == null) {
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
            ), this::clickMaterial);
        } else return new Socket(13, Slot.of(builder -> builder
                .material(material)
                .displayName(MiniMessage.miniMessage().deserialize("<blue>Item Material"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + FormatUtil.toName(material.toString())),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow>Left click with either an item or nothing to set a material")
                )))
                .clickSound(Sounds.MENU_CLICK_ITEM)
                .appearSound(Sounds.MENU_ITEM_APPEAR)
        ), this::clickMaterial);
    }

    private boolean clickMaterial(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;
        Material cursorMaterial = event.getCursor().getType();
        if(cursorMaterial.isEmpty()) {
            CompletableFuture<Material> future = new CompletableFuture<>();
            new ItemSelectMenu(super.getDPlayer(), future).open(true);
            future.thenAccept(material -> {
                if(material != null) this.material = material;
            });
        } else {
            if(!cursorMaterial.isItem()) {
                getDPlayer().playSound(Sounds.GENERAL_ERROR, true);
                return false;
            }
            this.material = cursorMaterial;
            getDPlayer().getCraftPlayer().setItemOnCursor(null);
            this.refreshSockets();
        }
        return true;
    }

    public Socket idSocket() {
        if(id == null) id = UUID.randomUUID().toString();
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
        ), this::inputID);
    }

    private boolean inputID(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Unique identifier",
                "for this item");

        new SignInput(super.getDPlayer(), signText).init(result -> {
            Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {
                if(!result.isEmpty()) {
                    if(Registries.DESTRUCTIBLE_ITEMS.containsKey(result)) {
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
        if(displayName == null)
            displayName = MiniMessage.miniMessage().deserialize("<white>" + FormatUtil.toName(material.toString()));
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
        ), this::inputDisplayName);
    }

    private boolean inputDisplayName(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;
        Component chatText = MiniMessage.miniMessage().deserialize(
                "<gold>Type the name of the item, using the <white><hover:show_text:'https://docs.advntr.dev/minimessage/format'><click:open_url:'https://docs.advntr.dev/minimessage/format'>MiniMessage</white> format. You may cancel by typing \"return\".");

        new ChatInput(super.getDPlayer(), chatText).init(result -> {
            Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {
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
        if(lore == null) lore = ItemLore.lore().build();
        return new Socket(11, Slot.of(builder -> builder
                .material(Material.LIME_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<green>Item Lore"))
                .lore(getLore())
                .clickSound(Sounds.MENU_CLICK_ITEM)
                .appearSound(Sounds.MENU_ITEM_APPEAR)
        ), this::modifyLore);
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
            Component chatText = MiniMessage.miniMessage().deserialize(
                    "<gold>Type the item's lore for line <white>" + (loreCycle + 1) + "</white> using the <white><hover:show_text:'https://docs.advntr.dev/minimessage/format'><click:open_url:'https://docs.advntr.dev/minimessage/format'>MiniMessage</white> format. You may cancel by typing \"return\".");

            new ChatInput(super.getDPlayer(), chatText).init(result -> {
                Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {
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
        if(stackSize == null) stackSize = 64;
        return new Socket(19, Slot.of(builder -> builder
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
        ), this::inputStackSize);
    }

    private boolean inputStackSize(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Item's maximum",
                "stack size");

        new SignInput(super.getDPlayer(), signText).init(result -> {
            Bukkit.getScheduler().runTask(super.getDPlayer().getPlugin(), () -> {
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
        if(rarity == null) rarity = Rarity.NONE;
        return new Socket(20, Slot.of(builder -> builder
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
            new RaritySelectMenu(getDPlayer(), future).open(true);
            future.thenAccept(rarity -> this.rarity = rarity);
            return true;
        });
    }

    public Socket resourceSocket() {
        if(resource == null) resource = true;
        return new Socket(28, Slot.of(builder -> builder
                .material(Material.DIAMOND)
                .displayName(MiniMessage.miniMessage().deserialize("<light_purple>Resource"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Toggle for if an item should should lose its"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>unique properties, ability to be placed, etc."),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + FormatUtil.toName(resource.toString())),
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
        });
    }

    @Override
    public void inventoryClickEvent(InventoryClickEvent event) {
        if(event.getClickedInventory().getHolder() instanceof StaticMenu) {
            event.setCancelled(true);
        }
    }

    @Override
    public void refreshSockets() {
        if(!fullMenu && material != null) {
            super.addSocket(materialSocket(), 100);
            this.populateSettings();
            fullMenu = true;
        } else if(material != null) {
            super.addSocket(List.of(
                    materialSocket(), idSocket(), displayNameSocket(), loreSocket(),
                    stackSizeSocket(), raritySocket(), resourceSocket()
            ));
        }
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
