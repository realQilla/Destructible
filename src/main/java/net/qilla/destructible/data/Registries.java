package net.qilla.destructible.data;

import net.qilla.destructible.mining.block.BlockMemory;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.DTool;
import net.qilla.destructible.player.DPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class Registries {
    private Registries() {
    }

    public static final RegistryMap<UUID, DPlayer> DESTRUCTIBLE_PLAYERS = new RegistryMap<>();

    public static final RegistryMap<@NotNull ChunkPos, RegistryMap<Integer, String>> LOADED_DESTRUCTIBLE_BLOCKS = new RegistryMap<>();
    public static final RegistryMap<@NotNull String, RegistryMap<ChunkPos, Set<Integer>>> LOADED_DESTRUCTIBLE_BLOCKS_GROUPED = new RegistryMap<>();
    public static final RegistryMap<@NotNull ChunkPos, RegistryMap<Integer, BlockMemory>> DESTRUCTIBLE_BLOCK_DATA = new RegistryMap<>();

    public static final RegistryMap<@NotNull String, DItem> DESTRUCTIBLE_ITEMS = new RegistryMap<>();
    public static final RegistryMap<@NotNull String, DBlock> DESTRUCTIBLE_BLOCKS = new RegistryMap<>();

    public static final Set<@NotNull DPlayer> DESTRUCTIBLE_BLOCK_EDITORS = new HashSet<>();
}