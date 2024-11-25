package net.qilla.destructible.mining.customblock;

import org.bukkit.Material;

public class ItemDrops {

    public static final ItemDrop[] SAND = new ItemDrop[]{
            new ItemDrop(ItemDrop.Properties.of()
                    .setMaterial(Material.SAND)),
            new ItemDrop(ItemDrop.Properties.of()
                    .setMaterial(Material.COBBLESTONE)
                    .setDropChance(0.10f))};

    public static final ItemDrop[] GRAVEL = new ItemDrop[]{
            new ItemDrop(ItemDrop.Properties.of()
                    .setMaterial(Material.GRAVEL))};

    public static final ItemDrop[] COBBLESTONE = new ItemDrop[]{
            new ItemDrop(ItemDrop.Properties.of()
                    .setMaterial(Material.COBBLESTONE)
                    .setAmount(1, 5))};

    public static final ItemDrop[] STONE = new ItemDrop[]{
            new ItemDrop(ItemDrop.Properties.of()
                    .setMaterial(Material.STONE))};
}