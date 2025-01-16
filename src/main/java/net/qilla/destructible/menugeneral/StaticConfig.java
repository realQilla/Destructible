package net.qilla.destructible.menugeneral;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import java.util.function.Consumer;

public class StaticConfig {

    private final MenuSize menuSize;
    private final Component title;
    private final int menuIndex;
    private final int returnIndex;

    public StaticConfig(Builder builder) {
        this.menuSize = builder.menuSize;
        this.title = builder.title;
        this.menuIndex = builder.menuIndex;
        this.returnIndex = builder.returnIndex;
    }

    public MenuSize menuSize() {
        return this.menuSize;
    }

    public Component title() {
        return this.title;
    }

    public int menuIndex() {
        return this.menuIndex;
    }

    public int returnIndex() {
        return this.returnIndex;
    }

    public static StaticConfig of(Consumer<Builder> builder) {
        Builder newBuilder = new Builder();
        builder.accept(newBuilder);
        return new StaticConfig(newBuilder);
    }

    public static class Builder {

        protected MenuSize menuSize;
        protected Component title;
        protected int menuIndex;
        protected int returnIndex;

        protected Builder() {
            this.menuSize = MenuSize.SIX;
            this.title = MiniMessage.miniMessage().deserialize("<red>Missing Title");
            this.menuIndex = 4;
            this.returnIndex = 49;
        }

        public Builder menuSize(MenuSize menuSize) {
            this.menuSize = menuSize;
            return this;
        }

        public Builder title(Component title) {
            this.title = title;
            return this;
        }

        public Builder menuIndex(int menuIndex) {
            this.menuIndex = menuIndex;
            return this;
        }

        public Builder returnIndex(int returnIndex) {
            this.returnIndex = returnIndex;
            return this;
        }
    }
}