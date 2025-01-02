package net.qilla.destructible.player;

public enum Cooldown {
    DEFAULT(1000);

    final long cooldown;

    Cooldown(long ms) {
        this.cooldown = ms;
    }

    public long getCooldown() {
        return this.cooldown;
    }
}
