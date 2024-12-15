package net.qilla.destructible.mining.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class DDrops {

    public static final DDrop[] SAND = new DDrop[]{
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.SAND))),
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.COBBLESTONE))
                    .dropChance(0.10f))};

    public static final DDrop[] GRAVEL = new DDrop[]{
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.GRAVEL))),
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.GRAVEL))),
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.GRAVEL))),
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.GRAVEL))),
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.GRAVEL))),
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.GRAVEL))),
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.GRAVEL))),
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.GRAVEL)))};

    public static final DDrop[] COBBLESTONE = new DDrop[]{
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.COBBLESTONE))
                    .amount(1, 5))};

    public static final DDrop[] STONE = new DDrop[]{
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.STONE)))};
}