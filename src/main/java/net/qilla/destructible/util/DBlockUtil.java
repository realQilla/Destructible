package net.qilla.destructible.util;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public final class DBlockUtil {

    public static Vec3 getMidFace(@NotNull final Direction dir) {
        Vec3 origin = dir.getUnitVec3();
        double offset = 0.5;
        return switch(dir) {
            case UP -> new Vec3(origin.x, offset, origin.z);
            case DOWN -> new Vec3(origin.x, -offset, origin.z);
            case EAST -> new Vec3(offset, origin.y, origin.z);
            case WEST -> new Vec3(-offset, origin.y, origin.z);
            case NORTH -> new Vec3(origin.x, origin.y, -offset);
            case SOUTH -> new Vec3(origin.x, origin.y, offset);
        };
    }

    public static Vec3 getMidFaceItem(@NotNull final Direction dir) {
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

    public static float[] getOffsetFace(@NotNull final Direction dir) {
        return switch(dir) {
            case UP, DOWN -> new float[] {0.25f, 0.05f, 0.25f};
            case EAST, WEST -> new float[] {0.05f, 0.25f, 0.25f};
            case NORTH, SOUTH -> new float[] {0.25f, 0.25f, 0.05f};
        };
    }
}
