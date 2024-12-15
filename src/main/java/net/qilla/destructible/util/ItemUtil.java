package net.qilla.destructible.util;

import net.qilla.destructible.mining.item.DDrop;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Random;

public class ItemUtil {

    private static final Random random = new Random();

    public static ItemStack[] rollItemDrops(final DDrop[] itemDrops) {
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

    public static void give(final Player player, final ItemStack[] itemStacks) {
        if(itemStacks.length == 0) return;

        player.getInventory().addItem(itemStacks);
    }

    public static void give(final Player player, final ItemStack itemStack) {
        if(itemStack.isEmpty()) return;

        player.getInventory().addItem(itemStack);
    }
}