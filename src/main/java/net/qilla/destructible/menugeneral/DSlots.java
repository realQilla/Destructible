package net.qilla.destructible.menugeneral;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.DSounds;
import net.qilla.qlibrary.menu.socket.QSlot;
import net.qilla.qlibrary.menu.socket.Slot;
import net.qilla.qlibrary.util.sound.QSounds;
import org.bukkit.Material;
import java.util.List;

public class DSlots {
    public static final Slot CONFIRM = QSlot.of(builder -> builder
            .material(Material.END_CRYSTAL)
            .displayName(MiniMessage.miniMessage().deserialize("<green><bold>CONFIRM"))
            .lore(ItemLore.lore(List.of(
                    Component.empty(),
                    MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to save any changes made")
            )))
            .clickSound(DSounds.GENERAL_SUCCESS)
    );

    public static final Slot MODIFICATION_CREATE = QSlot.of(builder -> builder
            .material(Material.GREEN_BUNDLE)
            .displayName(MiniMessage.miniMessage().deserialize("<green>Create New"))
            .lore(ItemLore.lore(List.of(
                    Component.empty(),
                    MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to open the creation menu")
            )))
            .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
    );

    public static final Slot MODIFICATION_REMOVE = QSlot.of(builder -> builder
            .material(Material.BARRIER)
            .displayName(MiniMessage.miniMessage().deserialize("<red>Remove"))
            .lore(ItemLore.lore(List.of(
                    Component.empty(),
                    MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to remove the"),
                    MiniMessage.miniMessage().deserialize("<!italic><yellow>object being modified")
            )))
    );

    public static final Slot OVERFLOW_MENU = QSlot.of(builder -> builder
            .material(Material.BROWN_BUNDLE)
            .displayName(MiniMessage.miniMessage().deserialize("<aqua>Overflowing Stash"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray><blue>ℹ</blue> Temporary storage menu"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>for any uncollected items")
            )))
    );

    public static final Slot ITEM_MENU = QSlot.of(builder -> builder
            .material(Material.QUARTZ)
            .displayName(MiniMessage.miniMessage().deserialize("<aqua>Item Overview"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray><blue>ℹ</blue> Select any item to view more"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>details or make changes")
            )))
    );

    public static final Slot BLOCK_MENU = QSlot.of(builder -> builder
            .material(Material.BARREL)
            .displayName(MiniMessage.miniMessage().deserialize("<aqua>Block Settings"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><blue>ℹ</blue> <gray>Hub menu for anything"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>related to custom blocks")
            )))
    );

    public static final Slot LOAD_BLOCK_MENU = QSlot.of(builder -> builder
            .material(Material.COMMAND_BLOCK)
            .displayName(MiniMessage.miniMessage().deserialize("<aqua>Loading Blocks"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray><blue>ℹ</blue> Central menu for viewing"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>and modifying loaded blocks globally")
            )))
    );

    public static final Slot BLOCK_OVERVIEW_MENU = QSlot.of(builder -> builder
            .material(Material.CHEST)
            .displayName(MiniMessage.miniMessage().deserialize("<aqua>Block Overview"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray><blue>ℹ</blue> General overview menu for custom"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>blocks and any applied settings")
            )))
    );

    public static final Slot BLOCK_MODIFICATION_MENU = QSlot.of(builder -> builder
            .material(Material.CRAFTING_TABLE)
            .displayName(MiniMessage.miniMessage().deserialize("<aqua>Block Modification"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray><blue>ℹ</blue> Central modification menu for each"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>and every type of custom block")
            )))
    );

    public static final Slot ITEM_MODIFICATION_MENU = QSlot.of(builder -> builder
            .material(Material.CRAFTING_TABLE)
            .displayName(MiniMessage.miniMessage().deserialize("<aqua>Item Modification"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray><blue>ℹ</blue> Central modification menu for both"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>custom tools and items alike")
                    )))
    );

    public static final Slot LOOTPOOL_MODIFICATION_MENU = QSlot.of(builder -> builder
            .material(Material.PINK_BUNDLE)
            .displayName(MiniMessage.miniMessage().deserialize("<aqua>Lootpool Modification"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><blue>ℹ</blue> <gray>General modification menu for items"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>existing in a lootpool")
            )))
    );

    public static final Slot ITEM_DROP_MODIFICATION_MENU = QSlot.of(builder -> builder
            .material(Material.BLUE_BUNDLE)
            .displayName(MiniMessage.miniMessage().deserialize("<aqua>Item Drop Modification"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray><blue>ℹ</blue> General modification menu for an item"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>tied to an existing lootpool")
            )))
    );

    public static final Slot LOOTPOOL_OVERVIEW_MENU = QSlot.of(builder -> builder
            .material(Material.PINK_BUNDLE)
            .displayName(MiniMessage.miniMessage().deserialize("<aqua>Lootpool Overview"))
            .lore(ItemLore.lore(List.of(
                    Component.empty(),
                    MiniMessage.miniMessage().deserialize("<!italic><gray><blue>ℹ</blue> General information regarding a block's"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>lootpool, each item is ordered by most"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>to least likely to drop.")
            )))
    );

    public static final Slot ATTRIBUTE_SELECTION_MENU = QSlot.of(builder -> builder
            .material(Material.RED_BUNDLE)
            .displayName(MiniMessage.miniMessage().deserialize("<aqua>Attribute Modification"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray><blue>ℹ</blue> Set which attributes apply"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>to the selected item")
            )))
    );

    public static final Slot SOUND_SELECTION_MENU = QSlot.of(builder -> builder
            .material(Material.NAUTILUS_SHELL)
            .displayName(MiniMessage.miniMessage().deserialize("<aqua>Sound Search"))
    );

    public static final Slot PARTICLE_SELECTION_MENU = QSlot.of(builder -> builder
            .material(Material.HEART_OF_THE_SEA)
            .displayName(MiniMessage.miniMessage().deserialize("<aqua>Particle Search"))
    );

    public static final Slot SINGLE_TOOL_SELECTION_MENU = QSlot.of(builder -> builder
            .material(Material.BLACK_BUNDLE)
            .displayName(MiniMessage.miniMessage().deserialize("<aqua>Tool Selection"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray><blue>ℹ</blue> Set which tool type applies to"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>the current item")
            )))
    );

    public static final Slot MULTI_TOOL_SELECTION_MENU = QSlot.of(builder -> builder
            .material(Material.BLACK_BUNDLE)
            .displayName(MiniMessage.miniMessage().deserialize("<dark_gray>Tool Selection"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray><blue>ℹ</blue> Set which tool types apply to"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>the current block")
            )))
    );

    public static final Slot RARITY_SELECTION_MENU = QSlot.of(builder -> builder
            .material(Material.LAPIS_LAZULI)
            .displayName(MiniMessage.miniMessage().deserialize("<aqua>Rarity Selection"))
    );

    public static final Slot ITEM_SELECTION_MENU = QSlot.of(builder -> builder
            .material(Material.IRON_INGOT)
            .displayName(MiniMessage.miniMessage().deserialize("<aqua>Item Search"))
    );

    public static final Slot BLOCK_SELECTION_MENU = QSlot.of(builder -> builder
            .material(Material.COARSE_DIRT)
            .displayName(MiniMessage.miniMessage().deserialize("<blue>Block Search"))
    );

    public static final Slot HIGHLIGHT_SELECTION_MENU = QSlot.of(builder -> builder
            .material(Material.BLUE_ICE)
            .displayName(MiniMessage.miniMessage().deserialize("<aqua>Block Highlight Search"))
    );

    public static final Slot DITEM_SELECTION_MENU = QSlot.of(builder -> builder
            .material(Material.DIAMOND_AXE)
            .displayName(MiniMessage.miniMessage().deserialize("<aqua>Custom Item Search"))
    );

    public static final Slot DBLOCK_SELECTION_MENU = QSlot.of(builder -> builder
            .material(Material.REINFORCED_DEEPSLATE)
            .displayName(MiniMessage.miniMessage().deserialize("<aqua>Custom BlockSearch"))
    );

    public static final Slot VIEW_LOADED_BLOCKS = QSlot.of(builder -> builder
            .material(Material.OMINOUS_BOTTLE)
            .displayName(MiniMessage.miniMessage().deserialize("<aqua>Loaded Block View"))
            .lore(ItemLore.lore(List.of(
                    Component.empty(),
                    MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to specify blocks to view")
            )))
            .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
    );

    public static final Slot DISABLE_LOADED_BLOCK_VIEW = QSlot.of(builder -> builder
            .material(Material.GLASS_BOTTLE)
            .displayName(MiniMessage.miniMessage().deserialize("<red>Disable Block Loading"))
            .lore(ItemLore.lore(List.of(
                    Component.empty(),
                    MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to disable block loading")
            )))
            .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
    );

    public static final Slot SAVED_CHANGES = QSlot.of(builder -> builder
            .material(Material.SLIME_BALL)
            .displayName(MiniMessage.miniMessage().deserialize("<green><bold>SAVE</bold> to Config"))
            .lore(ItemLore.lore(List.of(
                    Component.empty(),
                    MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to save any made changes.")
            )))
            .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
    );

    public static final Slot RELOADED_CHANGES = QSlot.of(builder -> builder
            .material(Material.SNOWBALL)
            .displayName(MiniMessage.miniMessage().deserialize("<aqua><bold>RELOAD</bold> from Config"))
            .lore(ItemLore.lore(List.of(
                    Component.empty(),
                    MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to load the config, undoing any unsaved changes.")
            )))
            .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
    );

    public static final Slot CLEAR_SAVED = QSlot.of(builder -> builder
            .material(Material.FIRE_CHARGE)
            .displayName(MiniMessage.miniMessage().deserialize("<red><bold>CLEAR</bold> Config"))
            .lore(ItemLore.lore(List.of(
                    Component.empty(),
                    MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to clear any stored data.")
            )))
            .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
    );
}