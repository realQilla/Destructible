package net.qilla.destructible.mining.player.data;

import net.minecraft.core.Direction;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.block.DBlocks;
import net.qilla.destructible.mining.item.tool.DTool;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MiningData {
    private final Location location;
    private final Direction direction;
    private DBlock dBlock;
    private final DTool dTool;
    private float durabilityTotal;
    private float durabilityRemaining;
    private int incrementProgress = 0;

    public MiningData(@NotNull final Location location, @NotNull final Direction dir, @NotNull DTool dTool) {
        this.location = location;
        this.direction = dir;
        this.dBlock = Registries.BLOCKS.get(this.location.getWorld().getBlockAt(this.location).getType());
        if(dBlock == null) dBlock = DBlocks.NONE;
        this.dTool = dTool;

        this.durabilityTotal = dBlock.getDurability();
        this.durabilityRemaining = durabilityTotal;
    }

    public boolean damage(float amount) {
        this.incrementProgress = Math.round(((durabilityTotal - durabilityRemaining) * 9 / durabilityTotal));
        return (this.durabilityRemaining -= amount) <= 0;
    }

    public void updateBlock() {
        this.dBlock = Registries.BLOCKS.get(this.location.getWorld().getBlockAt(this.location).getType());
        if(dBlock == null) this.dBlock = DBlocks.NONE;
        this.durabilityTotal = dBlock.getDurability();
        this.durabilityRemaining = durabilityTotal;
    }

    @NotNull
    public Location getLocation() {
        return this.location;
    }

    @Nullable
    public Direction getDirection() {
        return this.direction;
    }

    @Nullable
    public DBlock getDestructibleBlock() {
        return this.dBlock;
    }

    @NotNull
    public DTool getDTool() {
        return this.dTool;
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