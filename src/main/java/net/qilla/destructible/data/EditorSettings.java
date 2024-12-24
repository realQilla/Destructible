package net.qilla.destructible.data;

import net.qilla.destructible.mining.block.DBlock;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EditorSettings {
    private final Player player;
    private DBlock dblock;
    private boolean recursive;
    private final DestructibleRegistry<ChunkPos, DestructibleRegistry<Integer, Integer>> blockHighlight = new DestructibleRegistry<>();
    private boolean highlight = false;

    public EditorSettings(final Player player) {
        this.player = player;
        this.dblock = null;
        this.recursive = false;
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

    @NotNull
    public DestructibleRegistry<ChunkPos, DestructibleRegistry<Integer, Integer>> getBlockHighlight() {
        return blockHighlight;
    }

    public boolean isHighlight() {
        return highlight;
    }

    public void setDblock(DBlock dblock, boolean recursive) {
        this.dblock = dblock;
        this.recursive = recursive;
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
    }

    public void clearBlockHighlight() {
        blockHighlight.clear();
    }
}
