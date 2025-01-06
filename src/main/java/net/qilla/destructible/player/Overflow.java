package net.qilla.destructible.player;

import com.google.common.base.Preconditions;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.DItemStack;

import java.util.*;

public class Overflow {

    private final Map<DItem, DItemStack> overflowItems = new LinkedHashMap<>();

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

    public void put(List<DItemStack> items) {
        for(DItemStack dItemStack : items) {
            put(dItemStack);
        }
    }

    public void put(DItemStack dItemStack) {
        Preconditions.checkNotNull(dItemStack, "DItemStack cannot be null");

        this.overflowItems.merge(dItemStack.getDItem(), dItemStack, (existingItem, newItem) -> {
            existingItem.setAmount(existingItem.getAmount() + newItem.getAmount());
            return existingItem;
        });

        DItemStack existingItem = this.overflowItems.remove(dItemStack.getDItem());
        this.overflowItems.put(dItemStack.getDItem(), existingItem);
    }

    public DItemStack take(DItem dItem) {
        DItemStack dItemStack = this.overflowItems.get(dItem);
        if(dItemStack == null) {
            return null;
        }

        int space = dPlayer.getSpace(dItemStack.getItemStack());
        if(space == 0) {
            return null;
        }

        if(space >= dItemStack.getAmount()) {
            this.overflowItems.remove(dItem);
            return dItemStack;
        } else {
            dItemStack.setAmount(dItemStack.getAmount() - space);
            DItemStack splitItem = dItemStack.clone();
            splitItem.setAmount(space);
            return splitItem;
        }
    }

    public List<DItemStack> take() {
        List<DItemStack> itemList = new LinkedList<>();
        Iterator<Map.Entry<DItem, DItemStack>> iterator = this.overflowItems.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<DItem, DItemStack> entry = iterator.next();
            DItemStack dItemStack = entry.getValue();
            int space = dPlayer.getSpace(dItemStack.getItemStack());
            if(space == 0) break;

            if(space >= dItemStack.getAmount()) {
                itemList.add(dItemStack);
                iterator.remove();
            } else {
                dItemStack.setAmount(dItemStack.getAmount() - space);
                DItemStack splitItem = dItemStack.clone();
                splitItem.setAmount(space);
                itemList.add(splitItem);
                break;
            }
        }
        return itemList;
    }

    public Collection<DItemStack> getItems() {
        return overflowItems.values();
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