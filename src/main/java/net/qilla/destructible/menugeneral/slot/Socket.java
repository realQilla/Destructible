package net.qilla.destructible.menugeneral.slot;

import com.google.common.base.Preconditions;
import net.qilla.destructible.menugeneral.ClickAction;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

public class Socket {

    private final int index;
    private final Slot slot;
    private final ClickAction clickAction;
    private final CooldownType cooldownType;

    public Socket(int index, @NotNull Slot slot) {
        Preconditions.checkNotNull(slot, "Slot cannot be null");
        this.index = index;
        this.slot = slot;
        this.clickAction = null;
        this.cooldownType = null;
    }

    public Socket(int index, Slot slot, @Nullable ClickAction clickAction, @Nullable CooldownType cooldownType) {
        Preconditions.checkNotNull(slot, "Slot cannot be null");
        this.index = index;
        this.slot = slot;
        this.clickAction = clickAction;
        this.cooldownType = cooldownType;
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
        if(clickAction == null) return;
        if(dPlayer.getCooldown().has(cooldownType)) return;
        if(clickAction.onClick(event)) {
            dPlayer.getCooldown().set(cooldownType);
            dPlayer.playSound(slot.getClickSound(), true);
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