package net.qilla.destructible.mining.player.data;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Necessary player related data.
 */
public class PlayerData {

    private final Player player;
    private final Equipment equipment;
    private MineData mineData = null;

    public PlayerData(@NotNull final Player player) {
        this.player = player;
        this.equipment = new Equipment(this);
    }

    @NotNull
    public Player getPlayer() {
        return this.player;
    }

    @Nullable
    public MineData setMiningData(@Nullable MineData mineData) {
        return this.mineData = mineData;
    }

    @NotNull
    public Equipment getEquipment() {
        return equipment;
    }

    @Nullable
    public MineData getMiningData() {
        return this.mineData;
    }
}
