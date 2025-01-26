package net.qilla.destructible.menugeneral;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.DSounds;
import net.qilla.qlibrary.menu.socket.QSlot;
import net.qilla.qlibrary.menu.socket.Slot;
import net.qilla.qlibrary.util.sound.MenuSound;
import org.bukkit.Material;

import java.util.List;

public class DSlots {
    public static final Slot CONFIRM = QSlot.of(consumer -> consumer
            .material(Material.END_CRYSTAL)
            .displayName(MiniMessage.miniMessage().deserialize("<green><bold>CONFIRM"))
            .lore(ItemLore.lore(List.of(
                    Component.empty(),
                    MiniMessage.miniMessage().deserialize("<!italic><yellow><key:key.mouse.left> to set all made changes")
            )))
            .clickSound(DSounds.GENERAL_SUCCESS)
    );

    public static final Slot CREATE_NEW = QSlot.of(builder -> builder
            .material(Material.SHULKER_SHELL)
            .displayName(MiniMessage.miniMessage().deserialize("<green>New Item"))
            .lore(ItemLore.lore(List.of(
                    Component.empty(),
                    MiniMessage.miniMessage().deserialize("<!italic><yellow><key:key.mouse.left> to create a new item")
            )))
            .clickSound(MenuSound.MENU_CLICK_ITEM)
    );

    public static final Slot OVERFLOW_MENU = QSlot.of(consumer -> consumer
            .material(Material.BROWN_BUNDLE)
            .displayName(MiniMessage.miniMessage().deserialize("<gold>Overflowing Items"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray><key:key.mouse.left> to claim any items that were not"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>able to fit into your inventory")
            )))
    );

    public static final Slot ITEM_MENU = QSlot.of(consumer -> consumer
            .material(Material.QUARTZ)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Overview"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Select any item to view more"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>details or make changes")
            )))
    );

    public static final Slot TOOL_OVERVIEW_MENU = QSlot.of(consumer -> consumer
            .material(Material.IRON_PICKAXE)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Overview"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Select any tool to view more details or make changes")
            )))
    );

    public static final Slot BLOCK_MENU = QSlot.of(consumer -> consumer
            .material(Material.DECORATED_POT)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Block Settings"))
    );

    public static final Slot LOAD_BLOCK_MENU = QSlot.of(consumer -> consumer
            .material(Material.COMMAND_BLOCK)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Load Blocks"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Settings for loading, viewing, etc."),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>custom blocks into the world")
            )))
    );

    public static final Slot BLOCK_OVERVIEW_MENU = QSlot.of(consumer -> consumer
            .material(Material.CHEST)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Overview"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Select any block to view more"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>details or make changes")
            )))
    );

    public static final Slot BLOCK_MODIFICATION_MENU = QSlot.of(consumer -> consumer
            .material(Material.CRAFTING_TABLE)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Block Modification"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Here you can find all of the"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>necessary tools for creating"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>or modifying destructible blocks")
            )))
    );

    public static final Slot ITEM_MODIFICATION_MENU = QSlot.of(consumer -> consumer
            .material(Material.CRAFTING_TABLE)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Item Modification"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Here you can find all of the"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>necessary tools for creating"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>or modifying destructible items")
            )))
    );
}
