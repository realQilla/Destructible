package net.qilla.destructible.menugeneral;

import java.util.function.Consumer;

public class SearchConfig {

    private final Builder builder;
    private final int searchIndex;
    private final int resetSearchIndex;

    protected SearchConfig(Builder builder) {
        this.builder = builder;
        this.searchIndex = builder.searchIndex;
        this.resetSearchIndex = builder.resetSearchIndex;
    }

    public static SearchConfig of(Consumer<Builder> builder) {
        Builder newBuilder = new Builder();
        builder.accept(newBuilder);
        return new SearchConfig(newBuilder);
    }

    public Builder searchBuilder() {
        return this.builder;
    }

    public int searchIndex() {
        return this.searchIndex;
    }

    public int resetSearchIndex() {
        return this.resetSearchIndex;
    }

    public static class Builder {

        protected int searchIndex;
        protected int resetSearchIndex;

        protected Builder() {
            this.searchIndex = 0;
            this.resetSearchIndex = 0;
        }

        public Builder searchIndex(int index) {
            this.searchIndex = index;
            return this;
        }

        public Builder resetSearchIndex(int index) {
            this.resetSearchIndex = index;
            return this;
        }
    }
}
