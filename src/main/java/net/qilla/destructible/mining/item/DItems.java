package net.qilla.destructible.mining.item;

import net.qilla.destructible.data.Registries;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import java.util.function.Function;

public final class DItems {

    public static final DItem DEFAULT = new DItem(
            DItem.Properties.of()
                    .id("DEFAULT")
                    .material(Material.AIR)
                    .defaultDisplayName()
                    .noLore()
                    .stackSize(1)
                    .rarity(Rarity.NONE)
    );

    private static DItem register(@NotNull String id, @NotNull Function<DItem.Properties, DItem> factory, @NotNull DItem.Properties properties) {
        return Registries.DESTRUCTIBLE_ITEMS.put(id, factory.apply(properties.id(id)));
    }
}