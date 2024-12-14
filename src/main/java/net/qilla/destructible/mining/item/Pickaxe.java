package net.qilla.destructible.mining.item;

public class Pickaxe extends Tool{
    public Pickaxe(String name, Rarity rarity, int stackSize, int durability, int strength, int efficiency) {
        super(name, rarity, stackSize, ToolType.PICKAXE, durability, strength, efficiency);
    }
}
