package net.qilla.destructible.mining;

import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.block.DBlocks;
import org.jetbrains.annotations.NotNull;

public class BlockManager {

    public DBlock getDBlock(@NotNull DData dData) {
        var blockCache = Registries.DESTRUCTIBLE_BLOCKS_CACHE.computeIfPresent(dData.getChunkPos(), (k, v) -> v);
        if(blockCache == null) return DBlocks.NONE;
        String blockString = blockCache.computeIfPresent(dData.getChunkInt(), (k2, v2) -> v2);
        return blockString == null ? DBlocks.NONE : Registries.DESTRUCTIBLE_BLOCKS.getOrDefault(blockString, DBlocks.NONE);
    }
}
