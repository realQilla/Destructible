package net.qilla.destructible.util;

import net.minecraft.core.BlockPos;
import net.qilla.destructible.data.ChunkPos;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public final class CoordUtil {

    public static int posToChunkLocalPos(int x, int y, int z) {
        return ((x & 15) << 8) | ((z & 15) << 4) | (y & 15);
    }

    public static int posToChunkLocalPos(final Location loc) {
        return ((loc.getBlockX() & 15) << 8) | ((loc.getBlockZ() & 15) << 4) | (loc.getBlockY() & 15);
    }

    public static int posToChunkLocalPos(final BlockPos blockPos) {
        return ((blockPos.getX() & 15) << 8) | ((blockPos.getZ() & 15) << 4) | (blockPos.getY() & 15);
    }

    public static BlockPos chunkIntToPos(int chunkInt, ChunkPos chunkPos) {
        int x = (chunkInt >> 8) & 15;
        int z = (chunkInt >> 4) & 15;
        int y = chunkInt & 15;
        return new BlockPos(chunkPos.getX() * 16 + x, chunkPos.getY() * 16 + y, chunkPos.getZ() * 16 + z);
    }

    public static BlockPos locToBlockPos(@NotNull final Location loc) {
        return new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public static Location blockPosToLoc(@NotNull BlockPos blockPos, @NotNull World world) {
        return new Location(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }
}