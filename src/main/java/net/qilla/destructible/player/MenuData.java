package net.qilla.destructible.player;

import com.google.common.base.Preconditions;
import net.qilla.destructible.data.MenuMetadata;
import net.qilla.destructible.menugeneral.StaticMenu;
import net.qilla.destructible.menugeneral.MenuSave;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class MenuData {
    private final Deque<StaticMenu> menuHistory;
    private final EnumMap<MenuSave, MenuMetadata> menuMetadata;
    private CompletableFuture<String> playerInput;

    public MenuData() {
        this.menuHistory = new ArrayDeque<>();
        this.menuMetadata = new EnumMap<>(MenuSave.class);
        this.playerInput = null;
    }

    public Optional<StaticMenu> popFromHistory() {
        if(!menuHistory.isEmpty()) {
            menuHistory.pop();
            return Optional.ofNullable(menuHistory.peek());
        } else return Optional.empty();
    }

    public Optional<StaticMenu> peekFromHistory() {
        if(!menuHistory.isEmpty()) return Optional.ofNullable(menuHistory.peek());
        else return Optional.empty();
    }

    public void pushToHistory(@NotNull StaticMenu menu) {
        Preconditions.checkNotNull(menu, "Menu cannot be null");
        menuHistory.push(menu);
    }

    public void clearHistory() {
        menuHistory.clear();
    }

    public MenuMetadata menuData(MenuSave menuSave) {
        return menuMetadata.computeIfAbsent(menuSave, k -> new MenuMetadata());
    }

    public void clearMenuData(MenuSave menuSave) {
        menuMetadata.remove(menuSave);
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