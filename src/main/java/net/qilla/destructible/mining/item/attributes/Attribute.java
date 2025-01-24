package net.qilla.destructible.mining.item.attributes;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.qilla.destructible.data.registry.DRegistry;

import java.lang.reflect.Type;

/**
 * A holder class for an associated attribute type and its value.
 * @param type The attribute type in this holder.
 * @param value The value associated with the attribute
 * @param <T>
 */

public record Attribute<T>(AttributeType<T> type, T value) {

    private static final Gson GSON = new Gson();

    public Attribute {
        Preconditions.checkNotNull(type, "Type cannot be null");
        Preconditions.checkNotNull(value, "Value cannot be null");
    }

    public static <T> String encode(Attribute<T> attribute) {
        Preconditions.checkNotNull(attribute, "Attribute cannot be null");
        Preconditions.checkNotNull(attribute.type, "Attribute type cannot be null");
        Preconditions.checkNotNull(attribute.value, "Attribute value cannot be null");

        JsonObject json = new JsonObject();

        json.addProperty("ATTRIBUTE", attribute.type.getKey());
        json.add("VALUE", GSON.toJsonTree(attribute.value));

        return json.toString();
    }

    public static <T> Attribute<T> decode(String encoded) {
        Preconditions.checkNotNull(encoded, "Encoded string cannot be null");

        JsonObject json;
        try {
            json = JsonParser.parseString(encoded).getAsJsonObject();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse encoded JSON: " + encoded, e);
        }

        if (!json.has("ATTRIBUTE") || json.get("ATTRIBUTE").isJsonNull()) {
            throw new IllegalStateException("Missing 'ATTRIBUTE' key in encoded JSON: " + encoded);
        }

        String key = json.get("ATTRIBUTE").getAsString();
        AttributeType<T> attribute = getAttribute(key);

        if (attribute == null) {
            throw new IllegalStateException("Unknown attribute type for key: " + key);
        }

        T value;
        try {
            Type type = attribute.getType();
            value = GSON.fromJson(json.get("VALUE"), type);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize value for key: " + key, e);
        }

        return new Attribute<>(attribute, value);
    }

    public static <T> AttributeType<T> getAttribute(String key) {
        Preconditions.checkNotNull(key, "Key cannot be null");

        AttributeType<?> attribute = DRegistry.ATTRIBUTES.get(key);
        if (attribute == null) {
            throw new IllegalArgumentException("Attribute not found for key: " + key);
        }

        try {
            @SuppressWarnings("unchecked")
            AttributeType<T> castedAttribute = (AttributeType<T>) attribute;
            return castedAttribute;
        } catch (ClassCastException e) {
            throw new IllegalStateException("Type mismatch for attribute key: " + key, e);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Attribute<?> attribute = (Attribute<?>) object;
        return type.equals(attribute.type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
}