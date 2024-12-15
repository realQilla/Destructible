package net.qilla.destructible.data;

import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DDrop;
import net.qilla.destructible.mining.item.tool.DTool;
import net.qilla.destructible.mining.player.data.PlayerData;
import org.bukkit.Material;

import java.util.UUID;

public class Registries {
    public static final Registry<UUID, PlayerData> PLAYER_DATA = createRegistry(UUID.class, PlayerData.class);
    public static final Registry<Material, DBlock> BLOCKS = createRegistry(Material.class, DBlock.class);
    public static final Registry<String, DDrop> DROPS = createRegistry(String.class, DDrop.class);
    public static final Registry<String, DTool> TOOLS = createRegistry(String.class, DTool.class);

    private static <K, V> Registry<K, V> createRegistry(Class<K> key, Class<V> type) {
        return new Registry<>();
    }
}