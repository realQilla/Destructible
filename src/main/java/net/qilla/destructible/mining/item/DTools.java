package net.qilla.destructible.mining.item;

import net.kyori.adventure.text.minimessage.MiniMessage;
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
            .dToolType(List.of(DToolType.ANY))
            .strength(0)
            .efficiency(0)
            .noDurability()
            .build();

    public static final DTool WOODEN_PICKAXE = register("WOODEN_PICKAXE", DTool.Builder::build, new DTool.Builder()
            .dItem(new DItem.Builder()
                    .id("WOODEN_PICKAXE")
                    .material(Material.WOODEN_PICKAXE)
                    .defaultDisplayName()
                    .lore(List.of(MiniMessage.miniMessage().deserialize("<gray>Wooden Pickaxe")))
                    .stackSize(1)
                    .rarity(Rarity.COMMON))
            .dToolType(List.of(DToolType.PICKAXE))
            .strength(1)
            .efficiency(1)
            .durability(1000)
    );

    private static DTool register(@NotNull String id, @NotNull Function<DTool.Builder, @NotNull DTool> factory, DTool.Builder builder) {
        return Registries.DESTRUCTIBLE_TOOLS.put(id, factory.apply(builder));
    }
}