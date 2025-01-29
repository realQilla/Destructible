package net.qilla.destructible.data.registry;

import net.qilla.destructible.mining.block.BlockMemory;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.attributes.AttributeType;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public sealed interface DRegistryKey<T> permits DRegistryKeyImpl {

    DRegistryKey<Map<Long, Map<Integer, String>>> LOADED_BLOCKS = DRegistryKeyImpl.create("loaded_blocks");

    DRegistryKey<Map<Long, Map<Integer, BlockMemory>>> LOADED_BLOCK_MEMORY = DRegistryKeyImpl.create("loaded_block_memory");

    DRegistryKey<Map<String, Map<Long, Set<Integer>>>> LOADED_BLOCKS_GROUPED = DRegistryKeyImpl.create("loaded_blocks_grouped");

    DRegistryKey<Set<UUID>> BLOCK_EDITORS = DRegistryKeyImpl.create("block_editors");

    DRegistryKey<Map<String, DItem>> ITEMS = DRegistryKeyImpl.create("items");

    DRegistryKey<Map<String, DBlock>> BLOCKS = DRegistryKeyImpl.create("blocks");

    DRegistryKey<Map<String, AttributeType<?>>> ATTRIBUTES = DRegistryKeyImpl.create("attributes");
}
