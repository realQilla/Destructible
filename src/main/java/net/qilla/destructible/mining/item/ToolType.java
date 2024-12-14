package net.qilla.destructible.mining.item;

public enum ToolType {
    PICKAXE,
    DRILL,
    AXE,
    HATCHET;

    @Override
    public String toString() {
        return this.name();
    }

    public String getName() {
        return this.name().substring(0, 1).toUpperCase() + this.name().substring(1).toLowerCase();
    }
}