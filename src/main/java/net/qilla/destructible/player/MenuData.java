package net.qilla.destructible.player;

import net.qilla.destructible.gui.DestructibleMenu;

import java.util.ArrayDeque;
import java.util.Deque;

public class MenuData {
    private final Deque<DestructibleMenu> menuHistory;
    private String signText = null;

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

    public void setSignText(String signText) {
        this.signText = signText;
    }

    public String getSignText() {
        return this.signText;
    }

    public void clearSignText() {
        this.signText = null;
    }
}
