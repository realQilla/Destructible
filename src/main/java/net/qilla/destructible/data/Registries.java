package net.qilla.destructible.data;

import com.mojang.datafixers.util.Pair;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DDrop;
import net.qilla.destructible.mining.item.tool.DTool;
import net.qilla.destructible.mining.player.DMiner;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Registries {

    public static final DestructibleRegistry<ChunkPos, DestructibleRegistry<Integer, String>> DBLOCK_CACHE = new DestructibleRegistry<>();

    public static final DestructibleRegistry<UUID, DMiner> DMINER_DATA = new DestructibleRegistry<>();
    public static final DestructibleRegistry<String, DBlock> DBLOCKS = new DestructibleRegistry<>();
    public static final DestructibleRegistry<String, DDrop> DDROPS = new DestructibleRegistry<>();
    public static final DestructibleRegistry<String, DTool> DTOOLS = new DestructibleRegistry<>();

    public static final DestructibleRegistry<UUID, DBlock> DBLOCK_EDITOR = new DestructibleRegistry<>();
    public static final DestructibleRegistry<UUID, Pair<Player, DestructibleRegistry<ChunkPos, DestructibleRegistry<Integer, Integer>>>> DBLOCK_HIGHLIGHT = new DestructibleRegistry<>();
}