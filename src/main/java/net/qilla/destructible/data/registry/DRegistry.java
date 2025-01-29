package net.qilla.destructible.data.registry;

import net.qilla.destructible.mining.block.BlockMemory;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.attributes.AttributeType;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface DRegistry<T> extends Iterable<T> {

    Map<Long, Map<Integer, String>> LOADED_BLOCKS = DRegistryMaster.getRegistry(DRegistryKey.LOADED_BLOCKS);

    Map<Long, Map<Integer, BlockMemory>> LOADED_BLOCK_MEMORY = DRegistryMaster.getRegistry(DRegistryKey.LOADED_BLOCK_MEMORY);

    Map<String, Map<Long, Set<Integer>>> LOADED_BLOCKS_GROUPED = DRegistryMaster.getRegistry(DRegistryKey.LOADED_BLOCKS_GROUPED);

    Set<UUID> BLOCK_EDITORS = DRegistryMaster.getRegistry(DRegistryKey.BLOCK_EDITORS);

    Map<String, DItem> ITEMS = DRegistryMaster.getRegistry(DRegistryKey.ITEMS);

    Map<String, DBlock> BLOCKS = DRegistryMaster.getRegistry(DRegistryKey.BLOCKS);

    Map<String, AttributeType<?>> ATTRIBUTES = DRegistryMaster.getRegistry(DRegistryKey.ATTRIBUTES);
}