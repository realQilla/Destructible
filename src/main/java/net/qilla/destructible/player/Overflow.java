package net.qilla.destructible.player;

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
        if(this.overflowItems.containsKey(dItemStack.getDItem())) {
            this.overflowItems.computeIfPresent(dItemStack.getDItem(), (k, v) -> {
                v.setAmount(v.getAmount() + dItemStack.getAmount());
                return v;
            });
        } else {
            this.overflowItems.put(dItemStack.getDItem(), dItemStack);
        }
        this.overflowItems.get(dItemStack.getDItem());
    }

    public DItemStack take(DItem dItem) {
        DItemStack dItemStack = this.overflowItems.get(dItem);
        if(dItemStack == null) return null;

        int space = dPlayer.getSpace(dItemStack.getItemStack());
        if(space == 0) return null;

        if(space >= dItemStack.getAmount()) {
            this.overflowItems.remove(dItem);
            dPlayer.give(dItemStack);
            return dItemStack;
        } else {
            dItemStack.setAmount(dItemStack.getAmount() - space);
            DItemStack splitItem = dItemStack.clone();
            splitItem.setAmount(space);
            dPlayer.give(splitItem);
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
        itemList.forEach(dPlayer::give);
        return new LinkedList<>(itemList);
    }

    public LinkedList<DItemStack> getItems() {
        return new LinkedList<>(this.overflowItems.values());
    }

    public void clear() {
        this.overflowItems.clear();
    }
}