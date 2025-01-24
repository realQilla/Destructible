package net.qilla.destructible.data.registry;

import com.google.common.base.Preconditions;
import net.qilla.destructible.data.Subscriber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DRegistryHolder<T> {
    private final T registry;
    private final Set<Subscriber> subscribers = ConcurrentHashMap.newKeySet();

    public DRegistryHolder(@NotNull T registry) {
        Preconditions.checkNotNull(registry, "Registry cannot be null");
        this.registry = registry;
    }

    public @NotNull T get() {
        return registry;
    }

    public void subscribe(@NotNull Subscriber subscriber) {
        Preconditions.checkNotNull(subscriber, "Subscriber cannot be null");
        synchronized(subscribers) {
            subscribers.add(subscriber);
        }
    }

    public void unsubscribe(@Nullable Subscriber subscriber) {
        Preconditions.checkNotNull(subscriber, "Subscriber cannot be null");
        synchronized(subscribers) {
            subscribers.remove(subscriber);
        }
    }

    public void notifySubscriber() {
        synchronized(subscribers) {
            for(Subscriber subscriber : subscribers) {
                subscriber.update();
            }
        }
    }
}