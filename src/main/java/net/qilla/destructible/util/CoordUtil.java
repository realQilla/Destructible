package net.qilla.destructible.util;

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.qilla.destructible.data.ChunkPos;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

public final class CoordUtil {

    public static int toChunkInt(@NotNull BlockPos blockPos) {
        Preconditions.checkNotNull(blockPos, "BlockPos cannot be null");
        return ((blockPos.getX() & 15) << 8) | ((blockPos.getZ() & 15) << 4) | (blockPos.getY() & 15);
    }

    @NotNull
    public static BlockPos toBlockPos(@NotNull ChunkPos chunkPos, int chunkInt) {
        Preconditions.checkNotNull(chunkPos, "ChunkPos cannot be null");
        int x = (chunkInt >> 8) & 15;
        int z = (chunkInt >> 4) & 15;
        int y = chunkInt & 15;
        return new BlockPos(chunkPos.getX() * 16 + x, chunkPos.getY() * 16 + y, chunkPos.getZ() * 16 + z);
    }

    @NotNull
    public static BlockPos toBlockPos(@NotNull Location loc) {
        Preconditions.checkNotNull(loc, "Location cannot be null");
        return new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    @NotNull
    public static BlockPos toBlockPos(@NotNull Block block) {
        Preconditions.checkNotNull(block, "Block cannot be null");
        return new BlockPos(block.getX(), block.getY(), block.getZ());
    }

    @NotNull
    public static Location toLoc(@NotNull BlockPos blockPos, @NotNull World world) {
        Preconditions.checkNotNull(blockPos, "BlockPos cannot be null");
        Preconditions.checkNotNull(world, "World cannot be null");
        return new Location(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @NotNull
    public static Block toBlock(@NotNull BlockPos blockPos, @NotNull World world) {
        Preconditions.checkNotNull(blockPos, "BlockPos cannot be null");
        Preconditions.checkNotNull(world, "World cannot be null");
        return world.getBlockAt(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }
}