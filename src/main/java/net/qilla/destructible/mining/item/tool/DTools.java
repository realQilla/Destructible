package net.qilla.destructible.mining.item.tool;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.item.Rarity;
import org.bukkit.Material;

public class DTools {
    public static final DTool DEFAULT = new DefaultDTool(DTool.Properties.of("default"));

    public static final DTool WOODEN_PICKAXE = register(new PickaxeDTool(DTool.Properties.of("wooden_pickaxe")
            .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.COMMON.getColor() + "><!italic>Wooden Pickaxe"))
            .material(Material.WOODEN_PICKAXE)
            .dToolType(DToolType.PICKAXE)
            .rarity(Rarity.COMMON)
            .stackSize(1)
            .noDurability()
            .strength(1)
            .efficiency(1.5f)
    ));

    public static final DTool STONE_PICKAXE = register(new PickaxeDTool(DTool.Properties.of("stone_pickaxe")
            .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.UNUSUAL.getColor() + "><!italic>Stone Pickaxe"))
            .material(Material.STONE_PICKAXE)
            .dToolType(DToolType.PICKAXE)
            .rarity(Rarity.UNUSUAL)
            .stackSize(1)
            .noDurability()
            .strength(2)
            .efficiency(2.0f)
    ));

    public static final DTool IRON_PICKAXE = register(new PickaxeDTool(DTool.Properties.of("iron_pickaxe")
            .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.RARE.getColor() + "><!italic>Iron Pickaxe"))
            .material(Material.IRON_PICKAXE)
            .dToolType(DToolType.PICKAXE)
            .rarity(Rarity.RARE)
            .stackSize(1)
            .noDurability()
            .strength(3)
            .efficiency(4.0f)
    ));

    public static final DTool DIAMOND_PICKAXE = register(new PickaxeDTool(DTool.Properties.of("diamond_pickaxe")
            .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.RARE.getColor() + "><!italic>Diamond Pickaxe"))
            .material(Material.DIAMOND_PICKAXE)
            .dToolType(DToolType.PICKAXE)
            .rarity(Rarity.RARE)
            .stackSize(1)
            .noDurability()
            .strength(4)
            .efficiency(8.0f)
    ));

    public static final DTool WOODEN_AXE = register(new PickaxeDTool(DTool.Properties.of("wooden_axe")
            .displayName(MiniMessage.miniMessage().deserialize("<" + Rarity.COMMON.getColor() + "><!italic>Wooden Axe"))
            .material(Material.WOODEN_AXE)
            .dToolType(DToolType.AXE)
            .rarity(Rarity.COMMON)
            .stackSize(1)
            .noDurability()
            .strength(1)
            .efficiency(1.5f)
    ));

    private static DTool register(DTool dTool) {
        return Registries.TOOLS.register(dTool.getId(), dTool);
    }
}