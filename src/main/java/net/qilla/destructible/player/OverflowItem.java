package net.qilla.destructible.player;

import net.qilla.destructible.mining.item.DItem;

public class OverflowItem {

    private final DItem dItem;
    private int amount;

    public OverflowItem(DItem dItem, int amount) {
        this.dItem = dItem;
        this.amount = amount;
    }

    public int add(int amount) {
        return this.amount += amount;
    }

    public int subtract(int amount) {
        return this.amount -= amount;
    }

    public DItem getdItem() {
        return this.dItem;
    }

    public int getAmount() {
        return this.amount;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        OverflowItem that = (OverflowItem) o;

        return dItem.equals(that.dItem);
    }

    @Override
    public int hashCode() {
        return dItem.hashCode();
    }
}
