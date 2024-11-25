package net.qilla.destructible.util;

import net.qilla.destructible.mining.customblock.ItemDrop;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class GivePlayer {

    private static final Random random = new Random();

    public static void item(final Player player, final ItemDrop[] itemDrop) {
        if(itemDrop.length == 0) return;
        random.setSeed(System.currentTimeMillis());

        for(ItemDrop item : itemDrop) {
            if(random.nextFloat() > item.getDropChance()) continue;
            final int amount = random.nextInt(item.getMaxAmount() - item.getMinAmount() + 1) + item.getMinAmount();
            player.getInventory().addItem(new ItemStack(item.getMaterial(), amount));
        }
        //player.playSound(player,Sound.ENTITY_ITEM_PICKUP, 0.1f, (random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F);
    }
}
