package net.qilla.destructible.mining.block;


import net.qilla.destructible.mining.item.ItemDrops;
import net.qilla.destructible.mining.item.ToolType;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.HashMap;

public class DestructibleBlocks {

    private static HashMap<Material, DestructibleBlock> BLOCK_REGISTRY = new HashMap<>();

    public static final DestructibleBlock NONE = new DestructibleBlock(DestructibleBlock.Properties.of()
            .noDrops()
            .noTools());

    public static final DestructibleBlock STONE = register(Material.STONE, new DestructibleBlock(DestructibleBlock.Properties.of()
            .strengthRequirement(3)
            .instaBreak()
            .properTools(new ToolType[]{ToolType.PICKAXE})
            .itemDrops(ItemDrops.STONE)
            .sound(Sound.BLOCK_STONE_BREAK)
            .particle(Material.STONE)));

    public static final DestructibleBlock SAND = register(Material.SAND, new DestructibleBlock(DestructibleBlock.Properties.of()
            .strengthRequirement(0)
            .instaBreak()
            .properTools(new ToolType[]{ToolType.ANY})
            .itemDrops(ItemDrops.SAND)
            .sound(Sound.BLOCK_SAND_BREAK)
            .particle(Material.SAND)));

    public static final DestructibleBlock GRAVEL = register(Material.GRAVEL, new DestructibleBlock(DestructibleBlock.Properties.of()
            .strengthRequirement(1)
            .durability(20)
            .properTools(new ToolType[]{ToolType.ANY})
            .itemDrops(ItemDrops.GRAVEL)
            .sound(Sound.BLOCK_GRAVEL_BREAK)
            .particle(Material.GRAVEL)));

    public static final DestructibleBlock COBBLESTONE = register(Material.COBBLESTONE, new DestructibleBlock(DestructibleBlock.Properties.of()
            .strengthRequirement(1)
            .durability(15)
            .properTools(new ToolType[]{ToolType.PICKAXE})
            .itemDrops(ItemDrops.COBBLESTONE)
            .sound(Sound.BLOCK_STONE_BREAK)
            .particle(Material.COBBLESTONE)));

    public static final DestructibleBlock DIAMOND_ORE = register(Material.DIAMOND_ORE, new DestructibleBlock(DestructibleBlock.Properties.of()
            .strengthRequirement(3)
            .durability(100)
            .properTools(new ToolType[]{ToolType.PICKAXE})
            .itemDrops(ItemDrops.DIAMOND_ORE)
            .sound(Sound.BLOCK_STONE_BREAK)
            .particle(Material.DIAMOND_ORE)));

    private static DestructibleBlock register(final Material id, final DestructibleBlock block) {
        return BLOCK_REGISTRY.put(id, block);
    }

    public static DestructibleBlock getBlock(final Material id) {
        return BLOCK_REGISTRY.get(id);
    }

    public static boolean hasBlock(final Material id) {
        return BLOCK_REGISTRY.containsKey(id);
    }
}
