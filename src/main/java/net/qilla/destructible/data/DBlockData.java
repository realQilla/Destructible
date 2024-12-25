package net.qilla.destructible.data;

import net.minecraft.Util;
import org.bukkit.entity.Player;

public class DBlockData {
    private int minedAmount;
    private long cooldownUntil;
    private Player lastPlayer;

    public DBlockData(Player player) {
        this.minedAmount = 1;
        this.cooldownUntil = 0;
        this.lastPlayer = player;
    }

    public int getMinedAmount() {
        return minedAmount;
    }

    public long getCooldownUntil() {
        return cooldownUntil;
    }

    public Player getLastPlayer() {
        return lastPlayer;
    }

    public void addMineAmount() {
        this.minedAmount++;
    }

    public void mined(Player player, long cooldown) {
        this.lastPlayer = player;
        this.cooldownUntil = Util.getMillis() + cooldown;
        this.minedAmount++;

    }

    public boolean isOnCooldown() {
        return Util.getMillis() < cooldownUntil;
    }
}
