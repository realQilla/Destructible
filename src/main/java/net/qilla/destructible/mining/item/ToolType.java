package net.qilla.destructible.mining.item;

import net.qilla.destructible.menugeneral.MaterialRepresentation;
import org.bukkit.Material;

public enum ToolType implements MaterialRepresentation {
    PICKAXE(Material.IRON_PICKAXE),
    DRILL(Material.ARMOR_STAND),
    AXE(Material.IRON_AXE),
    HATCHET(Material.NETHERITE_AXE),
    SHOVEL(Material.IRON_SHOVEL);

    private final Material representation;

    ToolType(Material material) {
        this.representation = material;
    }

    @Override
    public String toString() {
        return this.name();
    }

    @Override
    public Material getRepresentation() {
        return this.representation;
    }
}