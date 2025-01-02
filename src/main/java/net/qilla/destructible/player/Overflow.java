package net.qilla.destructible.player;

import net.qilla.destructible.mining.item.DItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

public class Overflow {

    private final Queue<OverflowItem> overflowItems = new LinkedList<>();

    public int size() {
        return this.overflowItems.size();
    }

    public boolean isEmpty() {
        return this.overflowItems.isEmpty();
    }

    public void put(List<OverflowItem> items) {
        for(OverflowItem dItemStack : items) {
            put(dItemStack);
        }
    }

    public void put(OverflowItem item) {
        if(this.overflowItems.isEmpty()) {
            this.overflowItems.add(item);
            return;
        }

        LinkedList<OverflowItem> itemList = (LinkedList<OverflowItem>) this.overflowItems;
        OverflowItem lastItem = itemList.peekLast();

        if(lastItem != null && lastItem.equals(item)) {
            lastItem.add(item.getAmount());
        } else {
            itemList.add(item);
        }

        for(int i = 0; i < itemList.size() - 1; i++) {
            OverflowItem current = itemList.get(i);
            OverflowItem next = itemList.get(i + 1);

            if(current.equals(next)) {
                current.add(next.getAmount());
                itemList.remove(i + 1);
                i--;
            }
        }
    }

    public List<DItemStack> take(DPlayer dPlayer) {
        List<DItemStack> itemList = new LinkedList<>();
        while(!this.overflowItems.isEmpty()) {
            OverflowItem overflowItem = this.overflowItems.peek();
            DItemStack dItemStack = DItemStack.of(overflowItem.getDItem(), overflowItem.getAmount());
            int space = dPlayer.getSpace(dItemStack);
            if(space == 0) break;

            if(space >= dItemStack.getAmount()) {
                itemList.add(dItemStack);
                this.overflowItems.poll();
            } else {
                overflowItem.subtract(space);
                DItemStack splitItem = dItemStack.clone();
                splitItem.setAmount(space);
                itemList.add(splitItem);
                break;
            }
        }
        itemList.forEach(item -> dPlayer.give(item.clone()));
        return new LinkedList<>(itemList);
    }

    public void clear() {
        this.overflowItems.clear();
    }
}