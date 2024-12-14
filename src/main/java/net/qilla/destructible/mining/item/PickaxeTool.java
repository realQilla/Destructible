package net.qilla.destructible.mining.item;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public class PickaxeTool extends Tool{
    public PickaxeTool(String id, Component name, Material material, Rarity rarity, int stackSize, int durability, int strength, int efficiency) {
        super(id, name, material, rarity, stackSize, ToolType.PICKAXE, durability, strength, efficiency);
    }
}
