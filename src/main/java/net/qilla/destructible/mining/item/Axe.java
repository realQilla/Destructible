package net.qilla.destructible.mining.item;

public class Axe extends Tool {
    public Axe(String name, Rarity rarity, int stackSize, int durability, int strength, int efficiency) {
        super(name, rarity, stackSize, ToolType.AXE, durability, strength, efficiency);
    }
}