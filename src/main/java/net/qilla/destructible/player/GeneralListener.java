package net.qilla.destructible.player;

import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.core.BlockPos;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.*;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.*;
import net.qilla.destructible.util.*;
import net.qilla.qlibrary.util.tools.CoordUtil;
import net.qilla.qlibrary.util.tools.NumberUtil;
import net.qilla.qlibrary.util.tools.StringUtil;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class GeneralListener implements Listener {

    private static final Set<DPlayer> BLOCK_EDITOR_SET = DRegistry.BLOCK_EDITORS;
    private static final Map<UUID, DPlayer> DPLAYER_MAP = DRegistry.DPLAYERS;
    private static final Map<Long, ConcurrentHashMap<Integer, String>> LOADED_BLOCK_MAP = DRegistry.LOADED_BLOCKS;
    private static final Map<String, DItem> DITEM_MAP = DRegistry.ITEMS;
    private final Destructible plugin;
    private final BlockingQueue<Runnable> taskQueue = new ArrayBlockingQueue<>(16);
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    public GeneralListener(@NotNull Destructible plugin) {
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
        UUID uuid = event.getPlayer().getUniqueId();
        DPlayer dPlayer = DPLAYER_MAP.get(uuid);

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
            Set<BlockPos> recursiveBlocks = this.findConnectedBlocks(CoordUtil.getBlockPos(block), block.getWorld(), block.getType(), edit.getRecursionSize());
            if(!this.scheduleTask(() -> {
                this.registerBlock(dPlayer, dBlock, recursiveBlocks);
            })) {
                dPlayer.sendMessage(MiniMessage.miniMessage().deserialize("<red>There are too many queued operations! Please wait for one to complete before trying again!"));
                dPlayer.playSound(Sounds.GENERAL_ERROR, true);
            }
        }
    }

    private boolean registerBlock(DBlock dBlock, Block block) {
        BlockPos blockPos = CoordUtil.getBlockPos(block);

        if(!RegistryUtil.loadBlock(blockPos, dBlock.getId())) return false;
        if(block.getType() != dBlock.getMaterial()) block.setType(dBlock.getMaterial(), false);
        BLOCK_EDITOR_SET.forEach(curPlayer -> {
            curPlayer.getDBlockEdit().getBlockHighlight().createHighlight(blockPos, dBlock.getId());
        });
        return true;
    }

    private void registerBlock(DPlayer dPlayer, DBlock dBlock, Set<BlockPos> blockPosSet) {
        World world = dPlayer.getCraftPlayer().getWorld();
        AtomicInteger remainingBlocks = new AtomicInteger(blockPosSet.size());
        List<BlockPos> blocks = new ArrayList<>(blockPosSet);

        BukkitTask progressTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            String operationString = "<yellow>Operation is <gold>" + NumberUtil.numberPercentage(blockPosSet.size(), remainingBlocks.get()) + "</gold> completed" +
                    (taskQueue.isEmpty() ? "" : ", <gold>" + taskQueue.size() + "</gold> " + StringUtil.pluralize("operation", taskQueue.size()) + " remaining");
            dPlayer.sendActionBar(operationString);
            dPlayer.playSound(Sounds.LARGE_OPERATION_UPDATE, true);
        }, 0, 40);
        for(BlockPos blockPos : blocks) {
            Block block = CoordUtil.getBlock(blockPos, world);
            RegistryUtil.loadBlock(blockPos, dBlock.getId());

            Bukkit.getScheduler().runTask(plugin, () -> {
                BLOCK_EDITOR_SET.forEach(curPlayer -> {
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
            dPlayer.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Operation completed, <gold>" + NumberUtil.numberChar(blockPosSet.size(), false) + "</gold> block(s) cached as <gold>" + dBlock.getId() + "</gold>!"));
            dPlayer.sendActionBar(MiniMessage.miniMessage().deserialize("<yellow>Operation <gold>" + NumberUtil.numberPercentage(blockPosSet.size(), remainingBlocks.get()) + "</gold> completed"));
            dPlayer.playSound(Sounds.LARGE_OPERATION_COMPLETE, true);
        });

    }


    private Set<BlockPos> findConnectedBlocks(BlockPos origin, World world, Material material, int recursionSize) {
        Set<BlockPos> blockPosSet = new LinkedHashSet<>();
        Queue<BlockPos> blockQueue = new LinkedList<>();

        blockQueue.add(origin);

        while(!blockQueue.isEmpty() && blockPosSet.size() < recursionSize) {
            BlockPos currentPos = blockQueue.poll();

            for(BlockPos newPos : getNeighboringPositions(currentPos)) {
                if(blockPosSet.size() >= recursionSize || blockPosSet.contains(newPos) || newPos.equals(origin))
                    continue;

                if(CoordUtil.getBlock(newPos, world).getType().equals(material)) {
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
        UUID uuid = event.getPlayer().getUniqueId();
        DPlayer dPlayer = DPLAYER_MAP.get(uuid);
        Block block = event.getBlock();

        if(!dPlayer.getCraftPlayer().isOp()) return;

        BlockPos blockPos = CoordUtil.getBlockPos(block);
        Optional<DBlock> optional = DUtil.getDBlock(blockPos);

        if(optional.isEmpty()) return;
        DBlock dBlock = optional.get();

        if(RegistryUtil.unloadBlock(blockPos)) {
            BLOCK_EDITOR_SET.forEach(dPlayer2 -> {
                dPlayer2.getDBlockEdit().getBlockHighlight().removeHighlight(blockPos);
            });
            dPlayer.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Block: <gold><bold>" + dBlock.getId() + "</gold> has been <red><bold>UNLOADED</red>!"));
            dPlayer.playSound(Sounds.TINY_OPERATION_COMPLETE, true);
        } else {
            dPlayer.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Block: <gold><bold>" + dBlock.getId() + "</gold> could not be <red><bold>UNLOADED</red>!"));
            dPlayer.playSound(Sounds.GENERAL_ERROR, true);
        }
    }

    @EventHandler
    private void onChunkLoad(PlayerChunkLoadEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        DPlayer dPlayer = DPLAYER_MAP.get(uuid);
        if(!BLOCK_EDITOR_SET.contains(dPlayer)) return;

        BlockHighlight blockHighlight = dPlayer.getDBlockEdit().getBlockHighlight();
        Set<Long> chunkKeys = CoordUtil.getYChunkKeys(event.getChunk().getX(), event.getChunk().getZ());

        for(long chunkKey : chunkKeys) {
            if(!LOADED_BLOCK_MAP.containsKey(chunkKey)) continue;
            blockHighlight.createHighlights(chunkKey);
        }
    }

    @EventHandler
    private void onChunkUnload(PlayerChunkUnloadEvent event) {
        DPlayer dPlayer = DPLAYER_MAP.get(event.getPlayer().getUniqueId());
        if(!BLOCK_EDITOR_SET.contains(dPlayer)) return;

        BlockHighlight blockHighlight = dPlayer.getDBlockEdit().getBlockHighlight();
        Set<Long> chunkKeys = CoordUtil.getYChunkKeys(event.getChunk().getX(), event.getChunk().getZ());

        for(long chunkKey : chunkKeys) {
            if(!LOADED_BLOCK_MAP.containsKey(chunkKey)) continue;
            blockHighlight.removeHighlights(chunkKey);
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        this.initPlayer(player);
        this.updatePlayer(player);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        removePlayer(player);
    }

    public void initPlayer(@NotNull Player player) {
        RegistryUtil.registerPlayer(player);
        DPlayer dPlayer = DPLAYER_MAP.get(player.getUniqueId());
        plugin.getPlayerPacketListener().addListener(dPlayer);

        player.getAttribute(Attribute.BLOCK_BREAK_SPEED).setBaseValue(0.0);
    }

    public void removePlayer(@NotNull Player player) {
        DPlayer dPlayer = DPLAYER_MAP.get(player.getUniqueId());
        plugin.getPlayerPacketListener().removeListener(dPlayer);
        //RegistryUtil.unregisterPlayer(player);
    }

    @EventHandler
    private void onChatEvent(AsyncChatEvent event) {
        DPlayer dPlayer = DPLAYER_MAP.get(event.getPlayer().getUniqueId());

        if(dPlayer.getMenuHolder().fulfillInput(ComponentUtil.cleanComponent(event.message()))) {
            event.setCancelled(true);
        }
    }

    private void updatePlayer(@NotNull Player player) {
        for(int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack itemStack = player.getInventory().getItem(slot);

            if(itemStack == null) continue;
            if(validateItemVersion(itemStack)) continue;

            player.getInventory().setItem(slot, ItemStackFactory.ofUpdated(itemStack));
        }
    }

    @EventHandler
    private void onInventoryMoveEvent(InventoryMoveItemEvent event) {
        ItemStack itemStack = event.getItem();
        ItemData itemData = itemStack.getPersistentDataContainer().get(DataKey.DESTRUCTIBLE_ITEM, ItemDataType.ITEM);

        if(itemData == null) return;
        if(validateItemVersion(itemStack)) return;

        event.setItem(ItemStackFactory.ofUpdated(itemStack));
    }

    @EventHandler
    private void onInventoryClickEvent(InventoryClickEvent event) {
        ItemStack itemStack = event.getCurrentItem();
        if(itemStack == null) return;
        if(validateItemVersion(itemStack)) return;

        event.setCurrentItem(ItemStackFactory.ofUpdated(itemStack));
    }

    @EventHandler
    private void onItemDropEvent(PlayerDropItemEvent event) {
        ItemStack itemStack = event.getItemDrop().getItemStack();
        ItemData itemData = itemStack.getPersistentDataContainer().get(DataKey.DESTRUCTIBLE_ITEM, ItemDataType.ITEM);

        if(itemData == null) return;
        if(validateItemVersion(itemStack)) return;

        event.getItemDrop().setItemStack(ItemStackFactory.ofUpdated(itemStack));
    }

    //@EventHandler
    private void itemPickupItemEvent(PlayerAttemptPickupItemEvent event) {
        DPlayer dPlayer = DPLAYER_MAP.get(event.getPlayer().getUniqueId());

        event.setCancelled(true);
        event.getItem().remove();
        ItemStack itemStack = event.getItem().getItemStack();

        dPlayer.give(itemStack);
    }

    private boolean validateItemVersion(@NotNull ItemStack itemStack) {
        ItemData itemData = itemStack.getPersistentDataContainer().get(DataKey.DESTRUCTIBLE_ITEM, ItemDataType.ITEM);

        if(itemData == null) return true;

        DItem dItem = DUtil.getDItem(itemData.getItemID());

        long itemVersion = itemData.getVersion();
        long registryVersion = DITEM_MAP.getOrDefault(dItem.getId(), DItems.MISSING_ITEM).getVersion();

        if(itemVersion == registryVersion) return true;

        plugin.getLogger().log(Level.INFO, "Updating item " + dItem.getId() + " for a player as there is a version mismatch. (" + itemVersion + " != " + registryVersion + ")");
        return false;
    }
}