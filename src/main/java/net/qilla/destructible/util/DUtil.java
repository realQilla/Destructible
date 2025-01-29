package net.qilla.destructible.util;

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.DItems;
import net.qilla.qlibrary.util.tools.CoordUtil;
import org.jetbrains.annotations.NotNull;
import java.util.Map;
import java.util.Optional;

public final class DUtil {

    private static final Map<Long, Map<Integer, String>> LOADED_BLOCK_MAP = DRegistry.LOADED_BLOCKS;
    private static final Map<String, DBlock> DBLOCK_MAP = DRegistry.BLOCKS;
    private static final Map<String, DItem> ITEM_MAP = DRegistry.ITEMS;

    private DUtil() {
    }

    public static Optional<DBlock> getDBlock(@NotNull BlockPos blockPos) {
        Preconditions.checkNotNull(blockPos, "BlockPos cannot be null");

        long chunkKey = CoordUtil.getChunkKey(blockPos);
        int subChunkKey = CoordUtil.getSubChunkKey(blockPos);

        return Optional.ofNullable(LOADED_BLOCK_MAP.get(chunkKey))
                .map(chunkIntMap -> chunkIntMap.get(subChunkKey))
                .map(DBLOCK_MAP::get);
    }

    public static @NotNull DItem getDItem(@NotNull String itemID) {
        Preconditions.checkNotNull(itemID, "ItemID cannot be null");

        return ITEM_MAP.getOrDefault(itemID, DItems.MISSING_ITEM);
    }

    public static Vec3 getCenterOfFace(@NotNull Direction dir) {
        Preconditions.checkNotNull(dir, "Direction cannot be null");

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

    public static Vec3 getCenterOfFaceForItem(@NotNull Direction dir) {
        Preconditions.checkNotNull(dir, "Direction cannot be null");

        Vec3 origin = dir.getUnitVec3();
        double offset = 0.6;
        return switch(dir) {
            case UP -> new Vec3(origin.x, offset - 0.05, origin.z);
            case DOWN -> new Vec3(origin.x, -offset - 0.2, origin.z);
            case EAST -> new Vec3(offset, origin.y - 0.2, origin.z);
            case WEST -> new Vec3(-offset, origin.y - 0.2, origin.z);
            case NORTH -> new Vec3(origin.x, origin.y - 0.2, -offset);
            case SOUTH -> new Vec3(origin.x, origin.y - 0.2, offset);
        };
    }

    public static float[] getSideOffset(@NotNull Direction dir) {
        Preconditions.checkNotNull(dir, "Direction cannot be null");

        return switch(dir) {
            case UP, DOWN -> new float[]{0.25f, 0.020f, 0.25f};
            case EAST, WEST -> new float[]{0.020f, 0.25f, 0.25f};
            case NORTH, SOUTH -> new float[]{0.25f, 0.25f, 0.020f};
        };
    }
}