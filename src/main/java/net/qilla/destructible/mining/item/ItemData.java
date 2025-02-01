package net.qilla.destructible.mining.item;

import com.google.common.base.Preconditions;
import net.qilla.destructible.mining.item.attributes.Attribute;
import net.qilla.destructible.mining.item.attributes.AttributeType;
import net.qilla.destructible.mining.item.attributes.AttributeContainer;
import net.qilla.destructible.util.DUtil;
import org.jetbrains.annotations.NotNull;
import java.util.Set;

/**
 * Object for storing data within an item's Persistent Data Component.
 * Data includes, the item's ID, current version, and specified attributes.
 */

public class ItemData {

    public static final ItemData EMPTY = new ItemData(DItems.MISSING_ITEM);

    private final String itemID;
    private final long version;
    private final AttributeContainer attributes;

    /**
     * Constructs a new instance of ItemData utilizing the DItem given to the constructor.
     * Warning: This may result in a version mismatch from what exists in the registry.
     * @param dItem An instance of DItem either from the registry or elsewhere.
     */

    public ItemData(@NotNull DItem dItem) {
        Preconditions.checkNotNull(dItem, "DItem cannot be null");

        this.itemID = dItem.getID();
        this.version = dItem.getVersion();
        this.attributes = dItem.getDynamicAttributes();
    }

    /**
     * Constructs a new instance of ItemData utilizing while updating it to what currently exists in the registry.
     * @param itemData A pre-existing instance of ItemData.
     */

    public ItemData(@NotNull ItemData itemData) {
        Preconditions.checkNotNull(itemData, "ItemData cannot be null");

        DItem ditem = DUtil.getDItem(itemData.getItemID());

        this.itemID = ditem.getID();
        this.version = ditem.getVersion();
        this.attributes = new AttributeContainer(itemData.getAttributes().getAll());
    }

    /**
     * Builder for building ItemData with what is included within the builder.
     * Warning: This may result in a version mismatch from what exists in the registry.
     * @param builder An instance of the ItemData builder.
     */

    private ItemData(@NotNull Builder builder) {
        Preconditions.checkNotNull(builder, "Builder cannot be null");
        Preconditions.checkNotNull(builder.itemID, "ItemID cannot be null");
        Preconditions.checkNotNull(builder.dynamicAttributes, "Dynamic attribute container cannot be null");

        this.itemID = builder.itemID;
        this.version = builder.version;
        this.attributes = builder.dynamicAttributes;
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public String getItemID() {
        return itemID;
    }

    public long getVersion() {
        return version;
    }

    public <T> T getAttribute(@NotNull AttributeType<T> attribute) {
        Preconditions.checkNotNull(attribute, "Attribute cannot be null");

        return this.attributes.getValue(attribute);
    }

    public AttributeContainer getAttributes() {
        return this.attributes;
    }

    public static class Builder {
        private String itemID;
        private long version;
        private final AttributeContainer dynamicAttributes = new AttributeContainer();

        private Builder() {
        }

        public Builder itemID(@NotNull String itemID) {
            Preconditions.checkNotNull(itemID, "DItem cannot be null");

            this.itemID = itemID;
            return this;
        }

        public Builder version(long version) {
            this.version = version;
            return this;
        }

        public Builder dynamicAttributes(@NotNull Set<Attribute<?>> attributes) {
            Preconditions.checkNotNull(dynamicAttributes, "Dynamic attribute container cannot be null");

            this.dynamicAttributes.set(attributes);
            return this;
        }

        public ItemData build() {
            return new ItemData(this);
        }
    }
}
