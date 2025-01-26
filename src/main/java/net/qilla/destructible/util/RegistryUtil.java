package net.qilla.destructible.util;

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.mining.block.BlockMemory;
import net.qilla.qlibrary.util.tools.CoordUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RegistryUtil {

    private static final Map<Long, ConcurrentHashMap<Integer, String>> LOADED_BLOCKS = DRegistry.LOADED_BLOCKS;
    private static final Map<Long, ConcurrentHashMap<Integer, BlockMemory>> LOADED_BLOCKS_MEMORY = DRegistry.LOADED_BLOCK_MEMORY;
    private static final Map<String, ConcurrentHashMap<Long, Set<Integer>>> LOADED_BLOCKS_GROUPED = DRegistry.LOADED_BLOCKS_GROUPED;

    private RegistryUtil() {
    }

    public static boolean loadBlock(@NotNull BlockPos blockPos, @NotNull String blockID) {
        return loadBlock(CoordUtil.getChunkKey(blockPos), CoordUtil.getBlockIndexInChunk(blockPos), blockID);
    }

    public static boolean unloadBlock(@NotNull BlockPos blockPos) {
        return unloadBlock(CoordUtil.getChunkKey(blockPos), CoordUtil.getBlockIndexInChunk(blockPos));
    }

    public synchronized static boolean loadBlock(long chunkKey, int chunkInt, @NotNull String blockId) {
        Preconditions.checkNotNull(blockId, "DBlock cannot be null");

        var loadedBlocksChunkIntMap = LOADED_BLOCKS.computeIfAbsent(chunkKey, c -> new ConcurrentHashMap<>());
        var loadedBlocksGrouped = LOADED_BLOCKS_GROUPED;

        String previousID = loadedBlocksChunkIntMap.put(chunkInt, blockId);

        loadedBlocksGrouped.computeIfAbsent(blockId, id -> new ConcurrentHashMap<>())
                .computeIfAbsent(chunkKey, pos -> new HashSet<>())
                .add(chunkInt);

        if(previousID == null) return true;

        loadedBlocksGrouped.computeIfPresent(previousID, (id, chunkKeyMap) -> {
            chunkKeyMap.computeIfPresent(chunkKey, (chunkKey2, chunkIntSet) -> {
                chunkIntSet.remove(chunkInt);
                if(chunkIntSet.isEmpty()) return null;
                return chunkIntSet;
            });
            return chunkKeyMap.isEmpty() ? null : chunkKeyMap;
        });
        return true;
    }

    public synchronized static boolean unloadBlock(long chunkKey, int chunkInt) {
        var loadedBlocks = LOADED_BLOCKS;

        var chunkMap = loadedBlocks.get(chunkKey);
        if(chunkMap == null || !chunkMap.containsKey(chunkInt)) return false;

        String blockId = chunkMap.remove(chunkInt);

        if(chunkMap.isEmpty()) loadedBlocks.remove(chunkKey);

        LOADED_BLOCKS_GROUPED.computeIfPresent(blockId, (id, blockMap) -> {
            Set<Integer> localIndexes = blockMap.get(chunkKey);
            if(localIndexes != null) {
                localIndexes.remove(chunkInt);
                if(localIndexes.isEmpty()) blockMap.remove(chunkKey);
            }
            return blockMap.isEmpty() ? null : blockMap;
        });
        return true;
    }

    public static Map<Long, Set<Integer>> getBlocks(String blockId) {
        return Collections.unmodifiableMap(LOADED_BLOCKS_GROUPED.getOrDefault(blockId, new ConcurrentHashMap<>()));
    }

    public static Map<Long, Set<Integer>> getBlocks(Set<String> blockIDSet) {
        Map<Long, Set<Integer>> groupedBlocks = new ConcurrentHashMap<>();
        blockIDSet.forEach(blockID -> groupedBlocks.putAll(getBlocks(blockID)));

        return Collections.unmodifiableMap(groupedBlocks);
    }

    public static String getBlockID(long chunkKey, int chunkInt) {
        return LOADED_BLOCKS.getOrDefault(chunkKey, new ConcurrentHashMap<>()).get(chunkInt);
    }

    public static BlockMemory getBlockMemory(long chunkKey, int chunkInt) {
        return LOADED_BLOCKS_MEMORY.computeIfAbsent(chunkKey, k ->
                new ConcurrentHashMap<>()).computeIfAbsent(chunkInt, k ->
                new BlockMemory());
    }
}