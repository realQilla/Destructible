package net.qilla.destructible.data.registry;

import com.google.common.collect.Sets;
import org.jspecify.annotations.Nullable;
import java.util.Set;

public record DRegistryKeyImpl<T>(String key) implements DRegistryKey<T> {
    static final Set<DRegistryKey<?>> REGISTRY_KEYS = Sets.newIdentityHashSet();

    @Override
    public boolean equals(final @Nullable Object object) {
        return object == this;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    public static <T> DRegistryKey<T> create(final String key) {
        final DRegistryKey<T> registryKey = new DRegistryKeyImpl<>(key);
        REGISTRY_KEYS.add(registryKey);
        return registryKey;
    }
}