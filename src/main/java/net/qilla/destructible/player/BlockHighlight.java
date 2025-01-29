package net.qilla.destructible.player;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.util.NMSUtil;
import net.qilla.qlibrary.util.tools.CoordUtil;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BlockHighlight {

    private static final int HIGHLIGHT_CREATION_BATCH_SIZE = 1000;

    private final Plugin plugin;
    private final DPlayer player;
    private final Set<String> visibleDBlocks = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<String, Map<Long, Map<Integer, Integer>>> highlight = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    public BlockHighlight(@NotNull Plugin plugin, @NotNull DPlayer player) {
        this.plugin = plugin;
        this.player = player;
    }

    private void scheduleTask(Runnable task) {
        taskQueue.add(task);
        processingQueue();
    }

    private void processingQueue() {
        if (isProcessing.compareAndSet(false, true)) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    Runnable task;
                    while ((task = taskQueue.poll()) != null) {
                        task.run();
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Error while processing task queue", e);
                } finally {
                    isProcessing.set(false);

                    if (!taskQueue.isEmpty()) {
                        processingQueue();
                    }
                }
            });
        }
    }

    public boolean isDBlockVisible(@NotNull String blockId) {
        return this.visibleDBlocks.contains(blockId);
    }

    public boolean isDBlockVisibleAny() {
        return !this.visibleDBlocks.isEmpty();
    }

    public void addVisibleDBlock(@NotNull String blockId) {
        this.getVisibleDBlocks().add(blockId);
    }

    public void addVisibleDBlockAll() {
        this.getVisibleDBlocks().addAll(DRegistry.BLOCKS.keySet());
    }

    public void removeVisibleDBlock(@NotNull String blockId) {
        this.visibleDBlocks.remove(blockId);
    }

    public void removeVisibleDBlockAll() {
        this.visibleDBlocks.clear();
    }

    public void createHighlight(@NotNull BlockPos blockPos, @NotNull String blockId) {
        if(!this.isDBlockVisible(blockId)) return;

        long originChunkKey = CoordUtil.getChunkKey(blockPos);
        int originSubChunkKey = CoordUtil.getSubChunkKey(blockPos);
        CraftEntity entity = NMSUtil.createHighlightEntity(player.getWorld());

        highlight.computeIfAbsent(blockId, id ->
                new HashMap<>()).computeIfAbsent(originChunkKey, chunkKey ->
                new HashMap<>()).computeIfAbsent(originSubChunkKey, subChunkKey -> {

            player.sendPacket(new ClientboundAddEntityPacket(entity.getHandle(), 0, blockPos));
            player.sendPacket(new ClientboundSetEntityDataPacket(entity.getEntityId(), entity.getHandle().getEntityData().packAll()));
            return entity.getEntityId();
        });
    }

    public void removeHighlight(@NotNull BlockPos blockPos) {
        long originChunkKey = CoordUtil.getChunkKey(blockPos);
        int originSubChunkKey = CoordUtil.getSubChunkKey(blockPos);

        highlight.forEach((blockId, chunkKeyMap) ->
                chunkKeyMap.computeIfPresent(originChunkKey, (chunkKey, subChunkKeyMap) -> {
                    subChunkKeyMap.computeIfPresent(originSubChunkKey, (chunkInt, entityId) -> {
                        player.sendPacket(new ClientboundRemoveEntitiesPacket(entityId));
                        return null;
                    });
                    return subChunkKeyMap.isEmpty() ? null : subChunkKeyMap;
                }));
    }

    public void createHighlights(@NotNull String blockId) {
        if(!this.isDBlockVisible(blockId)) return;

        this.scheduleTask(() -> {
            var loadedBlocksGrouped = DRegistry.LOADED_BLOCKS_GROUPED;

            loadedBlocksGrouped.computeIfPresent(blockId, (id, chunkKeyMap) -> {
                chunkKeyMap.forEach((chunkKey, chunkIntSet) -> {
                    chunkIntSet.forEach(subChunkKey -> {
                        BlockPos blockPos = CoordUtil.getBlockPos(chunkKey, subChunkKey);
                        this.createHighlight(blockPos, blockId);
                    });
                });
                return chunkKeyMap;
            });
        });
    }

    public void removeHighlights(@NotNull String blockId) {
        this.scheduleTask(() -> {
            highlight.computeIfPresent(blockId, (k, v) -> {
                v.forEach((k2, v2) -> {
                    v2.forEach((k3, v3) -> {
                        player.sendPacket(new ClientboundRemoveEntitiesPacket(v3));
                    });
                });
                return null;
            });
            highlight.remove(blockId);
        });
    }

    public void createHighlights(long chunkKey) {
        this.scheduleTask(() -> {
            var loadedBlocks =  DRegistry.LOADED_BLOCKS;

            loadedBlocks.get(chunkKey).forEach((index, blockId) -> {
                BlockPos blockPos = CoordUtil.getBlockPos(chunkKey, index);
                this.createHighlight(blockPos, blockId);
            });
        });
    }

    public void removeHighlights(long chunkKey) {
        this.scheduleTask(() -> {
            highlight.forEach((blockId, chunkKeyMap) -> {
                chunkKeyMap.computeIfPresent(chunkKey, (chunkKey2, chunkIntMap) -> {
                    chunkIntMap.forEach((chunkInt, entityId) -> {
                        player.sendPacket(new ClientboundRemoveEntitiesPacket(entityId));
                    });
                    return null;
                });
            });
        });
    }

    public void createVisibleHighlights() {
        this.scheduleTask(() -> {
            var loadedBlocksGrouped =  DRegistry.LOADED_BLOCKS_GROUPED;

            visibleDBlocks.forEach(blockId -> {
                loadedBlocksGrouped.computeIfPresent(blockId, (id, chunkKeyMap) -> {
                    chunkKeyMap.entrySet().stream()
                            .flatMap(entry -> entry.getValue().stream().map(chunkInt -> CoordUtil.getBlockPos(entry.getKey(), chunkInt)))
                            .collect(Collectors.groupingBy(pos -> pos.hashCode() % HIGHLIGHT_CREATION_BATCH_SIZE))
                            .forEach((batchId, batch) -> scheduleTask(() -> {
                                batch.forEach(pos -> createHighlight(pos, blockId));
                            }));
                    return chunkKeyMap;
                });
            });
        });
    }

    public void removeHighlightsAll() {
        this.scheduleTask(() -> {

            highlight.forEach((blockId, chunkKeyMap) -> {
                chunkKeyMap.forEach((chunkKey, localIndexMap) -> {
                    localIndexMap.forEach((localIndex, entityId) -> {
                        player.sendPacket(new ClientboundRemoveEntitiesPacket(entityId));
                    });
                });
            });
            highlight.clear();
        });
    }

    public Set<String> getVisibleDBlocks() {
        return visibleDBlocks;
    }
}