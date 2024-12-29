package net.qilla.destructible.mining.item;

import net.qilla.destructible.data.Registries;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import java.util.function.Function;

public final class DItems {

    public static final DItem DEFAULT = new DItem.Builder()
            .material(Material.AIR)
            .defaultDisplayName()
            .noLore()
            .stackSize(1)
            .rarity(Rarity.NONE)
            .build();

    private static DItem register(@NotNull String id, @NotNull Function<DItem.Builder, DItem> factory, @NotNull DItem.Builder builder) {
        return Registries.DESTRUCTIBLE_ITEMS.put(id, factory.apply(builder.id(id)));
    }
}