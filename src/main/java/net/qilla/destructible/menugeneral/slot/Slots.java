package net.qilla.destructible.menugeneral.slot;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Sounds;
import org.bukkit.Material;

import java.util.List;

public class Slots {

    public static final Slot MISSING = Slot.of(slot -> slot
            .material(Material.BARRIER)
            .displayName(MiniMessage.miniMessage().deserialize("<red>Missing Item"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>This item is missing"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>from the menu!")
            )))
    );

    public static final Slot RETURN = Slot.of(slot -> slot
            .material(Material.BELL)
            .displayName(MiniMessage.miniMessage().deserialize("<red>Return"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Left Click to return to your"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>previously accessed menu")
            )))
            .clickSound(Sounds.RETURN_MENU)
    );

    public static final Slot EMPTY_MODULAR_SLOT = Slot.of(slot -> slot
            .material(Material.PALE_OAK_BUTTON)
            .hideTooltip(true)
    );

    public static final Slot FILLER = Slot.of(slot -> slot
            .hideTooltip(true)
            .material(null)
    );

    public static final Slot PREVIOUS = Slot.of(slot -> slot
            .material(Material.ARROW)
            .displayName(MiniMessage.miniMessage().deserialize("<white>Previous"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Left Click to shift the menu backwards")
            )))
            .clickSound(Sounds.MENU_ROTATE_PREVIOUS)
    );

    public static final Slot NEXT = Slot.of(slot -> slot
            .material(Material.SPECTRAL_ARROW)
            .displayName(MiniMessage.miniMessage().deserialize("<white>Next"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Left Click to shift the menu forwards")
            )))
            .clickSound(Sounds.MENU_ROTATE_NEXT)
    );

    public static final Slot SEARCH = Slot.of(slot -> slot
            .material(Material.OAK_SIGN)
            .displayName(MiniMessage.miniMessage().deserialize("<white>Search"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Left Click to search for"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>something more specific")
            )))
            .clickSound(Sounds.MENU_CLICK_ITEM)
    );

    public static final Slot RESET_SEARCH = Slot.of(builder2 -> builder2
            .material(Material.BARRIER)
            .displayName(MiniMessage.miniMessage().deserialize("<red>Reset Search"))
            .lore(ItemLore.lore(List.of(MiniMessage.miniMessage().deserialize("<!italic><gray>Resets your currently searched term")
            )))
            .clickSound(Sounds.RESET)
    );

    public static final Slot CONFIRM = Slot.of(consumer -> consumer
            .material(Material.END_CRYSTAL)
            .displayName(MiniMessage.miniMessage().deserialize("<green><bold>CONFIRM"))
            .lore(ItemLore.lore(List.of(
                    Component.empty(),
                    MiniMessage.miniMessage().deserialize("<!italic><yellow>Left click to set all made changes")
            )))
            .clickSound(Sounds.GENERAL_SUCCESS)
    );

    public static final Slot CREATE_NEW = Slot.of(builder -> builder
            .material(Material.SHULKER_SHELL)
            .displayName(MiniMessage.miniMessage().deserialize("<green>Create New"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Left Click to open the modification menu")
            )))
            .clickSound(Sounds.MENU_CLICK_ITEM)
    );

    public static final Slot OVERFLOW_MENU = Slot.of(consumer -> consumer
            .material(Material.BROWN_BUNDLE)
            .displayName(MiniMessage.miniMessage().deserialize("<gold>Overflowing Items"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Left Click to claim any items that were not"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>able to fit into your inventory")
            )))
    );

    public static final Slot ITEM_MENU = Slot.of(consumer -> consumer
            .material(Material.QUARTZ)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Item Overview"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Select any item to view more"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>details or make changes")
            )))
    );

    public static final Slot TOOL_MENU = Slot.of(consumer -> consumer
            .material(Material.IRON_PICKAXE)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Tool Overview"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Select any tool to view more details or make changes")
            )))
    );

    public static final Slot BLOCK_MENU = Slot.of(consumer -> consumer
            .material(Material.CHEST)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Block Modification"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Select any block to view more"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>details or make changes")
            )))
    );

    public static final Slot BLOCK_MODIFICATION_MENU = Slot.of(consumer -> consumer
            .material(Material.CRAFTING_TABLE)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Block Modification"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Here you can find all of the"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>necessary tools for creating"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>or modifying destructible blocks")
            )))
    );

    public static final Slot ITEM_MODIFICATION_MENU = Slot.of(consumer -> consumer
            .material(Material.CRAFTING_TABLE)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Item Modification"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Here you can find all of the"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>necessary tools for creating"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>or modifying destructible items")
            )))
    );
}