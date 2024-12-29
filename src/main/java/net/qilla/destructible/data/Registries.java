package net.qilla.destructible.data;

import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.DTool;
import net.qilla.destructible.mining.MiningCore;
import java.util.UUID;

public final class Registries {
    private Registries() {
    }

    public static final DestructibleRegistry<ChunkPos, DestructibleRegistry<Integer, String>> DESTRUCTIBLE_BLOCKS_CACHE = new DestructibleRegistry<>();

    public static final DestructibleRegistry<ChunkPos, DestructibleRegistry<Integer, BlockMemory>> DESTRUCTIBLE_BLOCK_DATA = new DestructibleRegistry<>();

    public static final DestructibleRegistry<UUID, MiningCore> DESTRUCTIBLE_MINERS_DATA = new DestructibleRegistry<>();

    public static final DestructibleRegistry<String, DItem> DESTRUCTIBLE_ITEMS = new DestructibleRegistry<>();

    public static final DestructibleRegistry<String, DBlock> DESTRUCTIBLE_BLOCKS = new DestructibleRegistry<>();

    public static final DestructibleRegistry<String, DTool> DESTRUCTIBLE_TOOLS = new DestructibleRegistry<>();

    public static final DestructibleRegistry<UUID, DBlockEditor> DESTRUCTIBLE_BLOCK_EDITORS = new DestructibleRegistry<>();
}