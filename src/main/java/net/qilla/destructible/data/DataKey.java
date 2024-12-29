package net.qilla.destructible.data;

import net.qilla.destructible.Destructible;
import org.bukkit.NamespacedKey;

public final class DataKey {
    private static final Destructible PLUGIN = Destructible.getInstance();

    public static final NamespacedKey DESTRUCTIBLE = new NamespacedKey(PLUGIN, "destructible");
    public static final NamespacedKey DESTRUCTIBLE_ID = new NamespacedKey(PLUGIN, "tool");
    public static final NamespacedKey DURABILITY = new NamespacedKey(PLUGIN, "durability");
}
