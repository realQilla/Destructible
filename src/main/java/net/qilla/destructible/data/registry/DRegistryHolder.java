package net.qilla.destructible.data.registry;

import com.google.common.base.Preconditions;
import net.qilla.qlibrary.registry.RegistrySubscriber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DRegistryHolder<K, V> extends ConcurrentHashMap<K, V> {
    private final Set<RegistrySubscriber> subscribers = ConcurrentHashMap.newKeySet();

    @Override
    public V put(@NotNull K key, @NotNull V value) {
        var old = super.put(key, value);
        notifySubscriber();
        return old;
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        super.putAll(m);
        notifySubscriber();
    }

    @Override
    public V remove(@NotNull Object key) {
        var old = super.remove(key);
        notifySubscriber();
        return old;
    }

    @Override
    public void clear() {
        super.clear();
        notifySubscriber();
    }

    @Override
    public V putIfAbsent(@NotNull K key, @NotNull V value) {
        var old = super.putIfAbsent(key, value);
        notifySubscriber();
        return old;
    }

    @Override
    public boolean remove(@NotNull Object key, @NotNull Object value) {
        var old = super.remove(key, value);
        notifySubscriber();
        return old;
    }

    @Override
    public V replace(@NotNull K key, @NotNull V value) {
        var old = super.replace(key, value);
        notifySubscriber();
        return old;
    }

    @Override
    public boolean replace(@NotNull K key, @NotNull V oldValue, @NotNull V newValue) {
        var old = super.replace(key, oldValue, newValue);
        notifySubscriber();
        return old;
    }

    @Override
    public V compute(@NotNull K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        var old = super.compute(key, remappingFunction);
        notifySubscriber();
        return old;
    }

    @Override
    public V computeIfAbsent(@NotNull K key, @NotNull Function<? super K, ? extends V> mappingFunction) {
        var old = super.computeIfAbsent(key, mappingFunction);
        notifySubscriber();
        return old;
    }

    @Override
    public V computeIfPresent(@NotNull K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        var old = super.computeIfPresent(key, remappingFunction);
        notifySubscriber();
        return old;
    }

    @Override
    public V merge(@NotNull K key, @NotNull V value, @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        var old = super.merge(key, value, remappingFunction);
        notifySubscriber();
        return old;
    }

    public void subscribe(@NotNull RegistrySubscriber subscriber) {
        Preconditions.checkNotNull(subscriber, "Subscriber cannot be null");

        synchronized(subscribers) {
            subscribers.add(subscriber);
        }
    }

    public void unsubscribe(@Nullable RegistrySubscriber subscriber) {
        Preconditions.checkNotNull(subscriber, "Subscriber cannot be null");

        synchronized(subscribers) {
            subscribers.remove(subscriber);
        }
    }

    public void notifySubscriber() {
        synchronized(subscribers) {
            for(RegistrySubscriber subscriber : subscribers) {
                subscriber.onUpdate();
            }
        }
    }
}