package net.qilla.destructible.mining.item;

import net.qilla.destructible.data.Registries;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public final class DTools {
    public static final DTool DEFAULT = new DTool.Builder()
            .dItem(new DItem.Builder()
                    .id("DEFAULT")
                    .material(Material.AIR)
                    .defaultDisplayName()
                    .noLore()
                    .stackSize(1)
                    .rarity(Rarity.NONE))
            .dToolType(List.of(ToolType.HAND))
            .strength(0)
            .efficiency(0)
            .noDurability()
            .build();

    private static DItem register(@NotNull String id, @NotNull Function<DTool.Builder, @NotNull DTool> factory, DTool.Builder builder) {
        return Registries.DESTRUCTIBLE_ITEMS.put(id, factory.apply(builder));
    }
}