package net.qilla.destructible.player;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.data.registry.DRegistryMaster;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.util.CoordUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftBlockDisplay;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BlockHighlight {

    private static final int HIGHLIGHT_CREATION_BATCH_SIZE = 1000;
    private static final Map<String, DBlock> DBLOCK_MAP = DRegistry.BLOCKS;

    private final Destructible plugin;
    private final DPlayer dPlayer;
    private final Set<String> visibleDBlocks = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<String, ConcurrentHashMap<Long, ConcurrentHashMap<Integer, Integer>>> highlight = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    public BlockHighlight(@NotNull Destructible plugin, @NotNull DPlayer dPlayer) {
        this.plugin = plugin;
        this.dPlayer = dPlayer;
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
        this.getVisibleDBlocks().addAll(DBLOCK_MAP.keySet());
    }

    public void removeVisibleDBlock(@NotNull String blockId) {
        this.visibleDBlocks.remove(blockId);
    }

    public void removeVisibleDBlockAll() {
        this.visibleDBlocks.clear();
    }

    public void createHighlight(@NotNull BlockPos blockPos, @NotNull String blockId) {
        if(!this.isDBlockVisible(blockId)) return;

        long chunkKey = CoordUtil.getChunkKey(blockPos);
        int chunkInt = CoordUtil.getBlockIndexInChunk(blockPos);
        CraftEntity entity = this.getHighlightEntity();

        highlight.computeIfAbsent(blockId, blockId2 ->
                new ConcurrentHashMap<>()).computeIfAbsent(chunkKey, chunkKey2 ->
                new ConcurrentHashMap<>()).computeIfAbsent(chunkInt, (chunkInt3) -> {
            dPlayer.sendPacket(new ClientboundAddEntityPacket(entity.getHandle(), 0, blockPos));
            dPlayer.sendPacket(new ClientboundSetEntityDataPacket(entity.getEntityId(), entity.getHandle().getEntityData().packAll()));
            return entity.getEntityId();
        });
    }

    public void removeHighlight(@NotNull BlockPos blockPos) {
        long chunkKey = CoordUtil.getChunkKey(blockPos);
        int chunkInt = CoordUtil.getBlockIndexInChunk(blockPos);

        highlight.forEach((blockId, chunkKeyMap) -> {
            chunkKeyMap.computeIfPresent(chunkKey, (chunkKey2, chunkIntMap) -> {
                chunkIntMap.computeIfPresent(chunkInt, (chunkInt2, entityId) -> {
                    dPlayer.sendPacket(new ClientboundRemoveEntitiesPacket(entityId));
                    return null;
                });
                return chunkIntMap.isEmpty() ? null : chunkIntMap;
            });
        });
    }

    public void createHighlights(@NotNull String blockId) {
        this.scheduleTask(() -> {
            if(!this.isDBlockVisible(blockId)) return;
            var loadedBlocksGrouped = DRegistry.LOADED_BLOCKS_GROUPED;

            loadedBlocksGrouped.computeIfPresent(blockId, (blockId2, chunkKeyMap) -> {
                chunkKeyMap.forEach((chunkKey, chunkIntSet) -> {
                    chunkIntSet.forEach(chunkInt -> {
                        BlockPos blockPos = CoordUtil.getBlockPos(chunkKey, chunkInt);
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
                        dPlayer.sendPacket(new ClientboundRemoveEntitiesPacket(v3));
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
                        dPlayer.sendPacket(new ClientboundRemoveEntitiesPacket(entityId));
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
                        dPlayer.sendPacket(new ClientboundRemoveEntitiesPacket(entityId));
                    });
                });
            });
            highlight.clear();
        });
    }

    private CraftEntity getHighlightEntity() {
            CraftBlockDisplay craftEntity = new CraftBlockDisplay(dPlayer.getCraftServer(), new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, dPlayer.getServerLevel()));
            craftEntity.setGlowing(true);
            craftEntity.setGlowColorOverride(Color.SILVER);
            craftEntity.setBlock(Material.LIGHT_GRAY_CONCRETE.createBlockData());
            craftEntity.setTransformation(new Transformation(
                    new Vector3f(0.05f, 0.05f, 0.05f),
                    new Quaternionf(),
                    new Vector3f(0.90f, 0.90f, 0.90f),
                    new Quaternionf()
            ));
        return craftEntity;
    }

    public Set<String> getVisibleDBlocks() {
        return visibleDBlocks;
    }
}