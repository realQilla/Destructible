package net.qilla.destructible.menugeneral.slot;

import com.google.common.base.Preconditions;
import net.qilla.destructible.menugeneral.ClickAction;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

public class Socket {

    private final int index;
    private final Slot slot;
    private boolean hasAppeared;
    private final ClickAction clickAction;

    public Socket(int index, @NotNull Slot slot) {
        Preconditions.checkNotNull(slot, "Slot cannot be null");
        this.index = index;
        this.slot = slot;
        this.hasAppeared = false;
        this.clickAction = null;
    }

    public Socket(int index, Slot slot, @Nullable ClickAction clickAction) {
        Preconditions.checkNotNull(slot, "Slot cannot be null");
        this.index = index;
        this.slot = slot;
        this.clickAction = clickAction;
    }

    public int index() {
        return index;
    }

    public Slot slot() {
        return slot;
    }

    public void onClick(@NotNull DPlayer dPlayer, @NotNull InventoryClickEvent event) {
        Preconditions.checkNotNull(dPlayer, "DPlayer cannot be null");
        Preconditions.checkNotNull(event, "Inventory click event cannot be null");
        if(clickAction != null) {
            if(clickAction.onClick(event) && !hasAppeared) {
                dPlayer.playSound(slot.getClickSound(), true);
                hasAppeared = true;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Socket socket = (Socket) o;
        return index == socket.index && slot.equals(socket.slot);
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(index);
        result = 31 * result + slot.hashCode();
        return result;
    }
}
