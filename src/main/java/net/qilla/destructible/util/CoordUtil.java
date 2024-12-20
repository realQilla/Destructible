package net.qilla.destructible.util;

import net.minecraft.core.BlockPos;
import org.bukkit.Location;

public final class CoordUtil {

    public static int getPosInChunk(int x, int y, int z) {
        return ((x & 15) << 8) | ((z & 15) << 4) | (y & 15);
    }

    public static int getPosInChunk(final Location loc) {
        return ((loc.getBlockX() & 15) << 8) | ((loc.getBlockZ() & 15) << 4) | (loc.getBlockY() & 15);
    }

    public static int getPosInChunk(final BlockPos blockPos) {
        return ((blockPos.getX() & 15) << 8) | ((blockPos.getZ() & 15) << 4) | (blockPos.getY() & 15);
    }
}