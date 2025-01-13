package net.qilla.destructible.player;

import com.google.common.base.Preconditions;
import net.qilla.destructible.menus.DestructibleMenu;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class MenuData {
    private final Deque<DestructibleMenu> menuHistory;
    private CompletableFuture<String> playerInput = null;

    public MenuData() {
        this.menuHistory = new ArrayDeque<>();
    }

    public DestructibleMenu getLastMenu() {
        if(!menuHistory.isEmpty()) {
            menuHistory.pop();
            return menuHistory.peek();
        }
        return null;
    }

    public void addHistory(@NotNull DestructibleMenu menu) {
        Preconditions.checkNotNull(menu, "Menu cannot be null");
        menuHistory.push(menu);
    }

    public void clearHistory() {
        menuHistory.clear();
    }

    public Future<String> requestInput() {
        return this.playerInput = new CompletableFuture<>();
    }

    public void fulfillInput(String input) {
        if(playerInput == null) return;
        playerInput.complete(input);
        playerInput = null;
    }
}