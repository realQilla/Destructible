package net.qilla.destructible.mining.item.tool;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.Rarity;
import org.bukkit.Material;

import java.util.function.Function;

public class DTools {
    public static final DTool DEFAULT = new DTool(DItem.Properties.of(),
            DTool.Properties.of()
    );

    public static final DTool WOODEN_PICKAXE = register("wooden_pickaxe",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.COMMON.getColor() + "><!italic>Wooden Pickaxe"))
                    .material(Material.WOODEN_PICKAXE)
                    .rarity(Rarity.COMMON)
                    .stackSize(1)
                    .noDurability(),
            itemProperties -> new DTool(itemProperties,
                    DTool.Properties.of()
                            .dToolType(DToolType.PICKAXE)
                            .strength(1)
                            .efficiency(1.5f))
    );

    public static final DTool STONE_PICKAXE = register("stone_pickaxe",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.UNIQUE.getColor() + "><!italic>Stone Pickaxe"))
                    .material(Material.STONE_PICKAXE)
                    .rarity(Rarity.UNIQUE)
                    .stackSize(1)
                    .noDurability(),
            itemProperties -> new DTool(itemProperties,
                    DTool.Properties.of()
                            .dToolType(DToolType.PICKAXE)
                            .strength(2)
                            .efficiency(2.25f))
    );

    public static final DTool IRON_PICKAXE = register("iron_pickaxe",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.RARE.getColor() + "><!italic>Iron Pickaxe"))
                    .material(Material.IRON_PICKAXE)
                    .rarity(Rarity.RARE)
                    .stackSize(1)
                    .noDurability(),
            itemProperties -> new DTool(itemProperties,
                    DTool.Properties.of()
                            .dToolType(DToolType.PICKAXE)
                            .strength(3)
                            .efficiency(4.0f))
    );

    public static final DTool DIAMOND_PICKAXE = register("diamond_pickaxe",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.RARE.getColor() + "><!italic>Diamond Pickaxe"))
                    .material(Material.DIAMOND_PICKAXE)
                    .rarity(Rarity.RARE)
                    .stackSize(1)
                    .noDurability(),
            itemProperties -> new DTool(itemProperties,
                    DTool.Properties.of()
                            .dToolType(DToolType.PICKAXE)
                            .strength(4)
                            .efficiency(8.0f))
    );

    public static final DTool WOODEN_AXE = register("wooden_axe",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.COMMON.getColor() + "><!italic>Wooden Axe"))
                    .material(Material.WOODEN_AXE)
                    .rarity(Rarity.COMMON)
                    .stackSize(1)
                    .noDurability(),
            itemProperties -> new DTool(itemProperties,
                    DTool.Properties.of()
                            .dToolType(DToolType.AXE)
                            .strength(1)
                            .efficiency(1.5f))
    );

    public static final DTool IRON_AXE = register("iron_axe",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.RARE.getColor() + "><!italic>Iron Axe"))
                    .material(Material.IRON_AXE)
                    .rarity(Rarity.RARE)
                    .stackSize(1)
                    .noDurability(),
            itemProperties -> new DTool(itemProperties,
                    DTool.Properties.of()
                            .dToolType(DToolType.AXE)
                            .strength(3)
                            .efficiency(4.0f))
    );

    public static final DTool DIAMOND_AXE = register("diamond_axe",
            DItem.Properties.of()
                    .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.RARE.getColor() + "><!italic>Diamond Axe"))
                    .material(Material.DIAMOND_AXE)
                    .rarity(Rarity.RARE)
                    .stackSize(1)
                    .noDurability(),
            itemProperties -> new DTool(itemProperties,
                    DTool.Properties.of()
                            .dToolType(DToolType.AXE)
                            .strength(4)
                            .efficiency(8.0f))
    );

    private static DTool register(String id, DItem.Properties dItem, Function<DItem.Properties, DTool> factory) {
        DTool dTool = factory.apply(dItem.id(id));
        return Registries.TOOLS.register(id, dTool);
    }
}