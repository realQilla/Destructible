package net.qilla.destructible.data.registry;

import net.qilla.destructible.Destructible;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.player.DPlayerData;
import net.qilla.qlibrary.data.PlayerDataRegistry;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DPlayerDataRegistry implements PlayerDataRegistry<DPlayerData> {

    private static final Plugin PLUGIN = Destructible.getInstance();
    private static final DPlayerDataRegistry INSTANCE = new DPlayerDataRegistry();
    private final Map<UUID, DPlayerData> playerDataRegistry = new ConcurrentHashMap<>();

    public static DPlayerDataRegistry getInstance() {
        return INSTANCE;
    }

    private DPlayerDataRegistry() {
    }

    @Override
    public @NotNull DPlayerData getData(@NotNull Player player) {
        DPlayerData playerData = playerDataRegistry.get(player.getUniqueId());

        if(playerData == null) {
            playerDataRegistry.put(player.getUniqueId(), new DPlayerData(new DPlayer((CraftPlayer) player), PLUGIN));
        } else if(!playerData.getPlayer().isConnected()) {
            playerDataRegistry.put(player.getUniqueId(), new DPlayerData(new DPlayer((CraftPlayer) player), playerData));
        }
        return playerDataRegistry.get(player.getUniqueId());
    }

    public @Nullable DPlayerData getData(@NotNull UUID uuid) {
        return playerDataRegistry.get(uuid);
    }

    @Override
    public @Nullable DPlayerData setData(@NotNull UUID uuid, @NotNull DPlayerData dPlayerData) {
        return playerDataRegistry.put(uuid, dPlayerData);
    }

    @Override
    public boolean hasData(@NotNull UUID uuid) {
        return playerDataRegistry.containsKey(uuid);
    }

    @Override
    public @Nullable DPlayerData clearData(@NotNull UUID uuid) {
        return playerDataRegistry.remove(uuid);
    }
}
