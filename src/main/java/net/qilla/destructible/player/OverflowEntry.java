package net.qilla.destructible.player;

import com.google.common.base.Preconditions;
import net.qilla.destructible.mining.item.ItemData;
import org.jetbrains.annotations.NotNull;

public class OverflowEntry {
    private final ItemData itemData;
    private int amount;

    public OverflowEntry(@NotNull ItemData itemData, int amount) {
        Preconditions.checkNotNull(itemData, "ItemData cannot be null");

        this.itemData = itemData;
        this.amount = amount;
    }

    public @NotNull ItemData getData() {
        return this.itemData;
    }

    public int addAmount(int amount) {
        return this.amount += amount;
    }

    public int subtractAmount(int amount) {
        return this.amount -= amount;
    }

    public int setAmount(int amount) {
        return this.amount = amount;
    }

    public int getAmount() {
        return this.amount;
    }
}
