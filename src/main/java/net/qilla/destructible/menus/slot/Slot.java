package net.qilla.destructible.menus.slot;

import com.google.common.base.Preconditions;
import net.qilla.destructible.menus.SoundSettings;
import net.qilla.destructible.util.RandomUtil;
import org.bukkit.event.inventory.InventoryClickEvent;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Slot {

    private final Builder builder;
    private final int index;
    private Display display;
    private BiConsumer<Slot, InventoryClickEvent> action;
    private SoundSettings appearSound;
    private SoundSettings clickSound;
    private final UniqueSlot uniqueSlot;

    private Slot(int index, Builder builder) {
        this.builder = builder;
        this.index = index;
        this.display = builder.display;
        this.action = builder.action;
        this.appearSound = builder.appearSound;
        this.clickSound = builder.clickSound;
        this.uniqueSlot = builder.uniqueSlot;
    }

    public static Slot of(int index, Consumer<Builder> builder) {
        Builder newBuilder = new Builder();
        builder.accept(newBuilder);
        return new Slot(index, newBuilder);
    }

    public static Slot of(int index, Display display) {
        Preconditions.checkNotNull(display, "Display cannot be null");
        Builder builder = new Builder();
        builder.display = display;
        return new Slot(index, builder);
    }

    public void onClick(InventoryClickEvent event) {
        if(action != null) {
            action.accept(this, event);
        }
    }

    public int getIndex() {
        return index;
    }

    public Display getDisplay() {
        return display;
    }

    public SoundSettings getAppearSound() {
        return this.appearSound;
    }

    public SoundSettings getClickSound() {
        return this.clickSound;
    }

    public UniqueSlot getUniqueSlot() {
        return this.uniqueSlot;
    }

    public Slot modify(Consumer<Builder> builder) {
        builder.accept(this.builder);
        this.display = this.builder.display;
        this.action = this.builder.action;
        this.appearSound = this.builder.appearSound;
        this.clickSound = this.builder.clickSound;
        return this;
    }

    public static class Builder {

        private Display display;
        private BiConsumer<Slot, InventoryClickEvent> action;
        private SoundSettings appearSound;
        private SoundSettings clickSound;
        private UniqueSlot uniqueSlot;

        private Builder() {
            this.display = Displays.MISSING;
            this.action = null;
            this.appearSound = null;
            this.clickSound = null;
            this.uniqueSlot = null;
        }

        public Builder display(Display display) {
            Preconditions.checkNotNull(display, "Display cannot be null");
            this.display = display;
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

        public Builder uniqueSlot(UniqueSlot uniqueSlot) {
            this.uniqueSlot = uniqueSlot;
            return this;
        }
    }
}