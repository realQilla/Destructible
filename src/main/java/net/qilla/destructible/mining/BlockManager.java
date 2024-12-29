package net.qilla.destructible.mining;

import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.block.DBlocks;
import org.jetbrains.annotations.NotNull;

public class BlockManager {

    public DBlock getDBlock(@NotNull BlockInstance blockInstance) {
        var blockCache = Registries.DESTRUCTIBLE_BLOCKS_CACHE.computeIfPresent(blockInstance.getChunkPos(), (k, v) -> v);
        if(blockCache == null) return DBlocks.DEFAULT;
        String blockString = blockCache.computeIfPresent(blockInstance.getChunkInt(), (k2, v2) -> v2);
        return blockString == null ? DBlocks.DEFAULT : Registries.DESTRUCTIBLE_BLOCKS.getOrDefault(blockString, DBlocks.DEFAULT);
    }
}
