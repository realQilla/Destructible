package net.qilla.destructible.player;

import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.DItemStack;

import java.util.Objects;

public class OverflowItem {

    private final DItem dItem;
    private int amount;

    private OverflowItem(DItem dItem, int amount) {
        this.dItem = dItem;
        this.amount = amount;
    }

    public static OverflowItem of(DItem dItem, int amount) {
        return new OverflowItem(dItem, amount);
    }

    public static OverflowItem of(DItemStack dItemStack) {
        return new OverflowItem(dItemStack.getDItem(), dItemStack.getAmount());
    }

    public void add(int amount) {
        this.amount += amount;
    }

    public void set(int amount) {
        this.amount = amount;
    }

    public void subtract(int amount) {
        this.amount -= amount;
    }

    public DItem getDItem() {
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
        return Objects.equals(dItem.getId(), that.dItem.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(dItem);
    }
}