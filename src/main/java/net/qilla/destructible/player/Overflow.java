package net.qilla.destructible.player;

import com.google.common.base.Preconditions;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.DItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Overflow {

    private final Map<DItem, Integer> overflowItems = new LinkedHashMap<>();

    private final DPlayer dPlayer;

    public Overflow(DPlayer dPlayer) {
        this.dPlayer = dPlayer;
    }

    public int size() {
        return this.overflowItems.size();
    }

    public boolean isEmpty() {
        return this.overflowItems.isEmpty();
    }

    public boolean put(DItem dItem, int amount) {
        Preconditions.checkNotNull(dItem, "DItem cannot be null");

        this.overflowItems.merge(dItem, amount, Integer::sum);
        return true;
    }

    public boolean put(ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "ItemStack cannot be null");

        Optional<DItem> optional = DItemStack.getDItem(itemStack);

        if(optional.isEmpty()) {
            return false;
        }

        DItem dItem = optional.get();

        this.overflowItems.merge(dItem, itemStack.getAmount(), Integer::sum);
        return true;
    }

    public ItemStack take(DItem dItem) {
        Preconditions.checkNotNull(dItem, "DItem cannot be null");
        if(!this.overflowItems.containsKey(dItem)) return null;

        ItemStack itemStack = DItemStack.of(dItem, this.overflowItems.get(dItem));

        int space = dPlayer.getSpace(itemStack);
        if(space == 0) return null;

        if(space >= itemStack.getAmount()) {
            this.overflowItems.remove(dItem);
        } else {
            this.overflowItems.computeIfPresent(dItem, (key, value) -> value - space);
            itemStack.setAmount(space);
        }
        return itemStack;
    }

    public List<ItemStack> take() {
        List<ItemStack> itemStacks = new LinkedList<>();

        Iterator<Map.Entry<DItem, Integer>> iterator = this.overflowItems.entrySet().iterator();

        while(iterator.hasNext()) {
            Map.Entry<DItem, Integer> entry = iterator.next();
            ItemStack itemStack = DItemStack.of(entry.getKey(), entry.getValue());
            int space = dPlayer.getSpace(itemStack);
            if(space == 0) break;

            if(space >= itemStack.getAmount()) {
                itemStacks.add(itemStack);
                iterator.remove();
            } else {
                this.overflowItems.computeIfPresent(entry.getKey(), (key, value) -> value - space);
                itemStack.setAmount(space);
                itemStack.setAmount(space);
                itemStacks.add(itemStack);
                break;
            }
        }
        return itemStacks;
    }

    public List<OverflowItem> getOverflow() {
        return this.overflowItems.entrySet().stream().map(entry -> new OverflowItem(entry.getKey(), entry.getValue())).toList();
    }

    public boolean contains(DItem dItem) {
        return this.overflowItems.containsKey(dItem);
    }

    public void remove(DItem dItem) {
        this.overflowItems.remove(dItem);
    }

    public void clear() {
        this.overflowItems.clear();
    }
}