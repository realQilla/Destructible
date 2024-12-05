package net.qilla.destructible.mining.item;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.FoodProperties;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemDrops {

    public static final ItemDrop[] SAND = new ItemDrop[]{
            new ItemDrop(ItemDrop.Properties.of()
                    .setItemStack(ItemStack.of(Material.SAND))),
            new ItemDrop(ItemDrop.Properties.of()
                    .setItemStack(ItemStack.of(Material.COBBLESTONE))
                    .setDropChance(0.10f))};

    public static final ItemDrop[] GRAVEL = new ItemDrop[]{
            new ItemDrop(ItemDrop.Properties.of()
                    .setItemStack(ItemStack.of(Material.GRAVEL))),
            new ItemDrop(ItemDrop.Properties.of()
                    .setItemStack(ItemStack.of(Material.GRAVEL))),
            new ItemDrop(ItemDrop.Properties.of()
                    .setItemStack(ItemStack.of(Material.GRAVEL))),
            new ItemDrop(ItemDrop.Properties.of()
                    .setItemStack(ItemStack.of(Material.GRAVEL))),
            new ItemDrop(ItemDrop.Properties.of()
                    .setItemStack(ItemStack.of(Material.GRAVEL))),
            new ItemDrop(ItemDrop.Properties.of()
                    .setItemStack(ItemStack.of(Material.GRAVEL))),
            new ItemDrop(ItemDrop.Properties.of()
                    .setItemStack(ItemStack.of(Material.GRAVEL))),
            new ItemDrop(ItemDrop.Properties.of()
                    .setItemStack(ItemStack.of(Material.GRAVEL)))};

    public static final ItemDrop[] COBBLESTONE = new ItemDrop[]{
            new ItemDrop(ItemDrop.Properties.of()
                    .setItemStack(ItemStack.of(Material.COBBLESTONE))
                    .setAmount(1, 5))};

    public static final ItemDrop[] STONE = new ItemDrop[]{
            new ItemDrop(ItemDrop.Properties.of()
                    .setItemStack(ItemStack.of(Material.STONE)))};

    public static final ItemDrop[] DIAMOND_ORE = new ItemDrop[]{
            new ItemDrop(ItemDrop.Properties.of()
                    .setItemStack(Material.DIAMOND, item -> {
                        Consumable consumable = Consumable.consumable()
                                .animation(ItemUseAnimation.EAT)
                                .consumeSeconds(2)
                                .build();

                        FoodProperties foodProperties = FoodProperties.food()
                                .canAlwaysEat(true)
                                .nutrition(5)
                                .saturation(5)
                                .build();

                        item.setData(DataComponentTypes.CONSUMABLE, consumable);
                        item.setData(DataComponentTypes.FOOD, foodProperties);
                    }))};
}