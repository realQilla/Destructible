package net.qilla.destructible.menus.slot;

import com.google.common.base.Preconditions;
import net.qilla.destructible.menus.SoundSettings;
import org.bukkit.event.inventory.InventoryClickEvent;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Slot {

    private Display display;
    private UniqueSlot uniqueSlot;
    private BiConsumer<Slot, InventoryClickEvent> action;
    private SoundSettings appearSound;
    private SoundSettings clickSound;
    private int delay;

    private Slot(Builder builder) {
        this.display = builder.display;
        this.uniqueSlot = builder.uniqueSlot;
        this.action = builder.action;
        this.appearSound = builder.appearSound;
        this.clickSound = builder.clickSound;
        this.delay = builder.delay;
    }

    public static Slot of(Consumer<Builder> builder) {
        Builder newBuilder = new Builder();
        builder.accept(newBuilder);
        return new Slot(newBuilder);
    }

    public static Slot of(Display display) {
        Preconditions.checkNotNull(display, "Display cannot be null");
        Builder builder = new Builder();
        builder.display = display;
        return new Slot(builder);
    }

    public void onClick(InventoryClickEvent event) {
        if(action != null) {
            action.accept(this, event);
        }
    }

    public Display getDisplay() {
        return display;
    }

public UniqueSlot getUniqueSlot() {
        return this.uniqueSlot;
    }

    public SoundSettings getAppearSound() {
        return this.appearSound;
    }

    public SoundSettings getClickSound() {
        return this.clickSound;
    }

    public int getDelay() {
        return this.delay;
    }

    public static class Builder {

        private Display display;
        private UniqueSlot uniqueSlot;
        private BiConsumer<Slot, InventoryClickEvent> action;
        private SoundSettings appearSound;
        private SoundSettings clickSound;
        private int delay;

        private Builder() {
            this.display = Displays.MISSING;
            this.action = null;
            this.appearSound = null;
            this.clickSound = null;
            this.delay = 0;
        }

        public Builder display(Display display) {
            Preconditions.checkNotNull(display, "Display cannot be null");
            this.display = display;
            return this;
        }

        public Builder uniqueSlot(UniqueSlot uniqueSlot) {
            this.uniqueSlot = uniqueSlot;
            return this;
        }

        public Builder action(BiConsumer<Slot, InventoryClickEvent> consumer) {
            this.action = consumer;
            return this;
        }

        public Builder appearSound(SoundSettings soundSettings) {
            this.appearSound = soundSettings;
            return this;
        }

        public Builder clickSound(SoundSettings soundSettings) {
            this.clickSound = soundSettings;
            return this;
        }

        public Builder delay(int delay) {
            this.delay = delay;
            return this;
        }
    }
}