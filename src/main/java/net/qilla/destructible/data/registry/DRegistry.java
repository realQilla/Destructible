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

public interface DRegistry<T> extends Iterable<T> {

    Map<Long, ConcurrentHashMap<Integer, String>> LOADED_BLOCKS = DRegistryMaster.getRegistry(DRegistryKey.LOADED_BLOCKS);

    Map<Long, ConcurrentHashMap<Integer, BlockMemory>> LOADED_BLOCK_MEMORY = DRegistryMaster.getRegistry(DRegistryKey.LOADED_BLOCK_MEMORY);

    Map<String, ConcurrentHashMap<Long, Set<Integer>>> LOADED_BLOCKS_GROUPED = DRegistryMaster.getRegistry(DRegistryKey.LOADED_BLOCKS_GROUPED);

    Map<UUID, DPlayer> DPLAYERS = DRegistryMaster.getRegistry(DRegistryKey.PLAYER_DATA);

    Set<DPlayer> BLOCK_EDITORS = DRegistryMaster.getRegistry(DRegistryKey.BLOCK_EDITORS);

    ConcurrentSkipListMap<String, DItem> ITEMS = DRegistryMaster.getRegistry(DRegistryKey.ITEMS);

    ConcurrentSkipListMap<String, DBlock> BLOCKS = DRegistryMaster.getRegistry(DRegistryKey.BLOCKS);

    Map<String, AttributeType<?>> ATTRIBUTES = DRegistryMaster.getRegistry(DRegistryKey.ATTRIBUTES);
}