package net.qilla.destructible.util;

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import java.util.HashSet;
import java.util.Set;

public final class CoordUtil {

    public static int getBlockIndexInChunk(@NotNull BlockPos blockPos) {
        Preconditions.checkNotNull(blockPos, "BlockPos cannot be null");
        return ((blockPos.getX() & 15) << 8) | ((blockPos.getZ() & 15) << 4) | (blockPos.getY() & 15);
    }

    public static long getChunkKey(int chunkX, int chunkY, int chunkZ) {
        return (((long) chunkX & 0x1FFFFF) << 42)
                | (((long) chunkY & 0x1FFFFF) << 21)
                | ((long) chunkZ & 0x1FFFFF);
    }

    public static long getChunkKey(@NotNull BlockPos blockPos) {
        Preconditions.checkNotNull(blockPos, "BlockPos cannot be null");
        return getChunkKey(blockPos.getX() >> 4, blockPos.getY() >> 4, blockPos.getZ() >> 4);
    }

    public static Set<Long> getYChunkKeys(int chunkX, int chunkZ) {
        Set<Long> chunkKeys = new HashSet<>();
        for(int chunkY = -4; chunkY <= 19; chunkY++) {
            chunkKeys.add(getChunkKey(chunkX, chunkY, chunkZ));
        }
        return chunkKeys;
    }

    @NotNull
    public static BlockPos getBlockPos(long chunkKey, int chunkInt) {
        int chunkX = (int) ((chunkKey >> 42) & 0x1FFFFF);
        int chunkY = (int) ((chunkKey >> 21) & 0x1FFFFF);
        int chunkZ = (int) (chunkKey & 0x1FFFFF);

        if (chunkX >= 0x100000) chunkX -= 0x200000;
        if (chunkY >= 0x100000) chunkY -= 0x200000;
        if (chunkZ >= 0x100000) chunkZ -= 0x200000;

        int localX = (chunkInt >> 8) & 15;
        int localZ = (chunkInt >> 4) & 15;
        int localY = chunkInt & 15;

        int worldX = chunkX * 16 + localX;
        int worldZ = chunkZ * 16 + localZ;
        int worldY = chunkY * 16 + localY;

        return new BlockPos(worldX, worldY, worldZ);
    }

    @NotNull
    public static BlockPos getBlockPos(@NotNull Location loc) {
        Preconditions.checkNotNull(loc, "Location cannot be null");
        return new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    @NotNull
    public static BlockPos getBlockPos(@NotNull Block block) {
        Preconditions.checkNotNull(block, "Block cannot be null");
        return new BlockPos(block.getX(), block.getY(), block.getZ());
    }

    @NotNull
    public static Location getLoc(@NotNull BlockPos blockPos, @NotNull World world) {
        Preconditions.checkNotNull(blockPos, "BlockPos cannot be null");
        Preconditions.checkNotNull(world, "World cannot be null");
        return new Location(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @NotNull
    public static Block getBlock(@NotNull BlockPos blockPos, @NotNull World world) {
        Preconditions.checkNotNull(blockPos, "BlockPos cannot be null");
        Preconditions.checkNotNull(world, "World cannot be null");
        return world.getBlockAt(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }
}