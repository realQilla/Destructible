package net.qilla.destructible.data;

import net.minecraft.Util;
import org.bukkit.entity.Player;

public class DBlockData {
    private int minedAmount;
    private long cooldownUntil;
    private Player lastPlayer = null;

    public DBlockData() {
        this.minedAmount = 0;
        this.cooldownUntil = 0;
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