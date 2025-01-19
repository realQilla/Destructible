package net.qilla.destructible.data;

import net.qilla.destructible.mining.block.BlockMemory;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.player.DPlayer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public final class DRegistry {
    private DRegistry() {
    }

    public static final java.util.concurrent.ConcurrentHashMap<UUID, DPlayer> DESTRUCTIBLE_PLAYERS = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<Long, ConcurrentHashMap<Integer, String>> LOADED_DESTRUCTIBLE_BLOCKS = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, ConcurrentHashMap<Long, Set<Integer>>> LOADED_DESTRUCTIBLE_BLOCKS_GROUPED = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<Long, ConcurrentHashMap<Integer, BlockMemory>> DESTRUCTIBLE_BLOCK_DATA = new ConcurrentHashMap<>();

    public static final ConcurrentSkipListMap<String, DItem> DESTRUCTIBLE_ITEMS = new ConcurrentSkipListMap<>();

    public static <T extends DItem> List<T> getDestructibleItem(Class<T> clazz) {
        return new AbstractList<T>() {
            @Override
            public T get(int index) {
                return clazz.cast(DESTRUCTIBLE_ITEMS.values().stream()
                        .filter(item -> item.getClass() == clazz)
                        .skip(index)
                        .findFirst()
                        .orElseThrow(IndexOutOfBoundsException::new));
            }

            @Override
            public int size() {
                return (int) DESTRUCTIBLE_ITEMS.values().stream()
                        .filter(item -> item.getClass() == clazz)
                        .count();
            }
        };
    }

    public static final ConcurrentSkipListMap<String, DBlock> DESTRUCTIBLE_BLOCKS = new ConcurrentSkipListMap<>();

    public static final Set<DPlayer> DESTRUCTIBLE_BLOCK_EDITORS = new HashSet<>();
}