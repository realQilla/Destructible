package net.qilla.destructible.data;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public final class Registry<K, V> extends ConcurrentHashMap<K, V> {
    private final Set<BiConsumer<K, V>> listeners;

    public Registry() {
        this.listeners = ConcurrentHashMap.newKeySet();
    }

    public void addListener(BiConsumer<K, V> listener) {
        this.listeners.add(listener);
    }

    public void removeListener(BiConsumer<K, V> listener) {
        this.listeners.remove(listener);
    }

    public void notifyListeners(K key, V value) {
        for(BiConsumer<K, V> listener : this.listeners) {
            listener.accept(key, value);
        }
    }
}