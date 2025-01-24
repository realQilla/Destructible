package net.qilla.destructible.mining.item.attributes;

import net.qilla.destructible.menugeneral.MaterialRepresentation;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class AttributeType<T> implements MaterialRepresentation {

    private final String key;
    private final T defaultValue;
    private final Material representation;
    private final Type type;

    public AttributeType(@NotNull String key, @Nullable T defaultValue, @NotNull Material material, @NotNull Type type) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.representation = material;
        this.type = type;
    }

    public @NotNull String getKey() {
        return key;
    }

    public @Nullable T getDefaultValue() {
        return defaultValue;
    }

    public @NotNull  Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        AttributeType<?> other = (AttributeType<?>) object;
        return key.equals(other.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public Material getRepresentation() {
        return representation;
    }
}