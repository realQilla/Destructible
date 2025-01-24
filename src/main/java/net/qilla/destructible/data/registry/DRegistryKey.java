package net.qilla.destructible.data.registry;

import net.qilla.destructible.mining.block.BlockMemory;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.attributes.AttributeType;
import net.qilla.destructible.player.DPlayer;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public sealed interface DRegistryKey<T> permits DRegistryKeyImpl {

    DRegistryKey<Map<Long, ConcurrentHashMap<Integer, String>>> LOADED_BLOCKS = DRegistryKeyImpl.create("loaded_blocks");

    DRegistryKey<Map<Long, ConcurrentHashMap<Integer, BlockMemory>>> LOADED_BLOCK_MEMORY = DRegistryKeyImpl.create("loaded_block_memory");

    DRegistryKey<Map<String, ConcurrentHashMap<Long, Set<Integer>>>> LOADED_BLOCKS_GROUPED = DRegistryKeyImpl.create("loaded_blocks_grouped");

    DRegistryKey<Map<UUID, DPlayer>> PLAYER_DATA = DRegistryKeyImpl.create("player_data");

    DRegistryKey<Set<DPlayer>> BLOCK_EDITORS = DRegistryKeyImpl.create("block_editors");

    DRegistryKey<ConcurrentSkipListMap<String, DItem>> ITEMS = DRegistryKeyImpl.create("items");

    DRegistryKey<ConcurrentSkipListMap<String, DBlock>> BLOCKS = DRegistryKeyImpl.create("blocks");

    DRegistryKey<Map<String, AttributeType<?>>> ATTRIBUTES = DRegistryKeyImpl.create("attributes");
}
