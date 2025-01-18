package net.qilla.destructible.menugeneral;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class MenuHolder<T extends StaticMenu> {

    private final Deque<T> menuHistory;
    private CompletableFuture<String> future;

    public MenuHolder() {
        this.menuHistory = new ArrayDeque<>();
        this.future = null;
    }

    public void newMenu(T menu) {
        this.resetMenuHistory();
        this.future = null;
        menu.open(true);
    }

    public Optional<T> popFromHistory() {
        if(!menuHistory.isEmpty()) {
            menuHistory.pop();
        }
        return Optional.ofNullable(menuHistory.peek());
    }

    public Optional<T> peekFromHistory() {
        return Optional.ofNullable(menuHistory.peek());
    }

    public void pushToHistory(@NotNull T menu) {
        Preconditions.checkNotNull(menu, "Menu cannot be null");
        menuHistory.push(menu);
    }

    public void resetMenuHistory() {
        menuHistory.clear();
    }

    public @NotNull Future<String> requestInput() {
        if (future != null && !future.isDone()) {
            throw new IllegalStateException("Input request already in progress");
        }
        future = new CompletableFuture<>();
        return future;
    }

    public boolean hasInput() {
        return future != null && !future.isDone();
    }

    public boolean fulfillInput(@NotNull String input) {
        if (future == null || future.isDone()) {
            return false;
        }
        future.complete(input);
        future = null;
        return true;
    }
}
