package net.qilla.destructible.mining.item;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Registries;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public final class DTools {
    public static final DTool DEFAULT = new DTool(DItem.Properties.of()
            .id("DEFAULT")
            .defaultDisplayName()
            .material(Material.AIR)
            .rarity(Rarity.NONE)
            .stackSize(1),
            DTool.Properties.of()
                    .dToolType(List.of(
                            DToolType.ANY)
                    )
                    .strength(0)
                    .efficiency(0)
                    .noDurability()
    );

    public static final DTool MULTI_TOOL = register("MULTI_TOOL",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.LEGENDARY.getTextColor() + "><!italic>Multi Tool"))
                    .material(Material.DIAMOND_SHOVEL)
                    .rarity(Rarity.LEGENDARY)
                    .stackSize(1),
            itemDItemProperties -> new DTool(itemDItemProperties,
                    DTool.Properties.of()
                            .dToolType(List.of(
                                    DToolType.ANY)
                            )
                            .strength(10)
                            .efficiency(1000)
                            .noDurability()
            )
    );

    public static final DTool CHAIN_SAW = register("CHAIN_SAW",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.LEGENDARY.getTextColor() + "><!italic>Chainsaw"))
                    .material(Material.LIGHTNING_ROD)
                    .rarity(Rarity.LEGENDARY)
                    .stackSize(1),
            itemDItemProperties -> new DTool(itemDItemProperties,
                    DTool.Properties.of()
                            .dToolType(List.of(
                                    DToolType.AXE)
                            )
                            .strength(2)
                            .efficiency(0.3f)
                            .durability(4)
            )
    );

    public static final DTool WOODEN_PICKAXE = register("WOODEN_PICKAXE",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.COMMON.getTextColor() + "><!italic>Wooden Pickaxe"))
                    .material(Material.WOODEN_PICKAXE)
                    .rarity(Rarity.COMMON)
                    .stackSize(1),
            itemDItemProperties -> new DTool(itemDItemProperties,
                    DTool.Properties.of()
                            .dToolType(List.of(
                                    DToolType.PICKAXE)
                            )
                            .strength(1)
                            .efficiency(1.5f)
                            .noDurability()
            )
    );

    public static final DTool STONE_PICKAXE = register("STONE_PICKAXE",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.UNIQUE.getTextColor() + "><!italic>Stone Pickaxe"))
                    .material(Material.STONE_PICKAXE)
                    .rarity(Rarity.UNIQUE)
                    .stackSize(1),
            itemDItemProperties -> new DTool(itemDItemProperties,
                    DTool.Properties.of()
                            .dToolType(List.of(
                                    DToolType.PICKAXE)
                            )
                            .strength(2)
                            .efficiency(2.25f)
                            .noDurability()
            )
    );

    public static final DTool IRON_PICKAXE = register("IRON_PICKAXE",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.RARE.getTextColor() + "><!italic>Iron Pickaxe"))
                    .material(Material.IRON_PICKAXE)
                    .rarity(Rarity.RARE)
                    .stackSize(1),
            itemDItemProperties -> new DTool(itemDItemProperties,
                    DTool.Properties.of()
                            .dToolType(
                                    List.of(DToolType.PICKAXE)
                            )
                            .strength(3)
                            .efficiency(4.0f)
                            .noDurability()
            )
    );

    public static final DTool DIAMOND_PICKAXE = register("DIAMOND_PICKAXE",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.RARE.getTextColor() + "><!italic>Diamond Pickaxe"))
                    .material(Material.DIAMOND_PICKAXE)
                    .rarity(Rarity.RARE)
                    .stackSize(1),
            itemDItemProperties -> new DTool(itemDItemProperties,
                    DTool.Properties.of()
                            .dToolType(
                                    List.of(DToolType.PICKAXE)
                            )
                            .strength(4)
                            .efficiency(8.0f)
                            .noDurability()
            )
    );

    public static final DTool WOODEN_AXE = register("WOODEN_AXE",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.COMMON.getTextColor() + "><!italic>Wooden Axe"))
                    .material(Material.WOODEN_AXE)
                    .rarity(Rarity.COMMON)
                    .stackSize(1),
            itemDItemProperties -> new DTool(itemDItemProperties,
                    DTool.Properties.of()
                            .dToolType(List.of(
                                    DToolType.AXE)
                            )
                            .strength(1)
                            .efficiency(1.5f)
                            .noDurability()
            )
    );

    public static final DTool STONE_AXE = register("STONE_AXE",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.UNIQUE.getTextColor() + "><!italic>Stone Axe"))
                    .material(Material.STONE_AXE)
                    .rarity(Rarity.UNIQUE)
                    .stackSize(1),
            itemDItemProperties -> new DTool(itemDItemProperties,
                    DTool.Properties.of()
                            .dToolType(List.of(
                                    DToolType.AXE)
                            )
                            .strength(2)
                            .efficiency(2.25f)
                            .noDurability()
            )
    );

    public static final DTool IRON_AXE = register("IRON_AXE",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.RARE.getTextColor() + "><!italic>Iron Axe"))
                    .material(Material.IRON_AXE)
                    .rarity(Rarity.RARE)
                    .stackSize(1),
            itemDItemProperties -> new DTool(itemDItemProperties,
                    DTool.Properties.of()
                            .dToolType(List.of(
                                    DToolType.AXE)
                            )
                            .strength(3)
                            .efficiency(4.0f)
                            .noDurability()
            )
    );

    public static final DTool DIAMOND_AXE = register("DIAMOND_AXE",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.RARE.getTextColor() + "><!italic>Diamond Axe"))
                    .material(Material.DIAMOND_AXE)
                    .rarity(Rarity.RARE)
                    .stackSize(1),
            itemDItemProperties -> new DTool(itemDItemProperties,
                    DTool.Properties.of()
                            .dToolType(List.of(
                                    DToolType.AXE)
                            )
                            .strength(4)
                            .efficiency(8.0f).noDurability()
            )
    );

    public static final DTool WOODEN_SHOVEL = register("WOODEN_SHOVEL",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.COMMON.getTextColor() + "><!italic>Wooden Axe"))
                    .material(Material.WOODEN_SHOVEL)
                    .rarity(Rarity.COMMON)
                    .stackSize(1),
            itemDItemProperties -> new DTool(itemDItemProperties,
                    DTool.Properties.of()
                            .dToolType(List.of(
                                    DToolType.SHOVEL)
                            )
                            .strength(1)
                            .efficiency(1.5f)
                            .durability(1000)
            )
    );

    private static DTool register(@NotNull String id, DItem.Properties dItem, @NotNull Function<DItem.Properties, @NotNull DTool> factory) {
        DTool dTool = factory.apply(dItem.id(id));
        return Registries.DESTRUCTIBLE_TOOLS.put(id, dTool);
    }
}