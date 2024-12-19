package net.qilla.destructible.mining.block;


import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.item.DDrops;
import net.qilla.destructible.mining.item.tool.DToolType;
import org.bukkit.Material;
import org.bukkit.Sound;

public final class DBlocks {

    public static final DBlock NONE = new DBlock(DBlock.Properties.of()
            .noDrops()
            .noTools()
            .neverBreak()
    );

    public static final DBlock STONE = register(Material.STONE, new DBlock(DBlock.Properties.of()
            .strengthRequirement(0)
            .durability(150)
            .properTools(new DToolType[]{DToolType.PICKAXE})
            .itemDrops(DDrops.COBBLESTONE)
            .sound(Sound.BLOCK_STONE_BREAK)
            .particle(Material.STONE)));

    public static final DBlock SAND = register(Material.SAND, new DBlock(DBlock.Properties.of()
            .strengthRequirement(0)
            .instaBreak()
            .properTools(new DToolType[]{DToolType.SHOVEL})
            .itemDrops(DDrops.SAND)
            .sound(Sound.BLOCK_SAND_BREAK)
            .particle(Material.SAND))
    );

    public static final DBlock RED_SAND = register(Material.RED_SAND, new DBlock(DBlock.Properties.of()
            .strengthRequirement(0)
            .instaBreak()
            .properTools(new DToolType[]{DToolType.SHOVEL})
            .itemDrops(DDrops.RED_SAND)
            .sound(Sound.BLOCK_SAND_BREAK)
            .particle(Material.RED_SAND))
    );

    public static final DBlock GRAVEL = register(Material.GRAVEL, new DBlock(DBlock.Properties.of()
            .strengthRequirement(1)
            .durability(20)
            .properTools(new DToolType[]{DToolType.SHOVEL})
            .itemDrops(DDrops.GRAVEL)
            .sound(Sound.BLOCK_GRAVEL_BREAK)
            .particle(Material.GRAVEL))
    );

    public static final DBlock HAY_BLOCK = register(Material.HAY_BLOCK, new DBlock(DBlock.Properties.of()
            .strengthRequirement(0)
            .neverBreak()
            .properTools(new DToolType[]{DToolType.ANY})
            .noDrops()
            .sound(Sound.BLOCK_GRASS_BREAK)
            .particle(Material.HAY_BLOCK))
    );

    public static final DBlock COBBLESTONE = register(Material.COBBLESTONE, new DBlock(DBlock.Properties.of()
            .strengthRequirement(1)
            .durability(120)
            .properTools(new DToolType[]{DToolType.PICKAXE})
            .itemDrops(DDrops.COBBLESTONE)
            .sound(Sound.BLOCK_STONE_BREAK)
            .particle(Material.COBBLESTONE))
    );

    public static final DBlock OAK_PLANK = register(Material.OAK_PLANKS, new DBlock(DBlock.Properties.of()
            .strengthRequirement(0)
            .durability(40)
            .properTools(new DToolType[]{DToolType.ANY})
            .itemDrops(DDrops.OAK_PLANK)
            .sound(Sound.BLOCK_WOOD_BREAK)
            .particle(Material.OAK_PLANKS))
    );

    public static final DBlock OAK_LOG = register(Material.OAK_LOG, new DBlock(DBlock.Properties.of()
            .strengthRequirement(1)
            .durability(80)
            .properTools(new DToolType[]{DToolType.AXE})
            .itemDrops(DDrops.OAK_LOG)
            .sound(Sound.BLOCK_WOOD_BREAK)
            .particle(Material.OAK_LOG))
    );

    public static final DBlock DIAMOND_ORE = register(Material.DIAMOND_ORE, new DBlock(DBlock.Properties.of()
            .strengthRequirement(3)
            .durability(100)
            .properTools(new DToolType[]{DToolType.PICKAXE})
            .noDrops()
            .sound(Sound.BLOCK_STONE_BREAK)
            .particle(Material.DIAMOND_ORE))
    );

    private static DBlock register(final Material id, final DBlock block) {
        return Registries.BLOCKS.register(id, block);
    }
}