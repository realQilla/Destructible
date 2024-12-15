package net.qilla.destructible.mining.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;

public class Tools {
    public static final Tool WOODEN_PICKAXE = register(
            new PickaxeTool("wooden_pickaxe",
                    MiniMessage.miniMessage().deserialize("<" + Rarity.COMMON.getColor() + "><!italic>Wooden Pickaxe"),
                    Material.WOODEN_PICKAXE,
                    Rarity.COMMON,
                    0,
                    0,
                    1,
                    2));

    public static final Tool STONE_PICKAXE = register(
            new PickaxeTool("stone_pickaxe",
                    MiniMessage.miniMessage().deserialize("<" + Rarity.UNUSUAL.getColor() + "><!italic>Wooden Pickaxe"),
                    Material.STONE_PICKAXE,
                    Rarity.UNUSUAL,
                    0,
                    0,
                    2,
                    4));

    public static final Tool IRON_PICKAXE = register(
            new PickaxeTool("iron_pickaxe",
                    MiniMessage.miniMessage().deserialize("<" + Rarity.RARE.getColor() + "><!italic>Iron Pickaxe"),
                    Material.IRON_PICKAXE,
                    Rarity.RARE,
                    0,
                    0,
                    3,
                    6
    ));

    public static final Tool DIAMOND_PICKAXE = register(
            new PickaxeTool("diamond_pickaxe",
                    MiniMessage.miniMessage().deserialize("<" + Rarity.LEGENDARY.getColor() + "><!italic>Diamond Pickaxe"),
                    Material.DIAMOND_PICKAXE,
                    Rarity.LEGENDARY,
                    0,
                    0,
                    3,
                    10
            ));

    public static final Tool WOODEN_AXE = register(
            new AxeTool("wooden_axe",
            MiniMessage.miniMessage().deserialize("<" + Rarity.COMMON.getColor() + "><!italic>Wooden Axe"),
            Material.WOODEN_AXE,
                    Rarity.COMMON,
            0,
            0,
            1,
            1));

    private static Tool register(Tool tool) {
        return ItemRegistry.getInstance().registerItem(tool.getId(), tool);
    }
}