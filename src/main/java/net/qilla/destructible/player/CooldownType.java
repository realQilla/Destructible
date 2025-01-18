package net.qilla.destructible.player;

public enum CooldownType {
    DEFAULT(1000),
    OPEN_MENU(250),
    MENU_CLICK(250),
    GET_ITEM(200);

    final long cooldown;

    CooldownType(long ms) {
        this.cooldown = ms;
    }

    public long getMs() {
        return this.cooldown;
    }
}
