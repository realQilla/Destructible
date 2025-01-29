package net.qilla.destructible.player;

import com.google.common.base.Preconditions;
import net.qilla.destructible.mining.item.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class Overflow {

    private final Map<String, OverflowEntry> overflowItems = new LinkedHashMap<>();

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

    public boolean put(@NotNull OverflowEntry overflowEntry) {
        Preconditions.checkNotNull(overflowEntry, "OverflowEntry cannot be null");

        this.overflowItems.merge(overflowEntry.getData().getItemID(), overflowEntry, (oldValue, newValue) -> {
           newValue.addAmount(oldValue.getAmount());
           return newValue;
        });
        return true;
    }

    public boolean put(@NotNull DItem item, int amount) {
        Preconditions.checkNotNull(item, "Item cannot be null");

        this.overflowItems.merge(item.getId(), new OverflowEntry(new ItemData(item), amount), (oldValue, newValue) -> {
            newValue.addAmount(amount);
            return newValue;
        });
        return true;
    }

    public boolean put(@NotNull ItemData itemData, int amount) {
        Preconditions.checkNotNull(itemData, "ItemData cannot be null");

        this.overflowItems.merge(itemData.getItemID(), new OverflowEntry(itemData, amount), (oldValue, newValue) -> {
            newValue.setAmount(oldValue.getAmount() + amount);
            return newValue;
        });
        return true;
    }

    public Optional<ItemStack> take(@NotNull String itemId) {
        Preconditions.checkNotNull(itemId, "String cannot be null");

        if(!this.overflowItems.containsKey(itemId)) return Optional.empty();

        OverflowEntry overflowEntry = this.overflowItems.get(itemId);
        ItemData itemData = overflowEntry.getData();
        int amount = overflowEntry.getAmount();

        ItemStack itemStack = DItemFactory.of(itemData, amount);

        int space = dPlayer.getSpace(itemStack);
        if(space == 0) return Optional.empty();

        if(space >= itemStack.getAmount()) this.overflowItems.remove(itemId);
        else {
            this.overflowItems.get(itemId).subtractAmount(space);
            itemStack.setAmount(space);
        }
        return Optional.of(itemStack);
    }

    public Set<Map.Entry<String, OverflowEntry>> getOverflow() {
        return this.overflowItems.entrySet();
    }

    public boolean contains(String itemID) {
        return this.overflowItems.containsKey(itemID);
    }

    public void remove(String itemID) {
        this.overflowItems.remove(itemID);
    }

    public void clear() {
        this.overflowItems.clear();
    }
}