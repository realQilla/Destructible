package net.qilla.destructible.player;

public enum CooldownType {
    OPEN_MENU(250),
    MENU_CLICK(100),
    MENU_ROTATE(100),
    GET_ITEM(100);

    final long cooldown;

    CooldownType(long ms) {
        this.cooldown = ms;
    }

    public long getMs() {
        return this.cooldown;
    }
}
