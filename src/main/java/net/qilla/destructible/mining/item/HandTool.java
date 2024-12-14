package net.qilla.destructible.mining.item;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public class HandTool extends Tool{
    public HandTool(String id, Component displayName, Material material, Rarity rarity, int stackSize, ToolType toolType, int durability, int strength, int efficiency) {
        super(id, displayName, material, rarity, stackSize, toolType, durability, strength, efficiency);
    }
}
