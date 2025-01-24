package net.qilla.destructible.mining.item;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;

import java.util.List;

public final class DItems {

    public static final DItem MISSING_ITEM = DItem.of(builder -> builder
            .id("MISSING_ITEM")
            .material(Material.BARRIER)
            .displayName(MiniMessage.miniMessage().deserialize("<red>Missing Item"))
            .stackSize(99)
            .lore(ItemLore.lore(List.of(
                    Component.empty(),
                    MiniMessage.miniMessage().deserialize("<!italic><red>You should never have this item...")
            )))
            .rarity(Rarity.NONE)
            .version(-1)
    );
}