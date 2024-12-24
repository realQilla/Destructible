package net.qilla.destructible.mining;

import net.minecraft.advancements.critereon.ItemDurabilityTrigger;
import net.qilla.destructible.Destructible;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

public class GeneralListener implements Listener {

    private final Destructible plugin;

    public GeneralListener(Destructible plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onDurabilityChange(PlayerItemDamageEvent event) {
        event.setCancelled(true);
    }
}
