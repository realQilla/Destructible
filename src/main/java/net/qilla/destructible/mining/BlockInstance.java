package net.qilla.destructible.mining;

import com.google.common.util.concurrent.AtomicDouble;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.qilla.destructible.data.ChunkPos;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.data.RegistryMap;
import net.qilla.destructible.mining.block.BlockMemory;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.block.DBlocks;
import net.qilla.destructible.util.CoordUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public final class BlockInstance {

    private final Location location;
    private final BlockPos blockPos;
    private final ChunkPos chunkPos;
    private final int chunkInt;
    private final Direction direction;
    private volatile DBlock dBlock;
    private final BlockMemory blockMemory;
    private final AtomicDouble totalDurability;
    private final AtomicDouble currentDurability;
    private final AtomicInteger crackLevel;

    public BlockInstance(@NotNull World world, @NotNull BlockPos blockPos, @NotNull ChunkPos chunkPos, int chunkInt, @NotNull Direction direction) {
        this.location = CoordUtil.blockPosToLoc(blockPos, world);
        this.blockPos = blockPos;
        this.chunkPos = chunkPos;
        this.chunkInt = chunkInt;
        this.direction = direction;
        this.dBlock = DBlocks.DEFAULT;
        this.blockMemory = Registries.DESTRUCTIBLE_BLOCK_DATA.computeIfAbsent(chunkPos, k ->
                new RegistryMap<>()).computeIfAbsent(chunkInt, k ->
                new BlockMemory());
        this.totalDurability = new AtomicDouble(dBlock.getBlockDurability());
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

    public void setDBlock(final DBlock dBlock) {
        this.dBlock = dBlock;
        this.totalDurability.set(dBlock.getBlockDurability());
        this.currentDurability.set(dBlock.getBlockDurability());
        this.crackLevel.set(0);
    }

    public void damageBlock(double amount) {
        this.currentDurability.addAndGet(-amount);
        this.crackLevel.set(Math.round(((totalDurability.floatValue() - currentDurability.floatValue()) * 9 / totalDurability.floatValue())));
    }
}