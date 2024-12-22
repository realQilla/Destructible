package net.qilla.destructible.mining.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class DDrops {

    public static final DDrop[] SAND = new DDrop[]{
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.SAND))
            ),
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.COBBLESTONE))
                    .dropChance(0.15f)
            )
    };

    public static final DDrop[] CACTUS = new DDrop[]{
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.GREEN_DYE))
                    .dropChance(0.23f)
            ),
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.DIAMOND))
                    .amount(1, 3)
                    .dropChance(0.03f)
            )
    };

    public static final DDrop[] RED_SAND = new DDrop[]{
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.RED_SAND))
            ),
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.COBBLESTONE))
                    .dropChance(0.15f)
            )
    };

    public static final DDrop[] GRAVEL = new DDrop[]{
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.GRAVEL))
            ),
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.COBBLESTONE))
                    .dropChance(0.5f)
            ),
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.SAND))
                    .amount(1, 3)
            ),
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.DEAD_BUSH))
                    .amount(1, 2)
                    .dropChance(0.25f)
            )
    };

    public static final DDrop[] COBBLESTONE = new DDrop[]{
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.COBBLESTONE))
                    .amount(1, 2)
            )
    };

    public static final DDrop[] OAK_PLANK = new DDrop[]{
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.OAK_PLANKS))
                    .amount(1)
            )
    };

    public static final DDrop[] OAK_LOG = new DDrop[]{
            new DDrop(DDrop.Properties.of()
                    .itemStack(ItemStack.of(Material.OAK_LOG))
                    .amount(1)
            )
    };
}