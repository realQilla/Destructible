package net.qilla.destructible.mining;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.*;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.block.DBlocks;
import net.qilla.destructible.player.BlockHighlight;
import net.qilla.destructible.player.DBlockEdit;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.CoordUtil;
import net.qilla.destructible.util.DBlockUtil;
import net.qilla.destructible.util.EntityUtil;
import net.qilla.destructible.util.FormatUtil;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PluginListener implements Listener {

    private final Destructible plugin;

    public PluginListener(Destructible plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        DPlayer dPlayer = Registries.DESTRUCTIBLE_PLAYERS.get(player.getUniqueId());

        if(!dPlayer.hasDBlockEdit()) return;
        DBlockEdit dBlockEdit = dPlayer.getDBlockEdit();

        Location location = event.getBlock().getLocation();
        BlockPos blockPos = CoordUtil.locToBlockPos(location);

        if(dBlockEdit.getDblock() == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {

            if(!dBlockEdit.isRecursive()) {
                dBlockEdit.loadBlock(blockPos, dBlockEdit.getDblock().getId());
                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    Registries.DESTRUCTIBLE_BLOCK_EDITORS.forEach(dPlayer2 -> {
                        dPlayer2.getDBlockEdit().getBlockHighlight().createHighlight(blockPos, dBlockEdit.getDblock().getId());
                    });
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Destructible block <gold><bold>" + dBlockEdit.getDblock().getId() + "</gold> has been <green><bold>LOADED</green>!"));
                    player.playSound(player, Sound.ENTITY_PLAYER_BURP, 0.40f, 2.0f);
                });
            } else {
                Set<BlockPos> recursiveBlocks = getRecursiveBlocks(blockPos, location.getWorld(), event.getBlock().getType(), dBlockEdit.getRecursionSize());
                int originalSize = recursiveBlocks.size();
                AtomicInteger currentSize = new AtomicInteger(recursiveBlocks.size());

                BukkitTask task = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
                    player.sendActionBar(MiniMessage.miniMessage().deserialize("<yellow>Recursive operation <gold>" + FormatUtil.numberPercentage(originalSize, currentSize.get()) + "</gold> completed"));
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 0.10f, 2f);
                }, 0, 40);

                for(BlockPos curBlockPos : recursiveBlocks) {
                    dBlockEdit.loadBlock(curBlockPos, dBlockEdit.getDblock().getId());
                    Registries.DESTRUCTIBLE_BLOCK_EDITORS.forEach(dPlayer2 -> {
                        dPlayer2.getDBlockEdit().getBlockHighlight().createHighlight(curBlockPos, dBlockEdit.getDblock().getId());
                    });
                    try {
                        Thread.sleep(1);
                    } catch(InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    currentSize.decrementAndGet();
                }
                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Destructible block <red><bold>RECURSIVE</red> operation completed, <gold>" + FormatUtil.numberChar(recursiveBlocks.size(), false) + "</gold> block(s) cached as <gold>" + dBlockEdit.getDblock().getId() + "</gold>!"));
                    player.sendActionBar(MiniMessage.miniMessage().deserialize("<yellow>Recursive operation <gold>" + FormatUtil.numberPercentage(originalSize, currentSize.get()) + "</gold> completed"));
                    player.playSound(player, Sound.ENTITY_PLAYER_BURP, 1.0f, 0.0f);
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

    @EventHandler(priority = EventPriority.NORMAL)
    private void onBlockBreak(final BlockBreakEvent event) {
        Player player = event.getPlayer();

        if(!player.isOp()) {
            event.setCancelled(true);
            return;
        }

        BlockPos blockPos = CoordUtil.locToBlockPos(event.getBlock().getLocation());
        DBlock dBlock = DBlockUtil.getDBlock(blockPos);

        if(dBlock.equals(DBlocks.DEFAULT)) return;

        DPlayer dPlayer = Registries.DESTRUCTIBLE_PLAYERS.get(player.getUniqueId());

        dPlayer.getDBlockEdit().unloadBlock(blockPos);
        Registries.DESTRUCTIBLE_BLOCK_EDITORS.forEach(dPlayer2 -> {
            dPlayer2.getDBlockEdit().getBlockHighlight().removeHighlight(blockPos);
        });


        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Destructible block <gold><bold>" + dBlock.getId() + "</gold> has been <red><bold>UNLOADED</red>!"));
        player.playSound(player, Sound.ENTITY_PLAYER_BURP, 0.40f, 1.0f);
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
        CraftPlayer serverPlayer = ((CraftPlayer) player);
        DPlayer dPlayer = new DPlayer((CraftServer) serverPlayer.getServer(), serverPlayer.getHandle());
        Registries.DESTRUCTIBLE_PLAYERS.put(player.getUniqueId(), dPlayer);
        this.plugin.getPlayerPacketListener().addListener(player, dPlayer);

        player.getAttribute(Attribute.BLOCK_BREAK_SPEED).setBaseValue(0.0);
    }

    public void removePlayer(final Player player) {
        Registries.DESTRUCTIBLE_PLAYERS.remove(player.getUniqueId());
        this.plugin.getPlayerPacketListener().removeListener(player);
    }
}