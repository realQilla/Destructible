package net.qilla.destructible.data;

import net.minecraft.core.BlockPos;
import org.bukkit.Location;

public class ChunkCoord {
    private final int x;
    private final int y;
    private final int z;

    public ChunkCoord(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public ChunkCoord(BlockPos blockPos) {
        this.x = blockPos.getX() >> 4;
        this.y = blockPos.getY() >> 4;
        this.z = blockPos.getZ() >> 4;
    }

    public ChunkCoord(Location loc) {
        this.x = loc.getBlockX() >> 4;
        this.y = loc.getBlockY() >> 4;
        this.z = loc.getBlockZ() >> 4;
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChunkCoord other = (ChunkCoord) obj;
        return this.x == other.x && this.y == other.y && this.z == other.z;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * this.x + this.y) + this.z;
    }
}
