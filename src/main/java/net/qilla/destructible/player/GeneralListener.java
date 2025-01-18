package net.qilla.destructible.player;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.core.BlockPos;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.*;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.util.CoordUtil;
import net.qilla.destructible.util.DestructibleUtil;
import net.qilla.destructible.util.FormatUtil;
import net.qilla.destructible.util.RegistryUtil;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class GeneralListener implements Listener {

    private final Destructible plugin;
    private final BlockingQueue<Runnable> taskQueue = new ArrayBlockingQueue<>(16);
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    public GeneralListener(Destructible plugin) {
        this.plugin = plugin;
    }

    private boolean scheduleTask(Runnable task) {
        if(!taskQueue.offer(task)) return false;
        processingQueue();
        return true;
    }

    private void processingQueue() {
        if(isProcessing.compareAndSet(false, true)) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                while(!taskQueue.isEmpty()) {
                    try {
                        Runnable nextAsk = taskQueue.poll();
                        if(nextAsk != null) nextAsk.run();
                    } catch(Exception e) {
                        plugin.getLogger().log(Level.SEVERE, "Error while processing task queue", e);
                    }
                }
                isProcessing.set(false);
            });
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onBlockPlace(BlockPlaceEvent event) {
        DPlayer dPlayer = DRegistry.DESTRUCTIBLE_PLAYERS.get(event.getPlayer().getUniqueId());

        if(!dPlayer.hasDBlockEdit()) return;
        DBlockEdit edit = dPlayer.getDBlockEdit();
        DBlock dBlock = edit.getDblock();

        if(dBlock == null) return;

        Block block = event.getBlock();
        if(edit.getRecursionSize() <= 0) {
            if(registerBlock(dBlock, block)) {
                dPlayer.sendMessage("<yellow>Block: <gold><bold>" + dBlock.getId() + "</gold> has been <green><bold>LOADED</green>!");
                dPlayer.playSound(Sounds.TINY_OPERATION_COMPLETE, true);
            } else {
                dPlayer.sendMessage("<yellow>Block: <gold><bold>" + dBlock.getId() + "</gold> could not be <red><bold>LOADED</red>: block already exists in this position. Destroy and try again!");
                dPlayer.playSound(Sounds.GENERAL_ERROR, true);
            }
        } else {
            event.setCancelled(true);
            Set<BlockPos> recursiveBlocks = this.findConnectedBlocks(CoordUtil.toBlockPos(block), block.getWorld(), block.getType(), edit.getRecursionSize());
            if(!this.scheduleTask(() -> {
                this.registerBlock(dPlayer, dBlock, recursiveBlocks);
            })) {
                dPlayer.sendMessage(MiniMessage.miniMessage().deserialize("<red>There are too many queued operations! Please wait for one to complete before trying again!"));
                dPlayer.playSound(Sounds.GENERAL_ERROR, true);
            }
        }
    }

    private boolean registerBlock(DBlock dBlock, Block block) {
        BlockPos blockPos = CoordUtil.toBlockPos(block);

        if(!RegistryUtil.registerBlock(blockPos, dBlock)) return false;
        if(block.getType() != dBlock.getMaterial()) block.setType(dBlock.getMaterial(), false);
        DRegistry.DESTRUCTIBLE_BLOCK_EDITORS.forEach(curPlayer -> {
            curPlayer.getDBlockEdit().getBlockHighlight().createHighlight(blockPos, dBlock.getId());
        });
        return true;
    }

    private void registerBlock(DPlayer dPlayer, DBlock dBlock, Set<BlockPos> blockPosSet) {
        World world = dPlayer.getCraftPlayer().getWorld();
        AtomicInteger remainingBlocks = new AtomicInteger(blockPosSet.size());
        List<BlockPos> blocks = new ArrayList<>(blockPosSet);

        BukkitTask progressTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            String operationString = "<yellow>Operation is <gold>" + FormatUtil.numberPercentage(blockPosSet.size(), remainingBlocks.get()) + "</gold> completed" +
                    (taskQueue.isEmpty() ? "" : ", <gold>" + taskQueue.size() + "</gold> " + FormatUtil.pluralizer("operation", taskQueue.size()) + " remaining");
            dPlayer.sendActionBar(operationString);
            dPlayer.playSound(Sounds.LARGE_OPERATION_UPDATE, true);
        }, 0, 40);
        for(BlockPos blockPos : blocks) {
            Block block = CoordUtil.toBlock(blockPos, world);
            RegistryUtil.registerBlock(blockPos, dBlock);

            Bukkit.getScheduler().runTask(plugin, () -> {
                DRegistry.DESTRUCTIBLE_BLOCK_EDITORS.forEach(curPlayer -> {
                    curPlayer.getDBlockEdit().getBlockHighlight().createHighlight(blockPos, dBlock.getId());
                });

                if(block.getType() != dBlock.getMaterial()) block.setType(dBlock.getMaterial(), false);
            });
            if(remainingBlocks.decrementAndGet() % 1000 == 0) {
                try {
                    Thread.sleep(100);
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        progressTask.cancel();

        Bukkit.getScheduler().runTask(plugin, () -> {
            dPlayer.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Operation completed, <gold>" + FormatUtil.numberChar(blockPosSet.size(), false) + "</gold> block(s) cached as <gold>" + dBlock.getId() + "</gold>!"));
            dPlayer.sendActionBar(MiniMessage.miniMessage().deserialize("<yellow>Operation <gold>" + FormatUtil.numberPercentage(blockPosSet.size(), remainingBlocks.get()) + "</gold> completed"));
            dPlayer.playSound(Sounds.LARGE_OPERATION_COMPLETE, true);
        });

    }


    private Set<BlockPos> findConnectedBlocks(BlockPos origin, World world, Material material, int recursionSize) {
        Set<BlockPos> blockPosSet = new LinkedHashSet<>();
        Queue<BlockPos> blockQueue = new LinkedList<>();

        blockQueue.add(origin);

        while (!blockQueue.isEmpty() && blockPosSet.size() < recursionSize) {
            BlockPos currentPos = blockQueue.poll();

            for (BlockPos newPos : getNeighboringPositions(currentPos)) {
                if (blockPosSet.size() >= recursionSize || blockPosSet.contains(newPos) || newPos.equals(origin)) continue;

                if (CoordUtil.toBlock(newPos, world).getType().equals(material)) {
                    blockPosSet.add(newPos);
                    blockQueue.add(newPos);
                }
            }
        }
        return blockPosSet;
    }

    private List<BlockPos> getNeighboringPositions(BlockPos pos) {
        return List.of(
                pos.offset(1, 0, 0),
                pos.offset(-1, 0, 0),
                pos.offset(0, 1, 0),
                pos.offset(0, -1, 0),
                pos.offset(0, 0, 1),
                pos.offset(0, 0, -1)
        );
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onBlockBreak(final BlockBreakEvent event) {
        DPlayer dPlayer = DRegistry.DESTRUCTIBLE_PLAYERS.get(event.getPlayer().getUniqueId());
        Block block = event.getBlock();

        if(!dPlayer.getCraftPlayer().isOp()) return;

        BlockPos blockPos = CoordUtil.toBlockPos(block);
        Optional<DBlock> optional = DestructibleUtil.getDBlock(blockPos);

        if(optional.isEmpty()) return;
        DBlock dBlock = optional.get();

        if(RegistryUtil.unregisterBlock(blockPos)) {
            DRegistry.DESTRUCTIBLE_BLOCK_EDITORS.forEach(dPlayer2 -> {
                dPlayer2.getDBlockEdit().getBlockHighlight().removeBlockHighlight(blockPos);
            });
            dPlayer.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Block: <gold><bold>" + dBlock.getId() + "</gold> has been <red><bold>UNLOADED</red>!"));
            dPlayer.playSound(Sounds.TINY_OPERATION_COMPLETE, true);
        } else {
            dPlayer.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Block: <gold><bold>" + dBlock.getId() + "</gold> could not be <red><bold>UNLOADED</red>!"));
            dPlayer.playSound(Sounds.GENERAL_ERROR, true);
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        initPlayer(player);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        removePlayer(player);
    }

    public void initPlayer(Player player) {
        RegistryUtil.registerPlayer(player);
        plugin.getPlayerPacketListener().addListener(DRegistry.DESTRUCTIBLE_PLAYERS.get(player.getUniqueId()));

        player.getAttribute(Attribute.BLOCK_BREAK_SPEED).setBaseValue(0.0);
    }

    public void removePlayer(Player player) {
        //RegistryUtil.unregisterPlayer(player);
        plugin.getPlayerPacketListener().removeListener(DRegistry.DESTRUCTIBLE_PLAYERS.get(player.getUniqueId()));
    }

    @EventHandler
    private void onChatEvent(AsyncChatEvent event) {
        DPlayer dPlayer = DRegistry.DESTRUCTIBLE_PLAYERS.get(event.getPlayer().getUniqueId());
        if(dPlayer.getMenuData().fulfillInput(FormatUtil.cleanComponent(event.message()))) {
            event.setCancelled(true);
        }
    }
}