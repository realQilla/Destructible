package net.qilla.destructible.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.qilla.destructible.data.ChunkPos;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.block.DBlocks;
import org.jetbrains.annotations.NotNull;

public final class DBlockUtil {

    public static DBlock getDBlock(@NotNull BlockPos blockPos) {
        ChunkPos chunkPos = new ChunkPos(blockPos);
        int chunkInt = CoordUtil.posToChunkLocalPos(blockPos);

        var loadedBlocks = Registries.LOADED_DESTRUCTIBLE_BLOCKS.computeIfPresent(chunkPos, (k, v) -> v);
        if(loadedBlocks == null) return DBlocks.DEFAULT;
        String blockString = loadedBlocks.computeIfPresent(chunkInt, (k2, v2) -> v2);
        return Registries.DESTRUCTIBLE_BLOCKS.getOrDefault(blockString, DBlocks.DEFAULT);
    }

    public static DBlock getDBlock(@NotNull ChunkPos chunkPos, int chunkInt) {
        var blockCache = Registries.LOADED_DESTRUCTIBLE_BLOCKS.computeIfPresent(chunkPos, (k, v) -> v);
        if(blockCache == null) return DBlocks.DEFAULT;
        String blockString = blockCache.computeIfPresent(chunkInt, (k2, v2) -> v2);
        return blockString == null ? DBlocks.DEFAULT : Registries.DESTRUCTIBLE_BLOCKS.getOrDefault(blockString, DBlocks.DEFAULT);
    }

    public static Vec3 getCenterFaceParticle(@NotNull final Direction dir) {
        Vec3 origin = dir.getUnitVec3();
        double offset = 0.65;
        return switch(dir) {
            case UP -> new Vec3(origin.x, offset, origin.z);
            case DOWN -> new Vec3(origin.x, -offset, origin.z);
            case EAST -> new Vec3(offset, origin.y, origin.z);
            case WEST -> new Vec3(-offset, origin.y, origin.z);
            case NORTH -> new Vec3(origin.x, origin.y, -offset);
            case SOUTH -> new Vec3(origin.x, origin.y, offset);
        };
    }

    public static Vec3 getFaceCenterItem(@NotNull final Direction dir) {
        Vec3 origin = dir.getUnitVec3();
        double offset = 0.65;
        return switch(dir) {
            case UP -> new Vec3(origin.x, offset - 0.05, origin.z);
            case DOWN -> new Vec3(origin.x, -offset - 0.2, origin.z);
            case EAST -> new Vec3(offset, origin.y - 0.2, origin.z);
            case WEST -> new Vec3(-offset, origin.y - 0.2, origin.z);
            case NORTH -> new Vec3(origin.x, origin.y - 0.2, -offset);
            case SOUTH -> new Vec3(origin.x, origin.y - 0.2, offset);
        };
    }

    public static float[] getFlatOffsetParticles(@NotNull final Direction dir) {
        return switch(dir) {
            case UP, DOWN -> new float[]{0.25f, 0.05f, 0.25f};
            case EAST, WEST -> new float[]{0.05f, 0.25f, 0.25f};
            case NORTH, SOUTH -> new float[]{0.25f, 0.25f, 0.05f};
        };
    }
}