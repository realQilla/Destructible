package net.qilla.destructible.player;

import net.qilla.destructible.mining.block.DBlock;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class BlockEdit {
    private final BlockHighlight blockHighlight;
    private DBlock dblock;
    private int recursionSize;

    public BlockEdit(@NotNull Plugin plugin, @NotNull DPlayer player) {
        this.blockHighlight = new BlockHighlight(plugin, player);
        this.dblock = null;
        this.recursionSize = 0;
    }

    @NotNull
    public BlockHighlight getBlockHighlight() {
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
