package net.qilla.destructible.player;

import net.qilla.destructible.gui.DestructibleMenu;

import java.util.ArrayDeque;
import java.util.Deque;

public class MenuData {
    private final Deque<DestructibleMenu> menuHistory;

    public MenuData() {
        this.menuHistory = new ArrayDeque<>();
    }

    public DestructibleMenu getLastMenu() {
        if(!menuHistory.isEmpty()) {
            menuHistory.pop();
            if(!menuHistory.isEmpty()) {
                return menuHistory.peekLast();
            }
        }
        return null;
    }

    public void addMenu(DestructibleMenu menu) {
        menuHistory.push(menu);
    }

    public void clearHistory() {
        menuHistory.clear();
    }
}
