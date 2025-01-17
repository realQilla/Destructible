package net.qilla.destructible.mining.item;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Registries;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import java.util.function.Function;

public final class DItems {

    public static final DItem MISSING_ITEM = new DItem.Builder()
            .id("MISSING_ITEM")
            .material(Material.BARRIER)
            .displayName(MiniMessage.miniMessage().deserialize("<red>Missing Item"))
            .noLore()
            .stackSize(1)
            .rarity(Rarity.NONE)
            .build();

    private static DItem register(@NotNull String id, @NotNull Function<DItem.Builder, DItem> factory, @NotNull DItem.Builder builder) {
        return Registries.DESTRUCTIBLE_ITEMS.put(id, factory.apply(builder.id(id)));
    }
}