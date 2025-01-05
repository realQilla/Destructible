package net.qilla.destructible.menus;

public enum GUISize {

    ONE(9),
    TWO(18),
    THREE(27),
    FOUR(36),
    FIVE(45),
    SIX(54);

    private final int size;
    GUISize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}
