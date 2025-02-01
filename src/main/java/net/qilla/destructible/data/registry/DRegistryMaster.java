package net.qilla.destructible.data.registry;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class DRegistryMaster {

    private DRegistryMaster() {
    }

    private static final Map<DRegistryKey<?>, DRegistryHolder<?, ?>> MASTER_REGISTRY = new ConcurrentHashMap<>();

    private static <T, K , V> void addRegistry(@NotNull DRegistryKey<T> key, @NotNull DRegistryHolder<K, V> registry) {
        Preconditions.checkNotNull(key, "Key cannot be null");
        Preconditions.checkNotNull(registry, "Registry cannot be null");

        MASTER_REGISTRY.put(key, registry);
    }

    public static @NotNull <T, K, V> DRegistryHolder<K, V> getRegistry(@NotNull DRegistryKey<T> key) {
        Preconditions.checkNotNull(key, "RegistryType cannot be null");

        return getHolder(key);
    }

    @SuppressWarnings("unchecked")
    public static <T, K, V> DRegistryHolder<K, V> getHolder(@NotNull DRegistryKey<T> key) {
        Preconditions.checkNotNull(key, "RegistryType cannot be null");

        if(!DRegistryKeyImpl.REGISTRY_KEYS.contains(key)) {
            throw new NoSuchElementException(key + " is not a valid registry key");
        } else {
            DRegistryHolder<K, V> registryHolder = (DRegistryHolder<K, V>) MASTER_REGISTRY.get(key);
            if(registryHolder == null) {
                throw new NoSuchElementException(key + " is not an existing registry");
            } else return registryHolder;
        }
    }

    static {
        addRegistry(DRegistryKey.LOADED_BLOCKS, new DRegistryHolder<>());
        addRegistry(DRegistryKey.LOADED_BLOCKS_GROUPED, new DRegistryHolder<>());
        addRegistry(DRegistryKey.BLOCK_EDITORS, new DRegistryHolder<>());
        addRegistry(DRegistryKey.LOADED_BLOCK_MEMORY, new DRegistryHolder<>());
        addRegistry(DRegistryKey.ITEMS, new DRegistryHolder<>());
        addRegistry(DRegistryKey.BLOCKS, new DRegistryHolder<>());
        addRegistry(DRegistryKey.ATTRIBUTES, new DRegistryHolder<>());
    }
}