package net.qilla.destructible.util;

import net.qilla.destructible.mining.item.DDrop;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public final class ItemUtil {
    private static final Random RANDOM = new Random();

    public static List<ItemStack> rollItemDrops(final List<DDrop> itemDrops) {
        if(itemDrops.isEmpty()) return List.of();
        RANDOM.setSeed(System.currentTimeMillis());

        return itemDrops.stream().filter(drop -> RANDOM.nextFloat() <= drop.getDropChance())
                .map(drop -> {
                    int amount = RANDOM.nextInt(drop.getMaxAmount() - drop.getMinAmount() + 1) + drop.getMinAmount();
                    ItemStack item = DItemsUtil.getItem(drop.getDItem());
                    item.setAmount(amount);
                    return item;
                })
                .toList();
    }

    public static void give(final Player player, final List<ItemStack> itemStacks) {
        if(itemStacks.isEmpty()) return;

        if(player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(itemStacks.toArray(new ItemStack[0]));
        } else {
            for(ItemStack itemStack : itemStacks) {
                player.getWorld().dropItem(player.getLocation(), itemStack);
            }
        }
    }

    public static void give(final Player player, final ItemStack itemStack) {
        if(itemStack.isEmpty()) return;

        if(player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(itemStack);
        } else {
            player.getWorld().dropItem(player.getLocation(), itemStack);
        }
    }
}