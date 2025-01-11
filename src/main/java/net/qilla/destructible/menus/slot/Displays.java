package net.qilla.destructible.menus.slot;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;

import java.util.List;

public class Displays {

    public static final Display MISSING = Display.of(slot -> slot
            .material(Material.BARRIER)
            .displayName(MiniMessage.miniMessage().deserialize("<red>Missing Item"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>This item is missing"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>from the menu")
            )))
    );

    public static final Display RETURN = Display.of(slot -> slot
            .material(Material.BELL)
            .displayName(MiniMessage.miniMessage().deserialize("<red>Return"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Click to return to your"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>previously opened menu")
            )))
    );

    public static final Display EMPTY_SLOT = Display.of(slot -> slot
            .material(Material.PALE_OAK_BUTTON)
            .hideTooltip(true)
    );

    public static final Display FILLER = Display.of(slot -> slot
            .noMaterial()
            .hideTooltip(true)
    );

    public static final Display PREVIOUS = Display.of(slot -> slot
            .material(Material.ARROW)
            .displayName(MiniMessage.miniMessage().deserialize("<white>Previous"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Left Click to shift the menu backwards")
            )))
    );

    public static final Display NEXT = Display.of(slot -> slot
            .material(Material.SPECTRAL_ARROW)
            .displayName(MiniMessage.miniMessage().deserialize("<white>Next"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Left Click to shift the menu forwards")
            )))
    );

    public static final Display SEARCH = Display.of(slot -> slot
            .material(Material.OAK_SIGN)
            .displayName(MiniMessage.miniMessage().deserialize("<white>Search"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Left Click to search for"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>something more specific")
            )))
    );

    public static final Display RESET_SEARCH = Display.of(builder2 -> builder2
            .material(Material.BARRIER)
            .displayName(MiniMessage.miniMessage().deserialize("<red>Reset Search"))
            .lore(ItemLore.lore(List.of(MiniMessage.miniMessage().deserialize("<!italic><gray>Resets your currently searched term")
            )))
    );

    public static final Display CONFIRM = Display.of(consumer -> consumer
            .material(Material.END_CRYSTAL)
            .displayName(MiniMessage.miniMessage().deserialize("<green><bold>CONFIRM"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Left click to accept any"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>changes you have made")
            )))
    );

    public static final Display OVERFLOW_MENU = Display.of(consumer -> consumer
            .material(Material.BROWN_BUNDLE)
            .displayName(MiniMessage.miniMessage().deserialize("<gold>Overflowing Items"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Left Click to claim any items that were not"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>able to fit into your inventory")
            )))
    );

    public static final Display ITEM_MENU = Display.of(consumer -> consumer
            .material(Material.QUARTZ)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Item Modification"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Select any item to view more"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>details or make changes")
            )))
    );

    public static final Display TOOL_MENU = Display.of(consumer -> consumer
            .material(Material.IRON_PICKAXE)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Tool Modification"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Select any tool to view more details or make changes")
            )))
    );

    public static final Display BLOCK_MENU = Display.of(consumer -> consumer
            .material(Material.CHEST)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Block Modification"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Select any block to view more"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>details or make changes")
            )))
    );

    public static final Display BLOCK_MODIFICATION_MENU = Display.of(consumer -> consumer
            .material(Material.CRAFTING_TABLE)
            .displayName(MiniMessage.miniMessage().deserialize("<yellow>Block Creation"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Here you can find all of the"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>necessary tools for creating"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>or modifying destructible blocks")
            )))
    );
}