package net.qilla.destructible.util;

import net.qilla.destructible.mining.item.ItemDrop;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Array;
import java.util.Arrays;
import java.util.Random;

public class ItemManager {

    private static final Random random = new Random();

    public static ItemStack[] pullItem(final ItemDrop[] itemDrops) {
        if(itemDrops.length == 0) return new ItemStack[0];
        random.setSeed(System.currentTimeMillis());

        return Arrays.stream(itemDrops)
                .filter(itemDrop -> random.nextFloat() <= itemDrop.getDropChance())
                .map(itemDrop -> {
                    int amount = random.nextInt(itemDrop.getMaxAmount() - itemDrop.getMinAmount() + 1) + itemDrop.getMinAmount();
                    ItemStack item = itemDrop.getItemStack();
                    item.setAmount(amount);
                    return item;
                })
                .toArray(ItemStack[]::new);
    }

    public static void give(final Player player, final ItemStack[] itemStack) {
        if(itemStack.length == 0) return;

        player.getInventory().addItem(itemStack);
        //player.playSound(player,Sound.ENTITY_ITEM_PICKUP, 0.1f, (random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F);
    }

    public static void give(final Player player, final ItemStack itemStack) {
        if(itemStack.isEmpty()) return;

        player.getInventory().addItem(itemStack);
        //player.playSound(player,Sound.ENTITY_ITEM_PICKUP, 0.1f, (random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F);
    }
}