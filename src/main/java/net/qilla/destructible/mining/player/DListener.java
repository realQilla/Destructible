package net.qilla.destructible.mining.player;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.*;
import net.qilla.destructible.util.CoordUtil;
import net.qilla.destructible.util.EntityUtil;
import net.qilla.destructible.util.FormatUtil;
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
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DListener implements Listener {

    private final Destructible plugin;

    public DListener(Destructible plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onBlockPlace(final BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();
        BlockPos blockPos = CoordUtil.locToBlockPos(location);
        DBlockEditor DBlockEditor = Registries.DESTRUCTIBLE_BLOCK_EDITORS.get(player.getUniqueId());

        if(DBlockEditor == null || DBlockEditor.getDblock() == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            if(!DBlockEditor.isRecursive()) {

                this.saveDBlock(blockPos, player, DBlockEditor);
                player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Destructible block <gold>" + DBlockEditor.getDblock().getId() + "</gold> has been cached!"));
                player.playSound(player, Sound.ENTITY_PLAYER_BURP, 0.40f, 2.0f);
            } else {
                Set<BlockPos> recursiveBlocks = getRecursiveBlocks(blockPos, location.getWorld(), event.getBlock().getType(), DBlockEditor.getRecursionSize());
                final int originalSize = recursiveBlocks.size();
                final AtomicInteger currentSize = new AtomicInteger(recursiveBlocks.size());
                BukkitTask task = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
                    player.sendActionBar(MiniMessage.miniMessage().deserialize("<yellow>Recursive operation <gold>" + FormatUtil.numberPercentage(originalSize, currentSize.get()) + "</gold> completed"));
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 0.10f, 2f);
                }, 0, 40);
                DBlockEditor.setLockHighlight(true);

                for(BlockPos curBlockPos : recursiveBlocks) {
                    this.saveDBlock(curBlockPos, player, DBlockEditor);
                    currentSize.decrementAndGet();
                }
                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Destructible block <red><bold>RECURSIVE</red> operation completed, <gold>" + FormatUtil.numberChar(recursiveBlocks.size(), false) + "</gold> block(s) cached as <gold>" + DBlockEditor.getDblock().getId() + "</gold>!"));
                    player.sendActionBar(MiniMessage.miniMessage().deserialize("<yellow>Recursive operation <gold>" + FormatUtil.numberPercentage(originalSize, currentSize.get()) + "</gold> completed"));
                    player.playSound(player, Sound.ENTITY_PLAYER_BURP, 1.0f, 0.0f);
                    DBlockEditor.setLockHighlight(false);
                });
                task.cancel();
            }
        });
    }

    private Set<BlockPos> getRecursiveBlocks(BlockPos origin, World world, Material originMaterial, int recursionSize) {
        Set<BlockPos> posList = new HashSet<>();
        Queue<BlockPos> recursivePos = new LinkedList<>();
        recursivePos.add(origin);
        posList.add(origin);

        while(!recursivePos.isEmpty() && posList.size() < recursionSize) {
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
                if(!block.getType().equals(originMaterial)) continue;
                posList.add(newPos);
                recursivePos.add(newPos);
            }
        }
        return posList.stream()
                .sorted(Comparator.comparingInt(pos ->
                        Math.max(Math.max(Math.abs(pos.getX() - origin.getX()),
                                        Math.abs(pos.getY() - origin.getY())),
                                Math.abs(pos.getZ() - origin.getZ()))))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void saveDBlock(BlockPos blockPos, Player player, DBlockEditor DBlockEditor) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ServerLevel serverLevel = serverPlayer.serverLevel();
        ChunkPos chunkPos = new ChunkPos(blockPos);
        int chunkInt = CoordUtil.posToChunkLocalPos(blockPos);

        DestructibleRegistry<ChunkPos, DestructibleRegistry<Integer, String>> blockCache = Registries.DESTRUCTIBLE_BLOCKS_CACHE;
        blockCache.computeIfAbsent(chunkPos, k -> new DestructibleRegistry<>()).put(chunkInt, DBlockEditor.getDblock().getId());

        try {
            Thread.sleep(1);
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.createHighlight(serverLevel, blockPos, chunkPos, chunkInt);
    }

    private void createHighlight(ServerLevel serverLevel, BlockPos blockPos, ChunkPos chunkPos, int chunkInt) {
        CraftEntity entity = EntityUtil.getHighlight(serverLevel);
        Registries.DESTRUCTIBLE_BLOCK_EDITORS.forEach((k, v) -> {
            if(!v.isHighlight()) return;

            Bukkit.getScheduler().runTask(this.plugin, () -> {
                v.getBlockHighlight().computeIfAbsent(chunkPos, k2 -> new DestructibleRegistry<>()).computeIfAbsent(chunkInt, v2 -> entity.getEntityId());
                ((CraftPlayer) v.getPlayer()).getHandle().connection.send(new ClientboundAddEntityPacket(entity.getHandle(), 0, blockPos));
                ((CraftPlayer) v.getPlayer()).getHandle().connection.send(new ClientboundSetEntityDataPacket(entity.getEntityId(), entity.getHandle().getEntityData().packAll()));
            });
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onBlockBreak(final BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();

        if(!player.isOp() || player.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            DBlockEditor DBlockEditor = Registries.DESTRUCTIBLE_BLOCK_EDITORS.get(player.getUniqueId());
            ChunkPos chunkPos = new ChunkPos(location);
            int chunkInt = CoordUtil.posToChunkLocalPos(location);

            Registries.DESTRUCTIBLE_BLOCKS_CACHE.computeIfPresent(chunkPos, (k, v) -> {
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

            Registries.DESTRUCTIBLE_BLOCK_EDITORS.forEach((k, v) ->
                    v.getBlockHighlight().computeIfPresent(chunkPos, (k2, v2) -> {
                        v2.computeIfPresent(chunkInt, (k3, v3) -> {
                            Bukkit.getScheduler().runTask(this.plugin, () -> ((CraftPlayer) DBlockEditor.getPlayer()).getHandle().connection.send(new ClientboundRemoveEntitiesPacket(v3)));
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
        DMiner dMiner = new DMiner(this.plugin, player, new Equipment(player));
        Registries.DESTRUCTIBLE_MINERS_DATA.put(player.getUniqueId(), dMiner);
        this.plugin.getPlayerPacketListener().addListener(player, dMiner);

        player.getAttribute(Attribute.BLOCK_BREAK_SPEED).setBaseValue(0.0);
    }

    public void removePlayer(final Player player) {
        Registries.DESTRUCTIBLE_MINERS_DATA.remove(player.getUniqueId());
        this.plugin.getPlayerPacketListener().removeListener(player);
    }
}