package net.qilla.destructible.data;

import net.qilla.destructible.mining.block.DBlock;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DBlockEditor {
    private final Player player;
    private DBlock dblock;
    private boolean recursive;
    private int recursionSize;
    private final DestructibleRegistry<ChunkPos, DestructibleRegistry<Integer, Integer>> blockHighlight = new DestructibleRegistry<>();
    private boolean highlight;
    private boolean lockHighlight;

    public DBlockEditor(final Player player) {
        this.player = player;
        this.dblock = null;
        this.recursive = false;;
        this.recursionSize = 0;
        this.highlight = false;
        this.lockHighlight = false;
    }

    public Player getPlayer() {
        return player;
    }

    public DBlock getDblock() {
        return dblock;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public int getRecursionSize() {
        return recursionSize;
    }

    @NotNull
    public DestructibleRegistry<ChunkPos, DestructibleRegistry<Integer, Integer>> getBlockHighlight() {
        return blockHighlight;
    }

    public boolean isHighlightLocked() {
        return lockHighlight;
    }

    public boolean isHighlight() {
        return highlight;
    }

    public void setDblock(DBlock dblock, boolean recursive) {
        this.dblock = dblock;
        this.recursive = recursive;
        this.recursionSize = 4096;
    }

    public void setDblock(DBlock dblock, boolean recursive, int recursionSize) {
        this.dblock = dblock;
        this.recursive = recursive;
        this.recursionSize = recursionSize;
    }

    public void setLockHighlight(boolean lock) {
        this.lockHighlight = lock;
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
    }

    public void clearBlockHighlight() {
        blockHighlight.clear();
    }
}
