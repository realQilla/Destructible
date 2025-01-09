package net.qilla.destructible.mining.item;

public enum ToolType {
    HAND,
    PICKAXE,
    DRILL,
    AXE,
    HATCHET,
    SHOVEL;

    @Override
    public String toString() {
        return this.name();
    }
}