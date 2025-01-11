package net.qilla.destructible.player;

import net.minecraft.core.BlockPos;
import net.qilla.destructible.data.ChunkPos;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.util.CoordUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class DBlockEdit {
    private final DPlayer dPlayer;
    private BlockHighlight blockHighlight;
    private DBlock dblock;
    private boolean recursive;
    private int recursionSize;

    public DBlockEdit(@NotNull DPlayer dPlayer) {
        this.dPlayer = dPlayer;
        this.dblock = null;
        this.recursive = false;;
        this.recursionSize = 0;
    }

    @NotNull
    public BlockHighlight getBlockHighlight() {
        if(this.blockHighlight == null) this.blockHighlight = new BlockHighlight(this.dPlayer);
        return this.blockHighlight;
    }

    public DBlock getDblock() {
        return dblock;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public int getRecursionSize() {
        return recursionSize;
    }

    public void setDblock(DBlock dblock, boolean recursive, int recursionSize) {
        this.dblock = dblock;
        this.recursive = recursive;
        this.recursionSize = recursionSize;
    }

    public void loadBlock(@NotNull BlockPos blockPos, @NotNull String blockId) {
        ChunkPos chunkPos = new ChunkPos(blockPos);
        int chunkInt = CoordUtil.posToChunkLocalPos(blockPos);

        var loadedBlocks = Registries.LOADED_DESTRUCTIBLE_BLOCKS;
        var loadedBlocksGrouped = Registries.LOADED_DESTRUCTIBLE_BLOCKS_GROUPED;

        loadedBlocks.computeIfAbsent(chunkPos, chunkPosMap ->
                new ConcurrentHashMap<>()).put(chunkInt, blockId);
        loadedBlocksGrouped.computeIfAbsent(blockId, blockId2 ->
                        new ConcurrentHashMap<>())
                .computeIfAbsent(chunkPos, chunkPos2 ->
                        new HashSet<>()).add(chunkInt);
    }

    public void unloadBlock(@NotNull BlockPos blockPos) {
        ChunkPos chunkPos = new ChunkPos(blockPos);
        int chunkInt = CoordUtil.posToChunkLocalPos(blockPos);

        var loadedBlocks = Registries.LOADED_DESTRUCTIBLE_BLOCKS;
        var loadedBlocksGrouped = Registries.LOADED_DESTRUCTIBLE_BLOCKS_GROUPED;

        loadedBlocks.computeIfPresent(chunkPos, (chunkPos2, chunkIntMap) -> {
            chunkIntMap.remove(chunkInt);
            return chunkIntMap.isEmpty() ? null : chunkIntMap;
        });

        loadedBlocksGrouped.forEach((blockId, chunkPosMap) -> {
            chunkPosMap.computeIfPresent(chunkPos, (chunkPos2, chunkIntSet) -> {
                chunkIntSet.remove(chunkInt);
                return chunkIntSet.isEmpty() ? null : chunkIntSet;
            });
        });
    }
}
