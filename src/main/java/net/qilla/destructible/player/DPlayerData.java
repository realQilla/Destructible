package net.qilla.destructible.player;

import net.qilla.destructible.mining.logic.MiningManager;
import net.qilla.qlibrary.data.QPlayerData;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class DPlayerData extends QPlayerData<DPlayer> {

    private final Plugin plugin;
    private final Overflow overflow;
    private final MiningManager miningManager;
    private BlockEdit blockEdit;

    public DPlayerData(@NotNull DPlayer player, @NotNull Plugin plugin) {
        super(player);

        this.plugin = plugin;
        this.overflow = new Overflow(this.getPlayer());
        this.miningManager = new MiningManager(plugin, this.getPlayer());
    }

    public DPlayerData(@NotNull DPlayer player, @NotNull DPlayerData playerData) {
        super(player, playerData);

        this.plugin = playerData.plugin;
        this.overflow = playerData.overflow;
        this.miningManager = playerData.miningManager;
        this.blockEdit = null;
    }

    public @NotNull Overflow getOverflow() {
        return overflow;
    }

    public @NotNull MiningManager getMiningManager() {
        return miningManager;
    }

    public synchronized @NotNull BlockEdit getBlockEdit() {
        if(blockEdit == null) blockEdit = new BlockEdit(plugin, this.getPlayer());
        return blockEdit;
    }

    public boolean isBlockEditing() {
        return this.blockEdit != null;
    }

    public synchronized void removeBlockEdit() {
        this.blockEdit = null;
    }

    @Override
    public @NotNull DPlayer getPlayer() {
        return super.getPlayer();
    }
}
