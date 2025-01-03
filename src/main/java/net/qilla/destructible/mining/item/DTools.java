package net.qilla.destructible.mining.item;

import net.qilla.destructible.data.Registries;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public final class DTools {
    public static final DTool DEFAULT = new DTool.Builder()
            .item(new DItem.Builder()
                    .id("DEFAULT")
                    .material(Material.AIR)
                    .defaultDisplayName()
                    .noLore()
                    .stackSize(1)
                    .rarity(Rarity.NONE))
            .toolType(List.of(ToolType.HAND))
            .toolStrength(0)
            .toolEfficiency(0)
            .noToolDurability()
            .build();

    private static DItem register(@NotNull String id, @NotNull Function<DTool.Builder, @NotNull DTool> factory, DTool.Builder builder) {
        return Registries.DESTRUCTIBLE_ITEMS.put(id, factory.apply(builder));
    }
}