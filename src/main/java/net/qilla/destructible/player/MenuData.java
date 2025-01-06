package net.qilla.destructible.player;

import net.qilla.destructible.menus.DestructibleMenu;

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
            return menuHistory.peekLast();
        }
        return null;
    }

    public void addMenu(DestructibleMenu menu) {
        menuHistory.push(menu);
    }

    public void clearHistory() {
        menuHistory.clear();
    }

    public Future<String> requestInput() {
        return this.playerInput = new CompletableFuture<>();
    }

    public void fulfillInput(String input) {
        if(this.playerInput != null) {
            this.playerInput.complete(input);
            this.playerInput = null;
        }
    }
}