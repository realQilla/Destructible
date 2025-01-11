package net.qilla.destructible.player;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.qilla.destructible.data.ChunkPos;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.util.CoordUtil;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftBlockDisplay;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BlockHighlight {

    private final DPlayer dPlayer;
    private Set<String> visibleBlocks;
    private final ConcurrentHashMap<String, ConcurrentHashMap<ChunkPos, ConcurrentHashMap<Integer, Integer>>> highlight = new ConcurrentHashMap<>();

    public BlockHighlight(@NotNull DPlayer dPlayer) {
        this.dPlayer = dPlayer;
    }

    public boolean isBlockVisible(@NotNull String blockId) {
        if(this.visibleBlocks == null) {
            return false;
        }
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
        if(!this.isBlockVisible(blockId)) return;

        ChunkPos chunkPos = new ChunkPos(blockPos);
        int chunkInt = CoordUtil.posToChunkLocalPos(blockPos);
        CraftEntity entity = this.getHighlight();

        dPlayer.sendPacket(new ClientboundAddEntityPacket(entity.getHandle(), 0, blockPos));
        dPlayer.sendPacket(new ClientboundSetEntityDataPacket(entity.getEntityId(), entity.getHandle().getEntityData().packAll()));

        this.highlight.computeIfAbsent(blockId, blockId2 ->
                new ConcurrentHashMap<>()).computeIfAbsent(chunkPos, chunkPos2 ->
                new ConcurrentHashMap<>()).putIfAbsent(chunkInt, entity.getEntityId());
    }

    public void removeHighlight(@NotNull BlockPos blockPos) {
        ChunkPos chunkPos = new ChunkPos(blockPos);
        int chunkInt = CoordUtil.posToChunkLocalPos(blockPos);

        this.highlight.forEach((blockId, chunkPosMap) -> {
            chunkPosMap.computeIfPresent(chunkPos, (chunkPos2, chunkIntMap) -> {
                chunkIntMap.computeIfPresent(chunkInt, (chunkInt2, entityId) -> {
                    dPlayer.sendPacket(new ClientboundRemoveEntitiesPacket(entityId));
                    return null;
                });
                return chunkIntMap.isEmpty() ? null : chunkIntMap;
            });
        });
    }

    public void createHighlights(@NotNull String blockId) {
        if(!this.isBlockVisible(blockId)) return;

        var loadedBlocksGrouped = Registries.LOADED_DESTRUCTIBLE_BLOCKS_GROUPED;

        loadedBlocksGrouped.computeIfPresent(blockId, (blockId2, chunkPosMap) -> {
            chunkPosMap.forEach((chunkPos, chunkIntSet) -> {
                chunkIntSet.forEach(chunkInt -> {
                    BlockPos blockPos = CoordUtil.chunkIntToPos(chunkPos, chunkInt);
                    CraftEntity entity = this.getHighlight();

                    dPlayer.sendPacket(new ClientboundAddEntityPacket(entity.getHandle(), 0, blockPos));
                    dPlayer.sendPacket(new ClientboundSetEntityDataPacket(entity.getEntityId(), entity.getHandle().getEntityData().packAll()));
                    this.highlight.computeIfAbsent(blockId, k4 -> new ConcurrentHashMap<>())
                            .computeIfAbsent(chunkPos, k5 -> new ConcurrentHashMap<>())
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
                    dPlayer.sendPacket(new ClientboundRemoveEntitiesPacket(v3));
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
                    CraftEntity entity = this.getHighlight();

                    dPlayer.sendPacket(new ClientboundAddEntityPacket(entity.getHandle(), 0, blockPos));
                    dPlayer.sendPacket(new ClientboundSetEntityDataPacket(entity.getEntityId(), entity.getHandle().getEntityData().packAll()));
                    this.highlight.computeIfAbsent(blockId, k4 -> new ConcurrentHashMap<>())
                            .computeIfAbsent(chunkPos, k5 -> new ConcurrentHashMap<>())
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
                    dPlayer.sendPacket(new ClientboundRemoveEntitiesPacket(entityId));
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

    public CraftEntity getHighlight() {
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
}