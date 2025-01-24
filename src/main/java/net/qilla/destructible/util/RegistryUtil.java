package net.qilla.destructible.util;

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.data.registry.DRegistryMaster;
import net.qilla.destructible.mining.block.BlockMemory;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RegistryUtil {

    private static final Map<UUID, DPlayer> DPLAYERS = DRegistry.DPLAYERS;
    private static final Map<Long, ConcurrentHashMap<Integer, String>> LOADED_BLOCKS = DRegistry.LOADED_BLOCKS;
    private static final Map<Long, ConcurrentHashMap<Integer, BlockMemory>> LOADED_BLOCKS_MEMORY = DRegistry.LOADED_BLOCK_MEMORY;
    private static final Map<String, ConcurrentHashMap<Long, Set<Integer>>> LOADED_BLOCKS_GROUPED = DRegistry.LOADED_BLOCKS_GROUPED;

    private RegistryUtil() {
    }

    public static boolean registerPlayer(@NotNull Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;

        DPLAYERS.compute(player.getUniqueId(), (uuid, existingDPlayer) -> {
            if(existingDPlayer != null) {
                existingDPlayer.resetCraftPlayer(craftPlayer);
                return existingDPlayer;
            }
            return new DPlayer(Destructible.getInstance(), craftPlayer);
        });

        return true;
    }

    public static boolean unregisterPlayer(Player player) {
        var registry = DPLAYERS;
        if(!registry.containsKey(player.getUniqueId())) return false;
        registry.remove(player.getUniqueId());
        return true;
    }

    public static boolean loadBlock(@NotNull BlockPos blockPos, @NotNull String blockID) {
        return loadBlock(CoordUtil.getChunkKey(blockPos), CoordUtil.getBlockIndexInChunk(blockPos), blockID);
    }

    public static boolean unloadBlock(@NotNull BlockPos blockPos) {
        return unloadBlock(CoordUtil.getChunkKey(blockPos), CoordUtil.getBlockIndexInChunk(blockPos));
    }

    public static boolean loadBlock(long chunkKey, int chunkInt, @NotNull String blockId) {
        Preconditions.checkNotNull(blockId, "DBlock cannot be null");
        var chunkMap = LOADED_BLOCKS.computeIfAbsent(chunkKey, c -> new ConcurrentHashMap<>());

        if(chunkMap.containsKey(chunkInt)) return false;

        chunkMap.put(chunkInt,blockId);
        LOADED_BLOCKS_GROUPED.computeIfAbsent(blockId, id -> new ConcurrentHashMap<>())
                .computeIfAbsent(chunkKey, pos -> new HashSet<>())
                .add(chunkInt);
        return true;
    }

    public static boolean unloadBlock(long chunkKey, int chunkInt) {
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