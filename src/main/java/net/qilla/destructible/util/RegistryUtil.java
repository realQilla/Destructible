package net.qilla.destructible.util;

import net.minecraft.core.BlockPos;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.DRegistry;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RegistryUtil {

    public static boolean registerPlayer(@NotNull Player player) {
        var registry = DRegistry.DESTRUCTIBLE_PLAYERS;
        CraftPlayer craftPlayer = (CraftPlayer) player;

        registry.compute(player.getUniqueId(), (uuid, existingDPlayer) -> {
            if(existingDPlayer != null) {
                existingDPlayer.resetCraftPlayer(craftPlayer);
                return existingDPlayer;
            }
            return new DPlayer(Destructible.getInstance(), craftPlayer);
        });

        return true;
    }

    public static boolean unregisterPlayer(Player player) {
        if(!DRegistry.DESTRUCTIBLE_PLAYERS.containsKey(player.getUniqueId())) return false;
        DRegistry.DESTRUCTIBLE_PLAYERS.remove(player.getUniqueId());
        return true;
    }

    public static boolean registerBlock(BlockPos blockPos, DBlock dBlock) {
        long chunkKey = CoordUtil.getChunkKey(blockPos);
        int chunkInt = CoordUtil.getBlockIndexInChunk(blockPos);

        var loadedBlocks = DRegistry.LOADED_DESTRUCTIBLE_BLOCKS;
        var groupedBlocks = DRegistry.LOADED_DESTRUCTIBLE_BLOCKS_GROUPED;

        var chunkMap = loadedBlocks.computeIfAbsent(chunkKey, c -> new ConcurrentHashMap<>());

        if(chunkMap.containsKey(chunkInt)) return false;

        chunkMap.put(chunkInt, dBlock.getId());
        groupedBlocks.computeIfAbsent(dBlock.getId(), id -> new ConcurrentHashMap<>())
                .computeIfAbsent(chunkKey, pos -> new HashSet<>())
                .add(chunkInt);
        return true;
    }

    public static boolean unregisterBlock(BlockPos blockPos) {
        long chunkKey = CoordUtil.getChunkKey(blockPos);
        int chunkInt = CoordUtil.getBlockIndexInChunk(blockPos);

        var loadedBlocks = DRegistry.LOADED_DESTRUCTIBLE_BLOCKS;
        var groupedBlocks = DRegistry.LOADED_DESTRUCTIBLE_BLOCKS_GROUPED;

        var chunkMap = loadedBlocks.get(chunkKey);
        if(chunkMap == null || !chunkMap.containsKey(chunkInt)) return false;

        String blockId = chunkMap.remove(chunkInt);
        if(chunkMap.isEmpty()) loadedBlocks.remove(chunkKey);

        groupedBlocks.computeIfPresent(blockId, (id, blockMap) -> {
            Set<Integer> localIndexes = blockMap.get(chunkKey);
            if(localIndexes != null) {
                localIndexes.remove(chunkInt);
                if(localIndexes.isEmpty()) blockMap.remove(chunkKey);
            }
            return blockMap.isEmpty() ? null : blockMap;
        });
        return true;
    }
}