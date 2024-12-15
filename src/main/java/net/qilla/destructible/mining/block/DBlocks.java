package net.qilla.destructible.mining.block;


import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.item.DDrops;
import net.qilla.destructible.mining.item.tool.DToolType;
import org.bukkit.Material;
import org.bukkit.Sound;

public class DBlocks {

    public static final DBlock NONE = new DBlock(DBlock.Properties.of()
            .noDrops()
            .noTools());

    public static final DBlock STONE = register(Material.STONE, new DBlock(DBlock.Properties.of()
            .strengthRequirement(0)
            .durability(20)
            .properTools(new DToolType[]{DToolType.PICKAXE})
            .itemDrops(DDrops.STONE)
            .sound(Sound.BLOCK_STONE_BREAK)
            .particle(Material.STONE)));

    public static final DBlock SAND = register(Material.SAND, new DBlock(DBlock.Properties.of()
            .strengthRequirement(0)
            .instaBreak()
            .properTools(new DToolType[]{DToolType.ALL})
            .itemDrops(DDrops.SAND)
            .sound(Sound.BLOCK_SAND_BREAK)
            .particle(Material.SAND)));

    public static final DBlock GRAVEL = register(Material.GRAVEL, new DBlock(DBlock.Properties.of()
            .strengthRequirement(1)
            .durability(20)
            .properTools(new DToolType[]{DToolType.PICKAXE})
            .itemDrops(DDrops.GRAVEL)
            .sound(Sound.BLOCK_GRAVEL_BREAK)
            .particle(Material.GRAVEL)));

    public static final DBlock COBBLESTONE = register(Material.COBBLESTONE, new DBlock(DBlock.Properties.of()
            .strengthRequirement(1)
            .durability(30)
            .properTools(new DToolType[]{DToolType.PICKAXE})
            .itemDrops(DDrops.COBBLESTONE)
            .sound(Sound.BLOCK_STONE_BREAK)
            .particle(Material.COBBLESTONE)));

    public static final DBlock DIAMOND_ORE = register(Material.DIAMOND_ORE, new DBlock(DBlock.Properties.of()
            .strengthRequirement(3)
            .durability(100)
            .properTools(new DToolType[]{DToolType.PICKAXE})
            .noDrops()
            .sound(Sound.BLOCK_STONE_BREAK)
            .particle(Material.DIAMOND_ORE)));

    private static DBlock register(final Material id, final DBlock block) {
        return Registries.BLOCKS.register(id, block);
    }
}
