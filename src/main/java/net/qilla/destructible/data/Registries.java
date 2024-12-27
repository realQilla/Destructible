package net.qilla.destructible.data;

import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DDrop;
import net.qilla.destructible.mining.item.tool.DTool;
import net.qilla.destructible.mining.MiningCore;
import java.util.UUID;

public final class Registries {
    private Registries() {
    }

    public static final DestructibleRegistry<ChunkPos, DestructibleRegistry<Integer, String>> DESTRUCTIBLE_BLOCKS_CACHE = new DestructibleRegistry<>();

    public static final DestructibleRegistry<ChunkPos, DestructibleRegistry<Integer, DBlockData>> DESTRUCTIBLE_BLOCK_DATA = new DestructibleRegistry<>();

    public static final DestructibleRegistry<UUID, MiningCore> DESTRUCTIBLE_MINERS_DATA = new DestructibleRegistry<>();

    public static final DestructibleRegistry<String, DBlock> DESTRUCTIBLE_BLOCKS = new DestructibleRegistry<>();

    public static final DestructibleRegistry<String, DDrop> DESTRUCTIBLE_DROPS = new DestructibleRegistry<>();

    public static final DestructibleRegistry<String, DTool> DESTRUCTIBLE_TOOLS = new DestructibleRegistry<>();

    public static final DestructibleRegistry<UUID, DBlockEditor> DESTRUCTIBLE_BLOCK_EDITORS = new DestructibleRegistry<>();
}