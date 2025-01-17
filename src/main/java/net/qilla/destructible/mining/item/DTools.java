package net.qilla.destructible.mining.item;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Registries;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Function;

public final class DTools {
    public static final DTool MISSING_TOOL = new DTool.Builder()
            .item(new DItem.Builder()
                    .id("DEFAULT")
                    .material(Material.WOODEN_PICKAXE)
                    .displayName(MiniMessage.miniMessage().deserialize("<red>Missing Tool"))
                    .noLore()
                    .stackSize(1)
                    .rarity(Rarity.NONE))
            .toolType(Set.of())
            .strength(0)
            .efficiency(0)
            .build();

    private static DItem register(@NotNull String id, @NotNull Function<DTool.Builder, @NotNull DTool> factory, DTool.Builder builder) {
        return Registries.DESTRUCTIBLE_ITEMS.put(id, factory.apply(builder));
    }
}