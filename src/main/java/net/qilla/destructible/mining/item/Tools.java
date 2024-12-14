package net.qilla.destructible.mining.item;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;

public class Tools {
    public static final Tool WOODEN_PICKAXE = register(
            new PickaxeTool("wooden_pickaxe",
                    MiniMessage.miniMessage().deserialize("<!italic><yellow>Wooden Pickaxe"),
                    Material.WOODEN_PICKAXE,
                    Rarity.COMMON,
                    0,
                    0,
                    1,
                    1));

    public static final Tool IRON_PICKAXE = register(
            new PickaxeTool("iron_pickaxe",
                    MiniMessage.miniMessage().deserialize("<!italic><blue>Iron Pickaxe"),
                    Material.IRON_PICKAXE,
                    Rarity.RARE,
                    0,
                    0,
                    2,
                    2
    ));

    public static final Tool DIAMOND_PICKAXE = register(
            new PickaxeTool("diamond_pickaxe",
                    MiniMessage.miniMessage().deserialize("<!italic><aqua>Diamond Pickaxe"),
                    Material.DIAMOND_PICKAXE,
                    Rarity.RARE,
                    0,
                    0,
                    3,
                    3
            ));

    public static final Tool WOODEN_AXE = register(
            new AxeTool("wooden_axe",
            MiniMessage.miniMessage().deserialize("<!italic><Yellow>Wooden Axe"),
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