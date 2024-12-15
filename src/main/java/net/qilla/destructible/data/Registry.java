package net.qilla.destructible.data;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class Registry<K, V> {
    private final Map<K, V> registry;
    private final Set<BiConsumer<K, V>> listeners;

    public Registry() {
        this.registry = new ConcurrentHashMap<>();
        this.listeners = ConcurrentHashMap.newKeySet();
    }

    public V register(K key, V value) {
        V previousValue = this.registry.put(key, value);

        return this.registry.put(key, value);
    }

    public V get(K key) {
        return this.registry.get(key);
    }

    public V unregister(K key) {
        return this.registry.remove(key);
    }

    public boolean has(K key) {
        return this.registry.containsKey(key);
    }

    public Map<K, V> getRegistry() {
        return this.registry;
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
