package net.qilla.destructible.menugeneral;

import java.util.List;
import java.util.function.Consumer;

public class DynamicConfig {

    private final List<Integer> dynamicSlots;
    private final int nextIndex;
    private final int previousIndex;
    private final int shiftAmount;

    protected DynamicConfig(Builder builder) {
        this.dynamicSlots = builder.dynamicSlots;
        this.nextIndex = builder.nextIndex;
        this.previousIndex = builder.previousIndex;
        this.shiftAmount = builder.shiftAmount;
    }

    public static DynamicConfig of(Consumer<Builder> builder) {
        Builder newBuilder = new Builder();
        builder.accept(newBuilder);
        return new DynamicConfig(newBuilder);
    }

    public List<Integer> dynamicIndexes() {
        return this.dynamicSlots;
    }

    public int nextIndex() {
        return this.nextIndex;
    }

    public int previousIndex() {
        return this.previousIndex;
    }

    public int shiftAmount() {
        return this.shiftAmount;
    }

    public static class Builder {

        protected List<Integer> dynamicSlots;
        protected int nextIndex;
        protected int previousIndex;
        protected int shiftAmount;

        protected Builder() {
            this.dynamicSlots = List.of(
                    9, 10, 11, 12, 13, 14, 15, 16, 17,
                    18, 19, 20, 21, 22, 23, 24, 25, 26,
                    27, 28, 29, 30, 31, 32, 33, 34, 35,
                    36, 37, 38, 39, 40, 41, 42, 43, 44
            );
            this.nextIndex = 52;
            this.previousIndex = 7;
            this.shiftAmount = 9;
        }

        public Builder dynamicSlots(List<Integer> dynamicSlots) {
            this.dynamicSlots = dynamicSlots;
            return this;
        }

        public Builder nextIndex(int nextIndex) {
            this.nextIndex = nextIndex;
            return this;
        }

        public Builder previousIndex(int previousIndex) {
            this.previousIndex = previousIndex;
            return this;
        }

        public Builder shiftAmount(int shiftAmount) {
            this.shiftAmount = shiftAmount;
            return this;
        }
    }
}
