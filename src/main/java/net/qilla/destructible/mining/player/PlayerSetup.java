package net.qilla.destructible.mining.player;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.player.data.PlayerData;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerSetup implements Listener {
    private final Destructible plugin;

    public PlayerSetup(Destructible plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void playerJoin(final PlayerJoinEvent event) {
        Player player = event.getPlayer();

        initPlayer(player);
    }

    @EventHandler
    private void playerLeave(final PlayerQuitEvent event) {
        Player player = event.getPlayer();

        removePlayer(player);
    }

    public void initPlayer(final Player player) {
        Registries.PLAYER_DATA.register(player.getUniqueId(), new PlayerData(player));
        this.plugin.getPlayerPacketListener().addListener(player);

        player.getAttribute(Attribute.BLOCK_BREAK_SPEED).setBaseValue(0.0);
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have been registered!"));
    }

    public void removePlayer(final Player player) {
        Registries.PLAYER_DATA.unregister(player.getUniqueId());
        this.plugin.getPlayerPacketListener().removeListener(player);
    }
}
