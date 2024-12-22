package net.qilla.destructible.mining.player;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.ChunkPos;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.data.DestructibleRegistry;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.util.CoordUtil;
import net.qilla.destructible.util.EntityUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class DListener implements Listener {

    private final Destructible plugin;

    public DListener(Destructible plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onBlockPlace(final BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if(!Registries.DBLOCK_EDITOR.containsKey(player.getUniqueId())) return;

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
            ServerLevel serverLevel = serverPlayer.serverLevel();
            DBlock dBlock = Registries.DBLOCK_EDITOR.get(player.getUniqueId());
            ChunkPos chunkPos = new ChunkPos(block.getLocation());
            int chunkInt = CoordUtil.posToChunkInt(block.getLocation());

            Registries.DBLOCK_CACHE.computeIfAbsent(chunkPos, k -> new DestructibleRegistry<>()).put(chunkInt, dBlock.getId());

            DestructibleRegistry<ChunkPos, DestructibleRegistry<Integer, Integer>> viewerMap = Registries.DBLOCK_VIEWER.get(player.getUniqueId());

            if(viewerMap != null) {
                BlockPos blockPos = CoordUtil.locToBlockPos(block.getLocation());
                Entity entity = EntityUtil.getHighlight(serverLevel);

                viewerMap.computeIfAbsent(chunkPos, v -> new DestructibleRegistry<>()).computeIfAbsent(chunkInt, v2 -> entity.getId());
                player.sendMessage("Entity added to map: " + entity.getId() + ": " + viewerMap.size());
                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    serverPlayer.connection.send(new ClientboundAddEntityPacket(entity, entity.getId(), blockPos));
                    serverPlayer.connection.send(new ClientboundSetEntityDataPacket(entity.getId(), entity.getEntityData().packAll()));
                });
            }
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>" + dBlock.getId() + " has been registered!"));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 0.40f, 2.0f);
            });
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onBlockBreak(final BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if(!Registries.DBLOCK_EDITOR.containsKey(player.getUniqueId())) return;
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            ChunkPos chunkPos = new ChunkPos(block.getLocation());
            int chunkInt = CoordUtil.posToChunkInt(block.getLocation());

            Registries.DBLOCK_CACHE.computeIfPresent(chunkPos, (k, v) -> {
                v.computeIfPresent(chunkInt, (k2, v2) -> {
                    Bukkit.getScheduler().runTask(this.plugin, () -> {
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>" + v2 + " been unregistered!"));
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 0.40f, 1.0f);
                    });
                    return null;
                });
                if(v.isEmpty()) return null;
                else return v;
            });
            Registries.DBLOCK_VIEWER.computeIfPresent(player.getUniqueId(), (k, v) -> {
                v.computeIfPresent(chunkPos, (k2, v2) -> {
                    v2.computeIfPresent(chunkInt, (k3, v3) -> {
                        Bukkit.getScheduler().runTask(this.plugin, () -> {
                            ((CraftPlayer) player).getHandle().connection.send(new ClientboundRemoveEntitiesPacket(v3));
                        });
                        return null;
                    });
                    return null;
                });
                return v;
            });
        });
    }
}