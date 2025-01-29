package net.qilla.destructible.command.temp;

import net.minecraft.core.BlockPos;

public class SelectedArea {

    private BlockPos firstPos;
    private BlockPos secondPos;

    public SelectedArea() {
    }

    public BlockPos getFirst() {
        return firstPos;
    }

    public BlockPos getSecond() {
        return secondPos;
    }

    public SelectedArea first(BlockPos firstPos) {
        this.firstPos = firstPos;
        return this;
    }

    public SelectedArea second(BlockPos secondPos) {
        this.secondPos = secondPos;
        return this;
    }

    public boolean isSelected() {
        return firstPos != null && secondPos != null;
    }

    public int size() {
        return (secondPos.getX() - firstPos.getX()) * (secondPos.getY() - firstPos.getY()) * (secondPos.getZ() - firstPos.getZ());
    }
}
