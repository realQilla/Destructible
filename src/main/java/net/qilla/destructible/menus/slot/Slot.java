package net.qilla.destructible.menus.slot;

import net.qilla.destructible.menus.Display;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;

public class Slot {

    private final int index;
    private final Display display;

    private final BiConsumer<Slot, ClickType> action;

    public Slot(int index, Display display, BiConsumer<Slot, ClickType> action) {
        this.index = index;
        this.display = display;
        this.action = action;
    }

    public Slot(int index, Display display) {
        this.index = index;
        this.display = display;
        this.action = null;
    }

    public static Slot of(int index, Display display, BiConsumer<Slot, ClickType> action) {
        return new Slot(index, display, action);
    }

    public static Slot of(int index, Display display) {
        return new Slot(index, display);
    }

    public void onClick(ClickType clickType) {
        if(action == null) return;
        action.accept(this, clickType);
    }

    public int getIndex() {
        return index;
    }

    public Display getDisplay() {
        return display;
    }
}