package net.qilla.destructible.menus;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.player.PlayType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

import java.util.List;

public class Slots {

    public static final Slot.Builder RETURN_ITEM = Slot.builder(slot -> slot
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

    public static final Slot.Builder PREVIOUS_ITEM = Slot.builder(slot -> slot
            .material(Material.ARROW)
            .displayName(MiniMessage.miniMessage().deserialize("<white>Previous"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Click to shift the menu backwards"))
            ))
            .soundSettings(SoundSettings.of(Sound.ENTITY_BREEZE_LAND, 0.75f, 1.75f, SoundCategory.PLAYERS, PlayType.PLAYER))
    );

    public static final Slot.Builder NEXT_ITEM = Slot.builder(slot -> slot
            .material(Material.SPECTRAL_ARROW)
            .displayName(MiniMessage.miniMessage().deserialize("<white>Next"))
            .lore(ItemLore.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<!italic><gray>Click to shift the menu forwards"))
            ))
            .soundSettings(SoundSettings.of(Sound.ENTITY_BREEZE_JUMP, 0.25f, 1f, SoundCategory.PLAYERS, PlayType.PLAYER))
    );
}