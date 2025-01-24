package net.qilla.destructible.data;

import net.qilla.destructible.Destructible;
import org.bukkit.NamespacedKey;

public final class DataKey {

    private DataKey() {
    }

    private static final Destructible PLUGIN = Destructible.getInstance();

    public static final NamespacedKey GUI_ITEM = new NamespacedKey(PLUGIN, "gui_item");
    public static final NamespacedKey DESTRUCTIBLE_ITEM = new NamespacedKey(PLUGIN, "destructrible_item");
}
