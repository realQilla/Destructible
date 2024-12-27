package net.qilla.destructible.data;

import net.minecraft.Util;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DBlockData {
    private final AtomicInteger minedAmount;
    private final AtomicLong cooldownUntil;
    private Player lastPlayer = null;

    public DBlockData() {
        this.minedAmount = new AtomicInteger(0);
        this.cooldownUntil = new AtomicLong(0);
    }

    public int getMinedAmount() {
        return minedAmount.get();
    }

    public long getCooldownUntil() {
        return cooldownUntil.get();
    }

    public Player getLastPlayer() {
        return lastPlayer;
    }

    public void addMineAmount() {
        this.minedAmount.incrementAndGet();
    }

    public void mined(Player player, long cooldown) {
        this.lastPlayer = player;
        this.cooldownUntil.set(Util.getMillis() + cooldown);
        this.minedAmount.incrementAndGet();

    }

    public boolean isOnCooldown() {
        return Util.getMillis() < cooldownUntil.get();
    }
}