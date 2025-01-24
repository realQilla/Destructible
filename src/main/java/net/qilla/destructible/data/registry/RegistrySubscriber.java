package net.qilla.destructible.data.registry;

public class RegistrySubscriber<K, V> {

    private final RegistryEvent type;
    private final K key;
    private final V value;

    public RegistrySubscriber(RegistryEvent type, K key, V value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }

    public RegistryEvent getType() {
        return type;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}