package net.qilla.destructible.mining.item;

public abstract class Tool implements Breakable {
    private final String name;
    private final Rarity rarity;
    private final int stackSize;
    private final int durability;
    private final ToolType toolType;
    private final int strength;
    private final int efficiency;

    public Tool(String name, Rarity rarity, int stackSize, ToolType toolType, int durability, int strength, int efficiency) {
        this.name = name;
        this.rarity = rarity;
        this.stackSize = Math.max(1, Math.min(99, stackSize));
        this.toolType = toolType;
        this.durability = Math.max(1, durability);
        this.strength = Math.max(0, strength);
        this.efficiency = Math.max(0, efficiency);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Rarity getRarity() {
        return this.rarity;
    }

    @Override
    public int getStackSize() {
        return this.stackSize;
    }

    @Override
    public int getDurability() {
        return this.durability;
    }

    public ToolType getToolType() {
        return this.toolType;
    }

    public int getStrength() {
        return this.strength;
    }

    public int getEfficiency() {
        return this.efficiency;
    }
}