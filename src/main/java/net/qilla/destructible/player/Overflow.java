package net.qilla.destructible.player;

import com.google.common.base.Preconditions;
import net.qilla.destructible.mining.item.*;
import net.qilla.qlibrary.registry.RegistrySubscriber;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class Overflow {

    private final Map<String, OverflowEntry> overflowItems = new LinkedHashMap<>();
    private final Set<RegistrySubscriber> registrySubscribers = new HashSet<>();

    private final DPlayer player;

    public Overflow(@NotNull DPlayer player) {
        Preconditions.checkNotNull(player, "DPlayer cannot be null");

        this.player = player;
    }

    public void subscribe(@NotNull RegistrySubscriber subscriber) {
        Preconditions.checkNotNull(subscriber, "Subscriber cannot be null");
        synchronized(registrySubscribers) {
            registrySubscribers.add(subscriber);
        }
    }

    public void unsubscribe(@NotNull RegistrySubscriber subscriber) {
        Preconditions.checkNotNull(subscriber, "Subscriber cannot be null");
        synchronized(registrySubscribers) {
            registrySubscribers.remove(subscriber);
        }
    }

    private void notifySubscribers() {
        for(RegistrySubscriber subscriber : registrySubscribers) {
            subscriber.onUpdate();
        }
    }

    public int typeSize() {
        return this.overflowItems.size();
    }

    public int itemSize() {
        return this.overflowItems.values().stream().mapToInt(OverflowEntry::getAmount).sum();
    }

    public boolean isEmpty() {
        return this.overflowItems.isEmpty();
    }

    public boolean put(@NotNull DItem item, int amount) {
        return this.put(new ItemData(item), amount);
    }

    public boolean put(@NotNull ItemData itemData, int amount) {
        return this.put(new OverflowEntry(itemData, amount));
    }

    public boolean put(@NotNull OverflowEntry overflowEntry) {
        Preconditions.checkNotNull(overflowEntry, "OverflowEntry cannot be null");

        synchronized(overflowItems) {
            overflowItems.merge(overflowEntry.getData().getItemID(), overflowEntry, (oldValue, newValue) -> {
                newValue.addAmount(oldValue.getAmount());
                return newValue;
            });
        }
        return true;
    }

    public Optional<ItemStack> take(@NotNull String ID) {
        Preconditions.checkNotNull(ID, "ID cannot be null");

        synchronized(overflowItems) {
            if(!this.overflowItems.containsKey(ID)) return Optional.empty();

            OverflowEntry overflowEntry = this.overflowItems.get(ID);
            ItemData itemData = overflowEntry.getData();
            int amount = overflowEntry.getAmount();

            ItemStack itemStack = DItemFactory.of(itemData, amount);

            int space = player.getSpace(itemStack);
            if(space == 0) return Optional.empty();

            if(space >= itemStack.getAmount()) this.overflowItems.remove(ID);
            else {
                this.overflowItems.get(ID).subtractAmount(space);
                itemStack.setAmount(space);
            }
            return Optional.of(itemStack);
        }
    }

    public List<Map.Entry<String, OverflowEntry>> getOverflow() {
        return List.copyOf(this.overflowItems.entrySet());
    }

    public boolean contains(String itemID) {
        return this.overflowItems.containsKey(itemID);
    }

    public synchronized void remove(String itemID) {
        this.overflowItems.remove(itemID);
    }

    public synchronized void clear() {
        this.overflowItems.clear();
    }
}