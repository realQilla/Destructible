package net.qilla.destructible.mining.item;

public enum DToolType {
    ANY,
    PICKAXE,
    DRILL,
    AXE,
    HATCHET,
    SHOVEL;

    @Override
    public String toString() {
        return this.name();
    }

    public String getName() {
        return this.name().substring(0, 1).toUpperCase() + this.name().substring(1).toLowerCase();
    }
}