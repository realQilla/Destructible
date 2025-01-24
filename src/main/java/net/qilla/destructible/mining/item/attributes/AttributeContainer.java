package net.qilla.destructible.mining.item.attributes;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A container that holds attributes along with their value
 */

public class AttributeContainer implements Iterable<Map.Entry<AttributeType<?>, Optional<?>>> {
    private final Reference2ObjectMap<AttributeType<?>, Optional<?>> attributeMap;

    /**
     * Create a container with a predefined set of attributes
     * @param attributes
     */

    public AttributeContainer(@NotNull Set<Attribute<?>> attributes) {
        Preconditions.checkNotNull(attributes, "Attributes cannot be null");

        this.attributeMap = new Reference2ObjectOpenHashMap<>();
        this.set(attributes);
    }

    /**
     * Create a new empty container.
     */

    public AttributeContainer() {
        this.attributeMap = new Reference2ObjectOpenHashMap<>();
    }

    public <T> void set(@Nullable AttributeType<T> attribute, T value) {
        Preconditions.checkNotNull(attribute, "Attribute cannot be null");
        Preconditions.checkNotNull(value, "Value cannot be null");

        attributeMap.put(attribute, Optional.of(value));
    }

    public <T> void set(@NotNull Attribute<T> attribute) {
        Preconditions.checkNotNull(attribute, "Attribute cannot be null");

        this.set(attribute.type(), attribute.value());
    }

    public void set(@NotNull Set<Attribute<?>> attributes) {
        attributes.forEach(this::set);
    }

    public <T> @NotNull T getValue(@NotNull AttributeType<T> attribute) {
        Preconditions.checkNotNull(attribute, "Attribute cannot be null");

        if (!attributeMap.containsKey(attribute)) return attribute.getDefaultValue();

        Optional<T> optional = (Optional<T>) attributeMap.get(attribute);

        return optional.orElseGet(attribute::getDefaultValue);
    }

    public <T> @NotNull Attribute<T> get(@NotNull AttributeType<T> attribute) {
        Preconditions.checkNotNull(attribute, "Attribute cannot be null");

        T value = this.getValue(attribute);
        return new Attribute<>(attribute, value);
    }

    public Set<Attribute<?>> getAll() {
        return attributeMap.keySet().stream().map(this::get).collect(Collectors.toSet());
    }

    public boolean has(@NotNull AttributeType<?> attribute) {
        Preconditions.checkNotNull(attribute, "Attribute cannot be null");

        return attributeMap.containsKey(attribute);
    }

    public void remove(@NotNull AttributeType<?> attribute) {
        Preconditions.checkNotNull(attribute, "Attribute cannot be null");

        attributeMap.remove(attribute);
    }

    public void clear() {
        attributeMap.clear();
    }

    @Override
    public @NotNull Iterator<Map.Entry<AttributeType<?>, Optional<?>>> iterator() {
        return attributeMap.entrySet().iterator();
    }

    public Stream<Map.Entry<AttributeType<?>, Optional<?>>> stream() {
        return attributeMap.entrySet().stream();
    }

    public Set<AttributeType<?>> keySet() {
        return attributeMap.keySet();
    }

    public Reference2ObjectMap<AttributeType<?>, Optional<?>> getAttributeMap() {
        return attributeMap;
    }
}