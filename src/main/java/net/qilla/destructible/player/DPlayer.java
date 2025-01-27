package net.qilla.destructible.player;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.data.DSounds;
import net.qilla.destructible.data.DataKey;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.ItemData;
import net.qilla.destructible.mining.item.ItemDataType;
import net.qilla.destructible.mining.item.ItemDrop;
import net.qilla.destructible.mining.item.attributes.AttributeTypes;
import net.qilla.destructible.util.ComponentUtil;
import net.qilla.destructible.util.DUtil;
import net.qilla.qlibrary.player.QEnhancedPlayer;
import net.qilla.qlibrary.util.sound.MenuSound;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.stream.Collectors;

public class DPlayer extends QEnhancedPlayer {

    private final Random random = new Random();

    public DPlayer(CraftPlayer craftPlayer) {
        super(craftPlayer);
    }

    public int getSpace(@NotNull ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "ItemStack cannot be null");
        int maxStackSize = itemStack.getMaxStackSize();
        int space = 0;

        for (ItemStack stack : this.getInventory().getStorageContents()) {
            if (stack == null) {
                space += maxStackSize;
            } else if (stack.isSimilar(itemStack)) {
                space += maxStackSize - stack.getAmount();
            }
        }
        return space;
    }

    public void give(@NotNull ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "ItemStack cannot be null");

        int space = this.getSpace(itemStack);
        if(space >= itemStack.getAmount()) {
            this.getInventory().addItem(itemStack);
            return;
        }
        Overflow overflow = DRegistry.PLAYER_DATA.get(this.getUniqueId()).getOverflow();
        ItemData itemData = itemStack.getPersistentDataContainer().get(DataKey.DESTRUCTIBLE_ITEM, ItemDataType.ITEM);

        if(itemData != null) {
            DItem dItem = DUtil.getDItem(itemData.getItemID());
            int overflowAmount = itemStack.getAmount() - space;
            itemStack.setAmount(space);

            overflow.put(itemData, overflowAmount);
            this.getInventory().addItem(itemStack);
            this.playSound(MenuSound.GET_ITEM, true);
            this.sendActionBar(ComponentUtil.getItemAmountAndType(dItem, overflowAmount).append(MiniMessage.miniMessage().deserialize("<green> added to stash")));
        } else {
            this.playSound(DSounds.GENERAL_ERROR, true);
            this.sendActionBar("<red>There was a problem adding an item to your stash!");
        }
    }

    public Map<DItem, Integer> calcItemDrops(@NotNull List<ItemDrop> itemDrops, @NotNull ItemData itemData, @NotNull DItem dItem) {
        int fortune = itemData.getAttributes().getValue(AttributeTypes.MINING_FORTUNE) + dItem.getStaticAttributes().getValue(AttributeTypes.MINING_FORTUNE);

        return itemDrops.stream()
                .filter(this::hasChanceToDrop)
                .sorted(Comparator.comparing(ItemDrop::getChance).reversed())
                .collect(Collectors.toUnmodifiableMap(
                        ItemDrop::getDItem,
                        value -> value.isFortuneAffected() ? calculateAmount(value, fortune) : calculateAmount(value, 0),
                        Integer::sum
                ));
    }

    private boolean hasChanceToDrop(ItemDrop itemDrop) {
        double dropChance = itemDrop.getChance();
        return random.nextDouble() < dropChance;
    }

    private int calculateAmount(ItemDrop itemDrop, int fortune) {
        int baseAmount = random.nextInt(itemDrop.getMaxAmount() - itemDrop.getMinAmount() + 1) + itemDrop.getMinAmount();

        if(fortune == 0) return baseAmount;

        int multiplier = random.nextInt(1, fortune + 1);

        return baseAmount * (multiplier);
    }
}