package net.qilla.destructible.mining;

import net.qilla.destructible.Destructible;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GeneralListener implements Listener {

    private final Destructible plugin;

    public GeneralListener(Destructible plugin) {
        this.plugin = plugin;
    }
}
