package net.qilla.destructible.mining.player;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.ChunkPos;
import net.qilla.destructible.data.EditorSettings;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.data.DestructibleRegistry;
import net.qilla.destructible.util.CoordUtil;
import net.qilla.destructible.util.EntityUtil;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.stream.Collectors;

public class DListener implements Listener {

    private static final int MAX_RECURSION_SIZE = 8192;
    private final Destructible plugin;

    public DListener(Destructible plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onBlockPlace(final BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();
        BlockPos blockPos = CoordUtil.locToBlockPos(location);
        EditorSettings editorSettings = Registries.DBLOCK_EDITOR.get(player.getUniqueId());

        if(editorSettings == null || editorSettings.getDblock() == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            if(!editorSettings.isRecursive()) {
                this.blockLogic(blockPos, player, editorSettings);
                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Destructible block <gold>" + editorSettings.getDblock().getId() + "</gold> has been cached!"));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 0.40f, 2.0f);
                });
            } else {
                editorSettings.setLockHighlight(true);
                Set<BlockPos> recursiveSet = getRecursiveSet(blockPos, location.getWorld(), event.getBlock().getType());
                for(BlockPos curBlockPos : recursiveSet) {
                    blockLogic(curBlockPos, player, editorSettings);
                }
                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Destructible recursive operation completed, <gold>" + recursiveSet.size() + "</gold> block(s) cached as <gold>" + editorSettings.getDblock().getId() + "</gold>!"));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 0.80f, 0.0f);
                    editorSettings.setLockHighlight(false);
                });
            }
        });
    }

    private Set<BlockPos> getRecursiveSet(BlockPos blockPos, World world, Material material) {
        Set<BlockPos> posList = new HashSet<>();
        Queue<BlockPos> recursivePos = new LinkedList<>();
        recursivePos.add(blockPos);
        posList.add(blockPos);

        while(!recursivePos.isEmpty() && posList.size() < MAX_RECURSION_SIZE) {
            BlockPos currentPos = recursivePos.poll();
            BlockPos[] directions = {
                    currentPos.offset(1, 0, 0),
                    currentPos.offset(-1, 0, 0),
                    currentPos.offset(0, 1, 0),
                    currentPos.offset(0, -1, 0),
                    currentPos.offset(0, 0, 1),
                    currentPos.offset(0, 0, -1)
            };

            for(BlockPos newPos : directions) {
                if(posList.contains(newPos)) continue;
                Block block = world.getBlockAt(newPos.getX(), newPos.getY(), newPos.getZ());
                if(!block.getType().equals(material)) continue;
                posList.add(newPos);
                recursivePos.add(newPos);
            }
        }
        return posList.stream()
                .sorted(Comparator.comparingInt(pos -> Math.max(Math.max(Math.abs(pos.getX() - blockPos.getX()),
                                Math.abs(pos.getY() - blockPos.getY())),
                        Math.abs(pos.getZ() - blockPos.getZ()))))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void blockLogic(BlockPos blockPos, Player player, EditorSettings editorSettings) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ServerLevel serverLevel = serverPlayer.serverLevel();
        ChunkPos chunkPos = new ChunkPos(blockPos);
        int chunkInt = CoordUtil.posToChunkInt(blockPos);

        DestructibleRegistry<ChunkPos, DestructibleRegistry<Integer, String>> blockCache = Registries.DBLOCK_CACHE;
        blockCache.computeIfAbsent(chunkPos, k -> new DestructibleRegistry<>()).put(chunkInt, editorSettings.getDblock().getId());

        try {
            Thread.sleep(1);
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(blockCache.computeIfPresent(chunkPos, (k, v) -> {
            if(v.containsKey(chunkInt)) return v;
            return null;
        }) == null) return;

        CraftEntity entity = EntityUtil.getHighlight(serverLevel);

        editorSettings.getBlockHighlight().computeIfAbsent(chunkPos, k -> new DestructibleRegistry<>()).computeIfAbsent(chunkInt, v -> entity.getEntityId());
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            Registries.DBLOCK_EDITOR.forEach((k, v) -> {
                ((CraftPlayer) v.getPlayer()).getHandle().connection.send(new ClientboundAddEntityPacket(entity.getHandle(), 0, blockPos));
                ((CraftPlayer) v.getPlayer()).getHandle().connection.send(new ClientboundSetEntityDataPacket(entity.getEntityId(), entity.getHandle().getEntityData().packAll()));
            });
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onBlockBreak(final BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if(!player.isOp() || player.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            EditorSettings editorSettings = Registries.DBLOCK_EDITOR.get(player.getUniqueId());
            ChunkPos chunkPos = new ChunkPos(block.getLocation());
            int chunkInt = CoordUtil.posToChunkInt(block.getLocation());

            Registries.DBLOCK_CACHE.computeIfPresent(chunkPos, (k, v) -> {
                v.computeIfPresent(chunkInt, (k2, v2) -> {
                    Bukkit.getScheduler().runTask(this.plugin, () -> {
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Destructible block <gold>" + v2 + "</gold> been removed from the cache!"));
                        player.getWorld().playSound(block.getLocation(), Sound.ENTITY_PLAYER_BURP, 0.40f, 1.0f);
                    });
                    return null;
                });
                if(v.isEmpty()) return null;
                else return v;
            });

            Registries.DBLOCK_EDITOR.forEach((k, v) -> v
                    .getBlockHighlight()
                    .computeIfPresent(chunkPos, (k2, v2) -> {
                        v2.computeIfPresent(chunkInt, (k3, v3) -> {
                            Bukkit.getScheduler().runTask(this.plugin, () -> ((CraftPlayer) editorSettings.getPlayer()).getHandle().connection.send(new ClientboundRemoveEntitiesPacket(v3)));
                            return null;
                        });
                        if(v2.isEmpty()) return null;
                        return v2;
                    }));
        });
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        initPlayer(player);
    }

    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        removePlayer(player);
    }

    public void initPlayer(final Player player) {
        DMiner dMiner = new DMiner(this.plugin, player);
        Registries.DMINER_DATA.put(player.getUniqueId(), dMiner);
        this.plugin.getPlayerPacketListener().addListener(player, dMiner);

        player.getAttribute(Attribute.BLOCK_BREAK_SPEED).setBaseValue(0.0);
    }

    public void removePlayer(final Player player) {
        Registries.DMINER_DATA.remove(player.getUniqueId());
        this.plugin.getPlayerPacketListener().removeListener(player);
    }
}