package net.qilla.destructible.util;

import net.minecraft.core.BlockPos;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.ChunkPos;
import net.qilla.destructible.data.DRegistry;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RegistryUtil {

    public static boolean registerPlayer(Player player) {
        var registry = DRegistry.DESTRUCTIBLE_PLAYERS;
        CraftPlayer craftPlayer = (CraftPlayer) player;

        registry.compute(player.getUniqueId(), (uuid, existingDPlayer) -> {
            if(existingDPlayer != null) {
                existingDPlayer.resetCraftPlayer(craftPlayer);
                return existingDPlayer;
            }
            return new DPlayer(craftPlayer, Destructible.getInstance());
        });

        return true;
    }

    public static boolean unregisterPlayer(Player player) {
        if(!DRegistry.DESTRUCTIBLE_PLAYERS.containsKey(player.getUniqueId())) return false;
        DRegistry.DESTRUCTIBLE_PLAYERS.remove(player.getUniqueId());
        return true;
    }

    public static boolean registerBlock(BlockPos blockPos, DBlock dBlock) {
        ChunkPos chunkPos = ChunkPos.of(blockPos);
        int chunkInt = CoordUtil.toChunkInt(blockPos);

        var loadedBlocks = DRegistry.LOADED_DESTRUCTIBLE_BLOCKS;
        var groupedBlocks = DRegistry.LOADED_DESTRUCTIBLE_BLOCKS_GROUPED;

        var chunkMap = loadedBlocks.computeIfAbsent(chunkPos, c -> new ConcurrentHashMap<>());

        if(chunkMap.containsKey(chunkInt)) return false;

        chunkMap.put(chunkInt, dBlock.getId());
        groupedBlocks.computeIfAbsent(dBlock.getId(), id -> new ConcurrentHashMap<>())
                .computeIfAbsent(chunkPos, pos -> new HashSet<>())
                .add(chunkInt);
        return true;
    }

    public static boolean unregisterBlock(BlockPos blockPos) {
        ChunkPos chunkPos = ChunkPos.of(blockPos);
        int chunkInt = CoordUtil.toChunkInt(blockPos);

        var loadedBlocks = DRegistry.LOADED_DESTRUCTIBLE_BLOCKS;
        var groupedBlocks = DRegistry.LOADED_DESTRUCTIBLE_BLOCKS_GROUPED;

        var chunkMap = loadedBlocks.get(chunkPos);
        if(chunkMap == null || !chunkMap.containsKey(chunkInt)) return false;

        String blockId = chunkMap.remove(chunkInt);
        if(chunkMap.isEmpty()) loadedBlocks.remove(chunkPos);

        groupedBlocks.computeIfPresent(blockId, (id, blockMap) -> {
            Set<Integer> blockInts = blockMap.get(chunkPos);
            if(blockInts != null) {
                blockInts.remove(chunkInt);
                if(blockInts.isEmpty()) blockMap.remove(chunkPos);
            }
            return blockMap.isEmpty() ? null : blockMap;
        });
        return true;
    }
}