package net.qilla.destructible.player;

public enum CooldownType {
    DEFAULT(1000),
    OPEN_MENU(333),
    MENU_CLICK(100),
    GET_ITEM(250);

    final long cooldown;

    CooldownType(long ms) {
        this.cooldown = ms;
    }

    public long getMs() {
        return this.cooldown;
    }
}
