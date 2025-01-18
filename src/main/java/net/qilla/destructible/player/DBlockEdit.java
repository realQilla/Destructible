package net.qilla.destructible.player;

import net.qilla.destructible.mining.block.DBlock;
import org.jetbrains.annotations.NotNull;

public class DBlockEdit {
    private final DPlayer dPlayer;
    private BlockHighlight blockHighlight;
    private DBlock dblock;
    private int recursionSize;

    public DBlockEdit(@NotNull DPlayer dPlayer) {
        this.dPlayer = dPlayer;
        this.dblock = null;
        this.recursionSize = 0;
    }

    @NotNull
    public BlockHighlight getBlockHighlight() {
        if(this.blockHighlight == null) this.blockHighlight = new BlockHighlight(this.dPlayer);
        return this.blockHighlight;
    }

    public DBlock getDblock() {
        return dblock;
    }

    public int getRecursionSize() {
        return recursionSize;
    }

    public void setDblock(DBlock dblock) {
        this.dblock = dblock;
    }

    public void setRecursionSize(int recursionSize) {
        this.recursionSize = recursionSize;
    }
}
