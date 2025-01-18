package net.qilla.destructible.mining.item;

import net.qilla.destructible.data.DRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public final class DTools {
    public static final DTool HAND = new DTool.Builder()
            .item(new DItem.Builder())
            .durability(-1)
            .strength(0)
            .efficiency(1)
            .build();

    private static DItem register(@NotNull String id, @NotNull Function<DTool.Builder, @NotNull DTool> factory, DTool.Builder builder) {
        return DRegistry.DESTRUCTIBLE_ITEMS.put(id, factory.apply(builder));
    }
}