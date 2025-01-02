package net.qilla.destructible.player;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.qilla.destructible.data.ChunkPos;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.data.RegistryMap;
import net.qilla.destructible.util.CoordUtil;
import net.qilla.destructible.util.EntityUtil;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class BlockHighlight {

    private final DPlayer dPlayer;
    private final ServerLevel serverLevel;
    private Set<String> visibleBlocks;
    private final RegistryMap<String, RegistryMap<ChunkPos, RegistryMap<Integer, Integer>>> highlight = new RegistryMap<>();

    public BlockHighlight(@NotNull DPlayer dPlayer) {
        this.dPlayer = dPlayer;
        this.serverLevel = dPlayer.getHandle().serverLevel();
    }

    public boolean isVisibleBlock(@NotNull String blockId) {
        if(this.visibleBlocks == null) return false;
        return this.visibleBlocks.contains(blockId);
    }

    public boolean isAnyBlockVisible() {
        if(this.visibleBlocks == null) return false;
        return !this.visibleBlocks.isEmpty();
    }

    public void addVisibleBlock(@NotNull String blockId) {
        if(this.visibleBlocks == null) this.visibleBlocks = new HashSet<>();
        this.visibleBlocks.add(blockId);
    }

    public void addAllVisibleBlocks() {
        if(this.visibleBlocks == null) this.visibleBlocks = new HashSet<>();
        this.visibleBlocks.addAll(Registries.DESTRUCTIBLE_BLOCKS.keySet());
    }

    public void removeVisibleBlock(@NotNull String blockId) {
        if(this.visibleBlocks == null) return;
        this.visibleBlocks.remove(blockId);
    }

    public void clearVisibleBlocks() {
        if(this.visibleBlocks == null) return;
        this.visibleBlocks.clear();
    }

    public void createHighlight(@NotNull BlockPos blockPos, @NotNull String blockId) {
        if(!this.isVisibleBlock(blockId)) return;

        ChunkPos chunkPos = new ChunkPos(blockPos);
        int chunkInt = CoordUtil.posToChunkLocalPos(blockPos);
        CraftEntity entity = EntityUtil.getHighlight(this.serverLevel);

        this.dPlayer.getHandle().connection.send(new ClientboundAddEntityPacket(entity.getHandle(), 0, blockPos));
        this.dPlayer.getHandle().connection.send(new ClientboundSetEntityDataPacket(entity.getEntityId(), entity.getHandle().getEntityData().packAll()));

        this.highlight.computeIfAbsent(blockId, blockId2 ->
                new RegistryMap<>()).computeIfAbsent(chunkPos, chunkPos2 ->
                new RegistryMap<>()).putIfAbsent(chunkInt, entity.getEntityId());
    }

    public void removeHighlight(@NotNull BlockPos blockPos) {
        ChunkPos chunkPos = new ChunkPos(blockPos);
        int chunkInt = CoordUtil.posToChunkLocalPos(blockPos);

        this.highlight.forEach((blockId, chunkPosMap) -> {
            chunkPosMap.computeIfPresent(chunkPos, (chunkPos2, chunkIntMap) -> {
                chunkIntMap.computeIfPresent(chunkInt, (chunkInt2, entityId) -> {
                    this.dPlayer.getHandle().connection.send(new ClientboundRemoveEntitiesPacket(entityId));
                    return null;
                });
                return chunkIntMap.isEmpty() ? null : chunkIntMap;
            });
        });
    }

    public void createHighlights(@NotNull String blockId) {
        if(!this.isVisibleBlock(blockId)) return;

        var loadedBlocksGrouped = Registries.LOADED_DESTRUCTIBLE_BLOCKS_GROUPED;

        loadedBlocksGrouped.computeIfPresent(blockId, (blockId2, chunkPosMap) -> {
            chunkPosMap.forEach((chunkPos, chunkIntSet) -> {
                chunkIntSet.forEach(chunkInt -> {
                    BlockPos blockPos = CoordUtil.chunkIntToPos(chunkPos, chunkInt);
                    CraftEntity entity = EntityUtil.getHighlight(this.serverLevel);

                    this.dPlayer.getHandle().connection.send(new ClientboundAddEntityPacket(entity.getHandle(), 0, blockPos));
                    this.dPlayer.getHandle().connection.send(new ClientboundSetEntityDataPacket(entity.getEntityId(), entity.getHandle().getEntityData().packAll()));
                    this.highlight.computeIfAbsent(blockId, k4 -> new RegistryMap<>())
                            .computeIfAbsent(chunkPos, k5 -> new RegistryMap<>())
                            .putIfAbsent(chunkInt, entity.getEntityId());
                });
                try {
                    Thread.sleep(10);
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            return chunkPosMap;
        });
    }

    public void removeHighlights(@NotNull String blockId) {
        this.highlight.computeIfPresent(blockId, (k, v) -> {
            v.forEach((k2, v2) -> {
                v2.forEach((k3, v3) -> {
                    this.dPlayer.getHandle().connection.send(new ClientboundRemoveEntitiesPacket(v3));
                });
                try {
                    Thread.sleep(10);
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            return null;
        });
        this.highlight.remove(blockId);
    }

    public void createAllHighlights() {
        var loadedBlocks = Registries.LOADED_DESTRUCTIBLE_BLOCKS_GROUPED;

        loadedBlocks.forEach((blockId, chunkPosMap) -> {
            chunkPosMap.forEach((chunkPos, chunkIntSet) -> {
                chunkIntSet.forEach(chunkInt -> {
                    BlockPos blockPos = CoordUtil.chunkIntToPos(chunkPos, chunkInt);
                    CraftEntity entity = EntityUtil.getHighlight(this.serverLevel);

                    this.dPlayer.getHandle().connection.send(new ClientboundAddEntityPacket(entity.getHandle(), 0, blockPos));
                    this.dPlayer.getHandle().connection.send(new ClientboundSetEntityDataPacket(entity.getEntityId(), entity.getHandle().getEntityData().packAll()));
                    this.highlight.computeIfAbsent(blockId, k4 -> new RegistryMap<>())
                            .computeIfAbsent(chunkPos, k5 -> new RegistryMap<>())
                            .putIfAbsent(chunkInt, entity.getEntityId());
                });
                try {
                    Thread.sleep(10);
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    public void removeAllHighlights() {
        this.highlight.forEach((blockId, chunkPosMap) -> {
            chunkPosMap.forEach((chunkPos, blockIntMap) -> {
                blockIntMap.forEach((blockInt, entityId) -> {
                    dPlayer.getHandle().connection.send(new ClientboundRemoveEntitiesPacket(entityId));
                });
                try {
                    Thread.sleep(10);
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        this.highlight.clear();
    }
}