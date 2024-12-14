package net.qilla.destructible.mining.item;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public class AxeTool extends Tool {
    public AxeTool(String id, Component name, Material material, Rarity rarity, int stackSize, int durability, int strength, int efficiency) {
        super(id, name, material, rarity, stackSize, ToolType.AXE, durability, strength, efficiency);
    }
}