package net.qilla.destructible.player.data;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class PlayerData {

    private final Player player;
    private MiningData miningData = null;

    public PlayerData(@NotNull final Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return this.player;
    }

    @Nullable
    public MiningData setMiningData(@Nullable MiningData miningData) {
        return this.miningData = miningData;
    }

    @Nullable
    public MiningData getMiningData() {
        return this.miningData;
    }
}
