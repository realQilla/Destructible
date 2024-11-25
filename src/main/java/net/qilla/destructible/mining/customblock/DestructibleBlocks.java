package net.qilla.destructible.mining.customblock;


import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class DestructibleBlocks {

    private static HashMap<Material, DestructibleBlock> BLOCK_REGISTRY = new HashMap<>();

    public static final DestructibleBlock NONE = new DestructibleBlock(DestructibleBlock.Properties.of()
            .noDrops()
            .noTools());

    public static final DestructibleBlock STONE = register(Material.STONE, new DestructibleBlock(DestructibleBlock.Properties.of()
            .instaBreak()
            .itemDrops(ItemDrops.STONE)
            .sound(Sound.BLOCK_STONE_BREAK)
            .blockParticle(Material.STONE)));

    public static final DestructibleBlock SAND = register(Material.SAND, new DestructibleBlock(DestructibleBlock.Properties.of()
            .instaBreak()
            .itemDrops(ItemDrops.SAND)
            .sound(Sound.BLOCK_SAND_BREAK)
            .blockParticle(Material.SAND)));

    public static final DestructibleBlock GRAVEL = register(Material.GRAVEL, new DestructibleBlock(DestructibleBlock.Properties.of()
            .itemDrops(ItemDrops.GRAVEL)
            .durability(20)
            .sound(Sound.BLOCK_GRAVEL_BREAK)
            .blockParticle(Material.GRAVEL)));

    public static final DestructibleBlock COBBLESTONE = register(Material.COBBLESTONE, new DestructibleBlock(DestructibleBlock.Properties.of()
            .itemDrops(ItemDrops.COBBLESTONE)
            .durability(80)
            .sound(Sound.BLOCK_STONE_BREAK)
            .blockParticle(Material.COBBLESTONE)));

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
