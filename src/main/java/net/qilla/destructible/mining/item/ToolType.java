package net.qilla.destructible.mining.item;

import org.bukkit.Material;

public enum ToolType {
    PICKAXE(Material.IRON_PICKAXE),
    DRILL(Material.ARMOR_STAND),
    AXE(Material.IRON_AXE),
    HATCHET(Material.NETHERITE_AXE),
    SHOVEL(Material.IRON_SHOVEL);

    private final Material material;

    ToolType(Material material) {
        this.material = material;
    }

    @Override
    public String toString() {
        return this.name();
    }

    public Material getMaterial() {
        return this.material;
    }
}