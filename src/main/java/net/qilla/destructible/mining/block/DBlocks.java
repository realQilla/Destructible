package net.qilla.destructible.mining.block;


import net.qilla.destructible.data.Registries;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.function.Function;

public final class DBlocks {

    public static final DBlock DEFAULT = new DBlock.Builder()
            .blockMaterial(Material.STONE)
            .blockStrength(0)
            .infiniteDurability()
            .blockCooldown(0)
            .noCorrectTools()
            .noItemDrops()
            .breakSound(Sound.BLOCK_STEM_BREAK)
            .breakParticle(Material.BEDROCK).build();

    private static DBlock register(String id, Function<DBlock.Builder, DBlock> factory, DBlock.Builder builder) {
        return Registries.DESTRUCTIBLE_BLOCKS.put(id, factory.apply(builder.id(id)));
    }
}