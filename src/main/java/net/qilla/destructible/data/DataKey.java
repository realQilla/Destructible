package net.qilla.destructible.data;

import net.qilla.destructible.Destructible;
import org.bukkit.NamespacedKey;

public class DataKey {
    public static NamespacedKey TOOL = new NamespacedKey(Destructible.getInstance(), "tool");
    public static NamespacedKey DURABILITY = new NamespacedKey(Destructible.getInstance(), "durability");
}
