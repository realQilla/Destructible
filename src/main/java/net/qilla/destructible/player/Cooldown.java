package net.qilla.destructible.player;

public enum Cooldown {
    DEFAULT(1000),
    OPEN_MENU(500),
    MENU_CLICK(333);

    final long cooldown;

    Cooldown(long ms) {
        this.cooldown = ms;
    }

    public long getMs() {
        return this.cooldown;
    }
}
