package net.qilla.destructible.mining.item.tool;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.Rarity;
import org.bukkit.Material;

import java.util.List;
import java.util.function.Function;

public final class DTools {
    public static final DTool DEFAULT = new DTool(DItem.Properties.of(),
            DTool.Properties.of()
    );

    public static final DTool MULTI_TOOL = register("MULTI_TOOL",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.LEGENDARY.getColor() + "><!italic>Multi Tool"))
                    .material(Material.DIAMOND_SHOVEL)
                    .rarity(Rarity.LEGENDARY)
                    .stackSize(1)
                    .noDurability(),
            itemProperties -> new DTool(itemProperties,
                    DTool.Properties.of()
                            .dToolType(List.of(DToolType.PICKAXE, DToolType.AXE, DToolType.SHOVEL))
                            .strength(10)
                            .efficiency(1000))
    );

    public static final DTool CHAIN_SAW = register("CHAIN_SAW",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.bidfjgdgdfgdfg.getColor() + "><!italic>Chainsaw"))
                    .material(Material.LIGHTNING_ROD)
                    .rarity(Rarity.bidfjgdgdfgdfg)
                    .stackSize(1)
                    .durability(4),
            itemProperties -> new DTool(itemProperties,
                    DTool.Properties.of()
                            .dToolType(List.of(DToolType.AXE))
                            .strength(2)
                            .efficiency(0.3f))
    );

    public static final DTool WOODEN_PICKAXE = register("WOODEN_PICKAXE",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.COMMON.getColor() + "><!italic>Wooden Pickaxe"))
                    .material(Material.WOODEN_PICKAXE)
                    .rarity(Rarity.COMMON)
                    .stackSize(1)
                    .noDurability(),
            itemProperties -> new DTool(itemProperties,
                    DTool.Properties.of()
                            .dToolType(List.of(DToolType.PICKAXE))
                            .strength(1)
                            .efficiency(1.5f))
    );

    public static final DTool STONE_PICKAXE = register("STONE_PICKAXE",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.UNIQUE.getColor() + "><!italic>Stone Pickaxe"))
                    .material(Material.STONE_PICKAXE)
                    .rarity(Rarity.UNIQUE)
                    .stackSize(1)
                    .noDurability(),
            itemProperties -> new DTool(itemProperties,
                    DTool.Properties.of()
                            .dToolType(List.of(DToolType.PICKAXE))
                            .strength(2)
                            .efficiency(2.25f))
    );

    public static final DTool IRON_PICKAXE = register("IRON_PICKAXE",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.RARE.getColor() + "><!italic>Iron Pickaxe"))
                    .material(Material.IRON_PICKAXE)
                    .rarity(Rarity.RARE)
                    .stackSize(1)
                    .noDurability(),
            itemProperties -> new DTool(itemProperties,
                    DTool.Properties.of()
                            .dToolType(List.of(DToolType.PICKAXE))
                            .strength(3)
                            .efficiency(4.0f))
    );

    public static final DTool DIAMOND_PICKAXE = register("DIAMOND_PICKAXE",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.RARE.getColor() + "><!italic>Diamond Pickaxe"))
                    .material(Material.DIAMOND_PICKAXE)
                    .rarity(Rarity.RARE)
                    .stackSize(1)
                    .noDurability(),
            itemProperties -> new DTool(itemProperties,
                    DTool.Properties.of()
                            .dToolType(List.of(DToolType.PICKAXE))
                            .strength(4)
                            .efficiency(8.0f))
    );

    public static final DTool WOODEN_AXE = register("WOODEN_AXE",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.COMMON.getColor() + "><!italic>Wooden Axe"))
                    .material(Material.WOODEN_AXE)
                    .rarity(Rarity.COMMON)
                    .stackSize(1)
                    .noDurability(),
            itemProperties -> new DTool(itemProperties,
                    DTool.Properties.of()
                            .dToolType(List.of(DToolType.AXE))
                            .strength(1)
                            .efficiency(1.5f))
    );

    public static final DTool STONE_AXE = register("STONE_AXE",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.UNIQUE.getColor() + "><!italic>Stone Axe"))
                    .material(Material.STONE_AXE)
                    .rarity(Rarity.UNIQUE)
                    .stackSize(1)
                    .noDurability(),
            itemProperties -> new DTool(itemProperties,
                    DTool.Properties.of()
                            .dToolType(List.of(DToolType.AXE))
                            .strength(2)
                            .efficiency(2.25f))
    );

    public static final DTool IRON_AXE = register("IRON_AXE",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.RARE.getColor() + "><!italic>Iron Axe"))
                    .material(Material.IRON_AXE)
                    .rarity(Rarity.RARE)
                    .stackSize(1)
                    .noDurability(),
            itemProperties -> new DTool(itemProperties,
                    DTool.Properties.of()
                            .dToolType(List.of(DToolType.AXE))
                            .strength(3)
                            .efficiency(4.0f))
    );

    public static final DTool DIAMOND_AXE = register("DIAMOND_AXE",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.RARE.getColor() + "><!italic>Diamond Axe"))
                    .material(Material.DIAMOND_AXE)
                    .rarity(Rarity.RARE)
                    .stackSize(1)
                    .noDurability(),
            itemProperties -> new DTool(itemProperties,
                    DTool.Properties.of()
                            .dToolType(List.of(DToolType.AXE))
                            .strength(4)
                            .efficiency(8.0f))
    );

    public static final DTool WOODEN_SHOVEL = register("WOODEN_SHOVEL",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.COMMON.getColor() + "><!italic>Wooden Axe"))
                    .material(Material.WOODEN_SHOVEL)
                    .rarity(Rarity.COMMON)
                    .stackSize(1)
                    .durability(1000),
            itemProperties -> new DTool(itemProperties,
                    DTool.Properties.of()
                            .dToolType(List.of(DToolType.SHOVEL))
                            .strength(1)
                            .efficiency(1.5f))
    );

    private static DTool register(String id, DItem.Properties dItem, Function<DItem.Properties, DTool> factory) {
        DTool dTool = factory.apply(dItem.id(id));
        return Registries.DESTRUCTIBLE_TOOLS.put(id, dTool);
    }
}