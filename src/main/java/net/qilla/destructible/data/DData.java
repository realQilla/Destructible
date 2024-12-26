package net.qilla.destructible.data;

import com.google.common.util.concurrent.AtomicDouble;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.block.DBlocks;
import net.qilla.destructible.mining.item.tool.DTool;
import net.qilla.destructible.mining.item.tool.DTools;
import net.qilla.destructible.util.CoordUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public final class DData {

    private final Location location;
    private final BlockPos blockPos;
    private final ChunkPos chunkPos;
    private final int chunkInt;
    private final Direction direction;
    private volatile DBlock dBlock;
    private volatile AtomicDouble totalDurability;
    private volatile AtomicDouble currentDurability;
    private final AtomicInteger crackLevel;
    private volatile DTool dTool;

    public DData(@NotNull final World world, @NotNull final BlockPos blockPos, @NotNull final ChunkPos chunkPos, final int chunkInt, @NotNull final Direction direction) {
        this.location = CoordUtil.blockPosToLoc(blockPos, world);
        this.blockPos = blockPos;
        this.chunkPos = chunkPos;
        this.chunkInt = chunkInt;
        this.direction = direction;
        this.dBlock = DBlocks.NONE;
        this.totalDurability = new AtomicDouble(dBlock.getDurability());
        this.currentDurability = new AtomicDouble(totalDurability.get());
        this.crackLevel = new AtomicInteger(0);
        this.dTool = DTools.DEFAULT;
    }

    @NotNull
    public Location getLocation() {
        return this.location;
    }

    @NotNull
    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    @NotNull
    public ChunkPos getChunkPos() {
        return this.chunkPos;
    }

    public int getChunkInt() {
        return this.chunkInt;
    }

    @NotNull
    public Direction getDirection() {
        return this.direction;
    }

    @NotNull
    public DBlock getDBlock() {
        return this.dBlock;
    }

    public float getTotalDurability() {
        return this.totalDurability.floatValue();
    }

    public float getCurrentDurability() {
        return this.currentDurability.floatValue();
    }

    public boolean isBroken() {
        return this.currentDurability.floatValue() <= 0;
    }

    public int getCrackLevel() {
        return crackLevel.get();
    }

    @NotNull
    public DTool getDTool() {
        return this.dTool;
    }

    public void setDBlock(final DBlock dBlock) {
        this.dBlock = dBlock;
        this.totalDurability.set(dBlock.getDurability());
        this.currentDurability.set(dBlock.getDurability());
        this.crackLevel.set(0);
    }

    public void damageBlock(float amount) {
        this.currentDurability.addAndGet(-amount);
        this.crackLevel.set(Math.round(((totalDurability.floatValue() - currentDurability.floatValue()) * 9 / totalDurability.floatValue())));
    }

    public void setDTool(final DTool dTool) {
        this.dTool = dTool;
    }
}