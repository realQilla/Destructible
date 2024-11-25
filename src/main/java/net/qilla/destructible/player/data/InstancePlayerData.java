package net.qilla.destructible.player.data;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public final class InstancePlayerData {

    private final HashMap<UUID, PlayerData> dataHashMap;

    public InstancePlayerData() {
        this.dataHashMap = new HashMap<>();
    }

    public void addPlayerData(@NotNull final Player player) {
        if (this.dataHashMap.containsKey(player.getUniqueId())) return;
        else dataHashMap.put(player.getUniqueId(), new PlayerData(player));
    }

    public PlayerData getPlayerData(@NotNull final Player player) {
        return this.dataHashMap.get(player.getUniqueId());
    }

    public boolean removePlayerData(@NotNull final Player player) {
        return this.dataHashMap.remove(player.getUniqueId()) != null;
    }

    public boolean hasPlayerData(@NotNull final Player player) {
        return this.dataHashMap.containsKey(player.getUniqueId());
    }

}
