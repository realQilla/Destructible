package net.qilla.destructible.mining.block;


import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.item.DDrops;
import net.qilla.destructible.mining.item.tool.DToolType;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.List;
import java.util.function.Function;

public final class DBlocks {

    public static final DBlock NONE = new DBlock(DBlock.Properties.of()
            .noDrops()
            .noTools()
            .neverBreak()
    );

    public static final DBlock STONE = register("STONE",
            DBlock::new, DBlock.Properties.of()
                    .strengthRequirement(0)
                    .durability(150)
                    .properTools(List.of(DToolType.PICKAXE))
                    .itemDrops(List.of(DDrops.COBBLESTONE))
                    .sound(Sound.BLOCK_STONE_BREAK)
                    .particle(Material.STONE)
    );

    public static final DBlock CACTUS = register("CACTUS",
            DBlock::new, DBlock.Properties.of()
                    .strengthRequirement(1)
                    .durability(60)
                    .properTools(List.of(DToolType.AXE))
                    .itemDrops(List.of(DDrops.CACTUS))
                    .sound(Sound.ENTITY_DOLPHIN_DEATH)
                    .particle(Material.CACTUS)
    );

    public static final DBlock SAND = register("SAND",
            DBlock::new, DBlock.Properties.of()
                    .strengthRequirement(0)
                    .instaBreak()
                    .properTools(List.of(DToolType.SHOVEL))
                    .itemDrops(List.of(DDrops.SAND))
                    .sound(Sound.BLOCK_SAND_BREAK)
                    .particle(Material.SAND)
    );

    public static final DBlock RED_SAND = register("RED_SAND",
            DBlock::new, DBlock.Properties.of()
                    .strengthRequirement(0)
                    .instaBreak()
                    .properTools(List.of(DToolType.SHOVEL))
                    .itemDrops(List.of(DDrops.RED_SAND))
                    .sound(Sound.BLOCK_SAND_BREAK)
                    .particle(Material.RED_SAND)
    );

    public static final DBlock GRAVEL = register("GRAVEL",
            DBlock::new, DBlock.Properties.of()
                    .strengthRequirement(1)
                    .durability(20)
                    .properTools(List.of(DToolType.SHOVEL))
                    .itemDrops(List.of(DDrops.GRAVEL))
                    .sound(Sound.BLOCK_GRAVEL_BREAK)
                    .particle(Material.GRAVEL)
    );

    public static final DBlock HAY_BLOCK = register("HAY_BLOCK",
            DBlock::new, DBlock.Properties.of()
                    .strengthRequirement(0)
                    .neverBreak()
                    .properTools(List.of(DToolType.ANY))
                    .noDrops()
                    .sound(Sound.BLOCK_GRASS_BREAK)
                    .particle(Material.HAY_BLOCK)
    );

    public static final DBlock COBBLESTONE = register("COBBLESTONE",
            DBlock::new, DBlock.Properties.of()
                    .strengthRequirement(1)
                    .durability(120)
                    .properTools(List.of(DToolType.PICKAXE))
                    .itemDrops(List.of(DDrops.COBBLESTONE))
                    .sound(Sound.BLOCK_STONE_BREAK)
                    .particle(Material.COBBLESTONE)
    );

    public static final DBlock OAK_PLANK = register("OAK_PLANKS",
            DBlock::new, DBlock.Properties.of()
                    .strengthRequirement(0)
                    .durability(40)
                    .properTools(List.of(DToolType.ANY))
                    .itemDrops(List.of(DDrops.OAK_PLANK))
                    .sound(Sound.BLOCK_WOOD_BREAK)
                    .particle(Material.OAK_PLANKS)
    );

    public static final DBlock OAK_LOG = register("OAK_LOG",
            DBlock::new,
            DBlock.Properties.of()
                    .strengthRequirement(1)
                    .durability(80)
                    .properTools(List.of(DToolType.AXE))
                    .itemDrops(List.of(DDrops.OAK_LOG))
                    .sound(Sound.BLOCK_WOOD_BREAK)
                    .particle(Material.OAK_LOG)
    );

    public static final DBlock DIAMOND_ORE = register("DIAMOND_ORE",
            DBlock::new,
            DBlock.Properties.of()
                    .strengthRequirement(3)
                    .durability(100)
                    .properTools(List.of(DToolType.PICKAXE))
                    .noDrops()
                    .sound(Sound.BLOCK_STONE_BREAK)
                    .particle(Material.DIAMOND_ORE)
    );

    private static DBlock register(final String id, final Function<DBlock.Properties, DBlock> factory, DBlock.Properties properties) {
        return Registries.DBLOCKS.put(id, factory.apply(properties.id(id)));
    }
}