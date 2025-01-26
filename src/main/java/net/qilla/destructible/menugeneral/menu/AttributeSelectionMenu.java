package net.qilla.destructible.menugeneral.menu;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.menugeneral.menu.select.SingleToolTypeSelectionMenu;
import net.qilla.destructible.mining.item.ToolType;
import net.qilla.destructible.mining.item.attributes.Attribute;
import net.qilla.destructible.mining.item.attributes.AttributeTypes;
import net.qilla.qlibrary.data.PlayerData;
import net.qilla.qlibrary.menu.DynamicConfig;
import net.qilla.qlibrary.menu.MenuScale;
import net.qilla.qlibrary.menu.QDynamicMenu;
import net.qilla.qlibrary.menu.StaticConfig;
import net.qilla.qlibrary.menu.input.SignInput;
import net.qilla.qlibrary.menu.socket.QSlot;
import net.qilla.qlibrary.menu.socket.QSocket;
import net.qilla.qlibrary.menu.socket.Socket;
import net.qilla.qlibrary.player.CooldownType;
import net.qilla.qlibrary.util.sound.MenuSound;
import net.qilla.qlibrary.util.tools.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class AttributeSelectionMenu extends QDynamicMenu<Attribute<?>> {

    private final Set<Attribute<?>> attributeSet;

    public AttributeSelectionMenu(@NotNull Plugin plugin, @NotNull PlayerData playerData, @NotNull Set<Attribute<?>> attributeSet) {
        super(plugin, playerData, attributeSet);

        this.attributeSet = attributeSet;

        super.addSocket(getSettingsSockets(), 50);
        super.populateModular();
        super.finalizeMenu();
    }

    private List<Socket> getSettingsSockets() {
        return List.of(
                toolEfficiencySocket(), toolStrengthSocket(), toolFortuneSocket(), toolTypeSocket(),
                toolDurabilitySocket()
        );
    }

    @Override
    public Socket createSocket(int index, Attribute<?> item) {
        return new QSocket(index, QSlot.of(builder -> builder
                .material(item.type().getRepresentation())
                .displayName(MiniMessage.miniMessage().deserialize("<yellow>" + StringUtil.toName(item.type().getKey())))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Value <white>" + item.value().toString()),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><key:key.mouse.right> to unset")
                )))
                .clickSound(MenuSound.MENU_CLICK_ITEM)
                .glow(true)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isRightClick()) {
                attributeSet.remove(item);
                super.refreshSockets();
                return true;
            } else return false;
        }, CooldownType.MENU_CLICK);
    }

    public Socket toolEfficiencySocket() {
        return new QSocket(11, QSlot.of(builder -> builder
                .material(Material.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE)
                .displayName(MiniMessage.miniMessage().deserialize("<aqua>Tool Efficiency"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><key:key.mouse.left> to modify")
                )))
                .clickSound(MenuSound.MENU_CLICK_ITEM)
                .appearSound(MenuSound.MENU_ITEM_APPEAR)
        ), this::inputEfficiency, CooldownType.OPEN_MENU);
    }

    private boolean inputEfficiency(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Efficiency value",
                "for this item");

        new SignInput(super.getPlugin(), super.getPlayerData(), signText).init(result -> {
            Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                if(!result.isEmpty()) {
                    int value = Math.max(1, Integer.parseInt(result));

                    attributeSet.removeIf(attribute -> attribute.type() == AttributeTypes.MINING_EFFICIENCY);
                    attributeSet.add(new Attribute<>(AttributeTypes.MINING_EFFICIENCY, value));
                    super.refreshSockets();
                    super.getPlayer().playSound(MenuSound.SIGN_INPUT, true);
                }
                super.open(false);
            });
        });
        return true;
    }

    public Socket toolStrengthSocket() {
        return new QSocket(12, QSlot.of(builder -> builder
                .material(Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE)
                .displayName(MiniMessage.miniMessage().deserialize("<red>Tool Strength"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><key:key.mouse.left> to set")
                )))
                .clickSound(MenuSound.MENU_CLICK_ITEM)
                .appearSound(MenuSound.MENU_ITEM_APPEAR)
        ), this::inputStrength, CooldownType.OPEN_MENU);
    }

    private boolean inputStrength(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Strength value",
                "for this item");

        new SignInput(super.getPlugin(), super.getPlayerData(), signText).init(result -> {
            Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                if(!result.isEmpty()) {
                    int value = Math.max(1, Integer.parseInt(result));

                    attributeSet.removeIf(attribute -> attribute.type() == AttributeTypes.MINING_STRENGTH);
                    attributeSet.add(new Attribute<>(AttributeTypes.MINING_STRENGTH, value));
                    super.refreshSockets();
                    super.getPlayer().playSound(MenuSound.SIGN_INPUT, true);
                }
                super.open(false);
            });
        });
        return true;
    }

    public Socket toolFortuneSocket() {
        return new QSocket(13, QSlot.of(builder -> builder
                .material(Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE)
                .displayName(MiniMessage.miniMessage().deserialize("<light_purple>Tool Fortune"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><key:key.mouse.left> to set")
                )))
                .clickSound(MenuSound.MENU_CLICK_ITEM)
                .appearSound(MenuSound.MENU_ITEM_APPEAR)
        ), this::inputFortune, CooldownType.OPEN_MENU);
    }

    private boolean inputFortune(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Fortune value",
                "for this item");

        new SignInput(super.getPlugin(), super.getPlayerData(), signText).init(result -> {
            Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                if(!result.isEmpty()) {
                    int value = Math.max(1, Integer.parseInt(result));

                    attributeSet.removeIf(attribute -> attribute.type() == AttributeTypes.MINING_FORTUNE);
                    attributeSet.add(new Attribute<>(AttributeTypes.MINING_FORTUNE, value));
                    super.refreshSockets();
                    super.getPlayer().playSound(MenuSound.SIGN_INPUT, true);
                }
                super.open(false);
            });
        });
        return true;
    }

    public Socket toolTypeSocket() {
        return new QSocket(14, QSlot.of(builder -> builder
                .material(Material.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE)
                .displayName(MiniMessage.miniMessage().deserialize("<gold>Tool Type"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><key:key.mouse.left> to set")
                )))
                .clickSound(MenuSound.MENU_CLICK_ITEM)
                .appearSound(MenuSound.MENU_ITEM_APPEAR)
        ), event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            CompletableFuture<ToolType> future = new CompletableFuture<>();
            new SingleToolTypeSelectionMenu(super.getPlugin(), super.getPlayerData(), future).open(true);

            future.thenAccept(toolType -> {
                attributeSet.removeIf(attribute -> attribute.type() == AttributeTypes.TOOL_TYPE);
                attributeSet.add(new Attribute<>(AttributeTypes.TOOL_TYPE, toolType));
            });
            return true;
        }, CooldownType.OPEN_MENU);
    }

    public Socket toolDurabilitySocket() {
        return new QSocket(15, QSlot.of(builder -> builder
                .material(Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_gray>Tool Durability"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><key:key.mouse.left> to set")
                )))
                .clickSound(MenuSound.MENU_CLICK_ITEM)
                .appearSound(MenuSound.MENU_ITEM_APPEAR)
        ), this::inputDurability, CooldownType.OPEN_MENU);
    }

    private boolean inputDurability(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Durability value",
                "for this item");

        new SignInput(super.getPlugin(), super.getPlayerData(), signText).init(result -> {
            Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                if(!result.isEmpty()) {
                    int value = Math.max(1, Integer.parseInt(result));

                    attributeSet.removeIf(attribute -> attribute.type() == AttributeTypes.ITEM_MAX_DURABILITY);
                    attributeSet.add(new Attribute<>(AttributeTypes.ITEM_MAX_DURABILITY, value));
                    super.refreshSockets();
                    super.getPlayer().playSound(MenuSound.SIGN_INPUT, true);
                }
                super.open(false);
            });
        });
        return true;
    }

    @Override
    public Socket menuSocket() {
        return new QSocket(4, QSlot.of(builder -> builder
                .material(Material.RED_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<red>Attribute Modification"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Set which attributes apply"),
                        MiniMessage.miniMessage().deserialize("<!italic><gray>to the selected item")
                )))
        ));
    }

    @Override
    public StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuScale.SIX)
                .title(Component.text("Attribute Modification"))
                .menuIndex(4)
                .returnIndex(49));
    }

    @Override
    public DynamicConfig dynamicConfig() {
        return DynamicConfig.of(builder -> builder
                .dynamicSlots(List.of(
                        28, 29, 30, 31, 32, 33, 34, 38, 39, 40, 41, 42
                ))
                .nextIndex(25)
                .previousIndex(19)
                .shiftAmount(1)
        );
    }
}