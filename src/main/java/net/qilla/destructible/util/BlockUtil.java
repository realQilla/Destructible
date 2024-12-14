package net.qilla.destructible.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class BlockUtil {

    public static Vec3 getMiddleFace(@NotNull final Direction dir) {
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

    public static BlockPos locToBlockPos(@NotNull final Location loc) {
        return new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public static Location blockPosToLoc(@NotNull BlockPos blockPos, World world) {
        return new Location(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }
}
