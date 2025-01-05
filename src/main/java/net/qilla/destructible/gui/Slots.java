package net.qilla.destructible.gui;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.player.PlayType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

import java.util.List;

public class Slots {

    public static final Slot.Builder BACK_ITEM = Slot.builder(slot -> slot
            .material(Material.BELL)
            .displayName(MiniMessage.miniMessage().deserialize("<red>Return"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Click to return to your"),
                    MiniMessage.miniMessage().deserialize("<!italic><gray>previously opened menu"))))
    );

    public static final Slot.Builder EMPTY_ITEM = Slot.builder(slot -> slot
            .material(Material.PALE_OAK_BUTTON)
            .hideTooltip(true)
    );

    public static final Slot.Builder FILLER_ITEM = Slot.builder(slot -> slot
            .noMaterial()
            .hideTooltip(true)
    );

    public static final Slot.Builder UP_ITEM = Slot.builder(slot -> slot
            .material(Material.ARROW)
            .displayName(MiniMessage.miniMessage().deserialize("<white>Go up"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Click to shift the menu up"))
            ))
            .soundSettings(SoundSettings.of(Sound.ENTITY_BREEZE_LAND, 0.75f, 1.75f, SoundCategory.PLAYERS, PlayType.PLAYER))
    );

    public static final Slot.Builder DOWN_ITEM = Slot.builder(slot -> slot
            .material(Material.SPECTRAL_ARROW)
            .displayName(MiniMessage.miniMessage().deserialize("<white>Go down"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Click to shift the menu down"))
            ))
            .soundSettings(SoundSettings.of(Sound.ENTITY_BREEZE_JUMP, 0.25f, 1f, SoundCategory.PLAYERS, PlayType.PLAYER))
    );
}