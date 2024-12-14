package net.qilla.destructible.mining.player.data;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.qilla.destructible.mining.block.DestructibleBlock;
import net.qilla.destructible.mining.block.DestructibleBlocks;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public final class MiningData {
    private final Location location;
    private final Direction direction;
    private DestructibleBlock destructibleBlock;
    private float durabilityTotal;
    private float durabilityRemaining;
    private int incrementProgress = 0;

    public MiningData(@NotNull final Location location, @NotNull final Direction dir) {
        this.location = location;
        this.direction = dir;
        this.destructibleBlock = DestructibleBlocks.getBlock(this.location.getWorld().getBlockAt(this.location).getType());
        if(destructibleBlock == null) destructibleBlock = DestructibleBlocks.NONE;

        this.durabilityTotal = destructibleBlock.getDurability();
        this.durabilityRemaining = durabilityTotal;
    }

    public boolean damage(int amount) {
        this.incrementProgress = Math.round(((durabilityTotal - durabilityRemaining) * 9 / durabilityTotal));
        return (this.durabilityRemaining -= amount) <= 0;
    }

    public void updateBlock() {
        this.destructibleBlock = DestructibleBlocks.getBlock(this.location.getWorld().getBlockAt(this.location).getType());
        if(destructibleBlock == null) this.destructibleBlock = DestructibleBlocks.NONE;
        this.durabilityTotal = destructibleBlock.getDurability();
        this.durabilityRemaining = durabilityTotal;
    }

    public Location getLocation() {
        return this.location;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public DestructibleBlock getDestructibleBlock() {
        return this.destructibleBlock;
    }


    public float getDurabilityTotal() {
        return this.durabilityTotal;
    }

    public float getDurabilityRemaining() {
        return this.durabilityRemaining;
    }

    public int getIncrementProgress() {
        return incrementProgress;
    }
}