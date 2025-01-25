package net.qilla.destructible.mining;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AtomicDouble;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.qilla.destructible.mining.block.BlockMemory;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.util.RegistryUtil;
import net.qilla.qlibrary.util.tools.CoordUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public final class BlockInstance {

    private final Location location;
    private final BlockPos blockPos;
    private final long chunkKey;
    private final int chunkInt;
    private final Direction direction;
    private volatile DBlock dBlock;
    private final BlockMemory blockMemory;
    private final AtomicDouble totalDurability;
    private final AtomicDouble currentDurability;
    private final AtomicInteger crackLevel;

    public BlockInstance(@NotNull World world, @NotNull BlockPos blockPos, long chunkKey, int chunkInt, @NotNull DBlock dBlock, @NotNull Direction direction) {
        Preconditions.checkNotNull(world, "World cannot be null");
        Preconditions.checkNotNull(blockPos, "BlockPos cannot be null");
        Preconditions.checkNotNull(dBlock, "DBlock cannot be null");
        Preconditions.checkNotNull(direction, "Direction cannot be null");
        this.location = CoordUtil.getLoc(blockPos, world);
        this.blockPos = blockPos;
        this.chunkKey = chunkKey;
        this.chunkInt = chunkInt;
        this.dBlock = dBlock;
        this.direction = direction;
        this.blockMemory = RegistryUtil.getBlockMemory(chunkKey, chunkInt);
        this.totalDurability = new AtomicDouble(dBlock.getDurability());
        this.currentDurability = new AtomicDouble(totalDurability.get());
        this.crackLevel = new AtomicInteger(0);
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
    public Long getChunkKey() {
        return this.chunkKey;
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

    @NotNull
    public BlockMemory getDBlockData() {
        return this.blockMemory;
    }

    public float getTotalDurability() {
        return this.totalDurability.floatValue();
    }

    public float getCurrentDurability() {
        return this.currentDurability.floatValue();
    }

    public boolean isDestroyed() {
        return this.currentDurability.floatValue() <= 0;
    }

    public int getCrackLevel() {
        return crackLevel.get();
    }

    public void setDBlock(DBlock dBlock) {
        Preconditions.checkNotNull(dBlock, "DBlock cannot be null");
        this.dBlock = dBlock;
        this.totalDurability.set(dBlock.getDurability());
        this.currentDurability.set(dBlock.getDurability());
        this.crackLevel.set(0);
    }

    public void damageBlock(double amount) {
        this.currentDurability.addAndGet(-amount);
        this.crackLevel.set(Math.round(((totalDurability.floatValue() - currentDurability.floatValue()) * 9 / totalDurability.floatValue())));
    }
}