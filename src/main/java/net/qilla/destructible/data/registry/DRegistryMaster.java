package net.qilla.destructible.data.registry;

import com.google.common.base.Preconditions;
import net.qilla.destructible.data.Subscriber;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public final class DRegistryMaster {

    private DRegistryMaster() {
    }

    private static final Map<DRegistryKey<?>, DRegistryHolder<?>> MASTER_REGISTRY = new ConcurrentHashMap<>();

    public static <T> void addRegistry(@NotNull DRegistryKey<T> key, @NotNull DRegistryHolder<T> registry) {
        Preconditions.checkNotNull(key, "Key cannot be null");
        Preconditions.checkNotNull(registry, "Registry cannot be null");

        MASTER_REGISTRY.put(key, registry);
    }

    public static @NotNull <T> T getRegistry(@NotNull DRegistryKey<T> key) {
        Preconditions.checkNotNull(key, "RegistryType cannot be null");

        return getHolder(key).get();
    }

    public static void subscribe(@NotNull DRegistryKey<?> key, @NotNull Subscriber subscriber) {
        Preconditions.checkNotNull(key, "RegistryType cannot be null");

        getHolder(key).subscribe(subscriber);
    }

    public static void unsubscribe(@NotNull DRegistryKey<?> key, @NotNull Subscriber subscriber) {
        Preconditions.checkNotNull(key, "RegistryType cannot be null");

        getHolder(key).unsubscribe(subscriber);
    }

    @SuppressWarnings("unchecked")
    public static <T> DRegistryHolder<T> getHolder(@NotNull DRegistryKey<T> key) {
        Preconditions.checkNotNull(key, "RegistryType cannot be null");

        if(!DRegistryKeyImpl.REGISTRY_KEYS.contains(key)) {
            throw new NoSuchElementException(key + " is not a valid registry key");
        } else {
            DRegistryHolder<T> registryHolder = (DRegistryHolder<T>) MASTER_REGISTRY.get(key);
            if(registryHolder == null) {
                throw new NoSuchElementException(key + " is not an existing registry");
            } else return registryHolder;
        }
    }

    static {
        addRegistry(DRegistryKey.LOADED_BLOCKS, new DRegistryHolder<>(new ConcurrentHashMap<>()));
        addRegistry(DRegistryKey.LOADED_BLOCKS_GROUPED, new DRegistryHolder<>(new ConcurrentHashMap<>()));
        addRegistry(DRegistryKey.PLAYER_DATA, new DRegistryHolder<>(new ConcurrentHashMap<>()));
        addRegistry(DRegistryKey.BLOCK_EDITORS, new DRegistryHolder<>(ConcurrentHashMap.newKeySet()));
        addRegistry(DRegistryKey.LOADED_BLOCK_MEMORY, new DRegistryHolder<>(new ConcurrentHashMap<>()));
        addRegistry(DRegistryKey.ITEMS, new DRegistryHolder<>(new ConcurrentSkipListMap<>()));
        addRegistry(DRegistryKey.BLOCKS, new DRegistryHolder<>(new ConcurrentSkipListMap<>()));
        addRegistry(DRegistryKey.ATTRIBUTES, new DRegistryHolder<>(new HashMap<>()));
    }
}