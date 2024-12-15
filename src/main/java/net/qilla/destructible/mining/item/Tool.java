package net.qilla.destructible.mining.item;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public abstract class Tool implements Breakable {
    private final String id;
    private final Component displayName;
    private final Material material;
    private final Rarity rarity;
    private final int stackSize;
    private final int durability;
    private final ToolType toolType;
    private final int strength;
    private final float efficiency;

    public Tool(String id, Component displayName, Material material, Rarity rarity, int stackSize, ToolType toolType, int durability, int strength, int efficiency) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.rarity = rarity;
        this.stackSize = Math.max(1, Math.min(99, stackSize));
        this.toolType = toolType;
        this.durability = Math.max(1, durability);
        this.strength = Math.max(0, strength);
        this.efficiency = Math.max(0, efficiency);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Component getDisplayName() {
        return this.displayName;
    }

    @Override
    public Material getMaterial() {
        return this.material;
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

    public float getEfficiency() {
        return this.efficiency;
    }
}