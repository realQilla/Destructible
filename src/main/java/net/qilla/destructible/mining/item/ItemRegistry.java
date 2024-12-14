package net.qilla.destructible.mining.item;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public final class ItemRegistry {
    private static ItemRegistry INSTANCE;

    private final HashMap<String, Tool> toolRegistry;

    private ItemRegistry() {
        this.toolRegistry = new HashMap<>();
    }

    public Tool registerItem(@NotNull String id, @NotNull Tool tool) {
        this.toolRegistry.put(id, tool);
        return tool;
    }

    public Tool getTool(String id) {
        return this.toolRegistry.get(id);
    }

    public HashMap<String, Tool> getTools() {
        return this.toolRegistry;
    }

    public static ItemRegistry getInstance() {
        if(INSTANCE == null) return INSTANCE = new ItemRegistry();
        else return INSTANCE;
    }
}