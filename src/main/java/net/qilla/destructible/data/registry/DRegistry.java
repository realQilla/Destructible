package net.qilla.destructible.data.registry;

import net.qilla.destructible.mining.block.BlockMemory;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.attributes.AttributeType;
import net.qilla.destructible.player.DPlayer;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface DRegistry<T> extends Iterable<T> {

    DRegistryHolder<Long, Map<Integer, String>> LOADED_BLOCKS = DRegistryMaster.getRegistry(DRegistryKey.LOADED_BLOCKS);

    DRegistryHolder<Long, Map<Integer, BlockMemory>> LOADED_BLOCK_MEMORY = DRegistryMaster.getRegistry(DRegistryKey.LOADED_BLOCK_MEMORY);

    DRegistryHolder<String, Map<Long, Set<Integer>>> LOADED_BLOCKS_GROUPED = DRegistryMaster.getRegistry(DRegistryKey.LOADED_BLOCKS_GROUPED);

    DRegistryHolder<UUID, DPlayer> BLOCK_EDITORS = DRegistryMaster.getRegistry(DRegistryKey.BLOCK_EDITORS);

    DRegistryHolder<String, DItem> ITEMS = DRegistryMaster.getRegistry(DRegistryKey.ITEMS);

    DRegistryHolder<String, DBlock> BLOCKS = DRegistryMaster.getRegistry(DRegistryKey.BLOCKS);

    DRegistryHolder<String, AttributeType<?>> ATTRIBUTES = DRegistryMaster.getRegistry(DRegistryKey.ATTRIBUTES);
}