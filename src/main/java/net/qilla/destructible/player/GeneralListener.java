package net.qilla.destructible.player;

import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.*;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.*;
import net.qilla.destructible.util.*;
import net.qilla.qlibrary.data.PlayerData;
import net.qilla.qlibrary.util.tools.CoordUtil;
import net.qilla.qlibrary.util.tools.NumberUtil;
import net.qilla.qlibrary.util.tools.StringUtil;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
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

    private static final Set<UUID> BLOCK_EDITOR_SET = DRegistry.BLOCK_EDITORS;
    private static final Map<UUID, DPlayerData> PLAYER_DATA = DRegistry.PLAYER_DATA;
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
        DPlayer player = new DPlayer((CraftPlayer) event.getPlayer());

        if(!player.isOp()) return;

        DPlayerData playerData = PLAYER_DATA.get(player.getUniqueId());

        if(!playerData.isBlockEditing()) return;

        BlockEdit blockEdit = playerData.getBlockEdit();
        DBlock dBlock = blockEdit.getDblock();

        if(dBlock == null) return;

        Block block = event.getBlock();

        if(blockEdit.getRecursionSize() <= 0) {
            if(registerBlock(playerData, block)) {
                player.sendMessage("<yellow>Block: <gold><bold>" + dBlock.getId() + "</gold> has been <green><bold>LOADED</green>!");
                player.playSound(DSounds.TINY_OPERATION_COMPLETE, true);
            } else {
                player.sendMessage("<yellow>Block: <gold><bold>" + dBlock.getId() + "</gold> could not be <red><bold>LOADED</red>: block already exists in this position. Destroy and try again!");
                player.playSound(DSounds.GENERAL_ERROR, true);
            }
        } else {
            event.setCancelled(true);
            Set<BlockPos> recursiveBlocks = this.findConnectedBlocks(CoordUtil.getBlockPos(block), block.getWorld(), block.getType(), blockEdit.getRecursionSize());
            if(!this.scheduleTask(() -> {
                this.registerBlock(playerData, recursiveBlocks);
            })) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>There are too many queued operations! Please wait for one to complete before trying again!"));
                player.playSound(DSounds.GENERAL_ERROR, true);
            }
        }
    }

    private boolean registerBlock(@NotNull DPlayerData playerData, @NotNull Block block) {
        DBlock dBlock = playerData.getBlockEdit().getDblock();
        BlockPos blockPos = CoordUtil.getBlockPos(block);

        if(!RegistryUtil.loadBlock(blockPos, dBlock.getId())) return false;
        if(block.getType() != dBlock.getMaterial()) block.setType(dBlock.getMaterial(), false);

        BLOCK_EDITOR_SET.forEach(uuid -> {
            DPlayerData curPlayerData = PLAYER_DATA.get(uuid);
            curPlayerData.getBlockEdit().getBlockHighlight().createHighlight(blockPos, dBlock.getId());
        });
        return true;
    }

    private void registerBlock(@NotNull DPlayerData playerData, @NotNull Set<BlockPos> blockPosSet) {
        DPlayer player = playerData.getPlayer();
        World world = player.getWorld();
        DBlock dBlock = playerData.getBlockEdit().getDblock();

        AtomicInteger remainingBlocks = new AtomicInteger();
        List<BlockPos> blocks = new ArrayList<>(blockPosSet);

        BukkitTask progressTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            String operationString = "<yellow>Operation is <gold>" + NumberUtil.numberPercentage(blockPosSet.size(), remainingBlocks.get()) + "</gold> completed" +
                    (taskQueue.isEmpty() ? "" : ", <gold>" + taskQueue.size() + "</gold> " + StringUtil.pluralize("operation", taskQueue.size()) + " remaining");
            player.sendActionBar(operationString);
            player.playSound(DSounds.LARGE_OPERATION_UPDATE, true);
        }, 0, 40);
        for(BlockPos blockPos : blocks) {
            Block block = CoordUtil.getBlock(blockPos, world);
            RegistryUtil.loadBlock(blockPos, dBlock.getId());

            Bukkit.getScheduler().runTask(plugin, () -> {
                BLOCK_EDITOR_SET.forEach(curPlayer -> {
                    playerData.getBlockEdit().getBlockHighlight().createHighlight(blockPos, dBlock.getId());
                });

                if(block.getType() != dBlock.getMaterial()) block.setType(dBlock.getMaterial(), false);
            });
            if(remainingBlocks.incrementAndGet() % 1000 == 0) {
                try {
                    Thread.sleep(100);
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        progressTask.cancel();

        Bukkit.getScheduler().runTask(plugin, () -> {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Operation completed, <gold>" + NumberUtil.numberChar(blockPosSet.size(), false) + "</gold> block(s) cached as <gold>" + dBlock.getId() + "</gold>!"));
            player.sendActionBar(MiniMessage.miniMessage().deserialize("<yellow>Operation <gold>" + NumberUtil.numberPercentage(blockPosSet.size(), remainingBlocks.get()) + "</gold> completed"));
            player.playSound(DSounds.LARGE_OPERATION_COMPLETE, true);
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
        DPlayer player = new DPlayer((CraftPlayer) event.getPlayer());

        if(!player.isOp()) return;

        DPlayerData playerData = PLAYER_DATA.get(player.getUniqueId());;
        Block block = event.getBlock();
        BlockPos blockPos = CoordUtil.getBlockPos(block);
        Optional<DBlock> optional = DUtil.getDBlock(blockPos);

        if(optional.isEmpty()) return;
        DBlock dBlock = optional.get();

        if(RegistryUtil.unloadBlock(blockPos)) {
            BLOCK_EDITOR_SET.forEach(dPlayer2 -> {
                playerData.getBlockEdit().getBlockHighlight().removeHighlight(blockPos);
            });
            player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Block: <gold><bold>" + dBlock.getId() + "</gold> has been <red><bold>UNLOADED</red>!"));
            player.playSound(DSounds.TINY_OPERATION_COMPLETE, true);
        } else {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Block: <gold><bold>" + dBlock.getId() + "</gold> could not be <red><bold>UNLOADED</red>!"));
            player.playSound(DSounds.GENERAL_ERROR, true);
        }
    }

    @EventHandler
    private void onChunkLoad(final PlayerChunkLoadEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if(!BLOCK_EDITOR_SET.contains(uuid)) return;
        DPlayerData playerData = PLAYER_DATA.get(uuid);

        BlockHighlight blockHighlight = playerData.getBlockEdit().getBlockHighlight();
        Set<Long> chunkKeys = CoordUtil.getYChunkKeys(event.getChunk().getX(), event.getChunk().getZ());

        for(long chunkKey : chunkKeys) {
            if(!LOADED_BLOCK_MAP.containsKey(chunkKey)) continue;
            blockHighlight.createHighlights(chunkKey);
        }
    }

    @EventHandler
    private void onChunkUnload(PlayerChunkUnloadEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if(!BLOCK_EDITOR_SET.contains(uuid)) return;
        DPlayerData playerData = PLAYER_DATA.get(uuid);

        BlockHighlight blockHighlight = playerData.getBlockEdit().getBlockHighlight();
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
        CraftPlayer craftPlayer = (CraftPlayer) player;

        if(PLAYER_DATA.containsKey(craftPlayer.getUniqueId())) PLAYER_DATA.put(craftPlayer.getUniqueId(), new DPlayerData(new DPlayer(craftPlayer), plugin));
        else PLAYER_DATA.put(craftPlayer.getUniqueId(), new DPlayerData(new DPlayer(craftPlayer), plugin));

        PlayerPacketListener.getInstance().addListener(PLAYER_DATA.get(craftPlayer.getUniqueId()));
        player.getAttribute(Attribute.BLOCK_BREAK_SPEED).setBaseValue(0.0);
    }

    public void removePlayer(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        if(!PLAYER_DATA.containsKey(uuid)) return;
        DPlayerData playerData = PLAYER_DATA.get(uuid);

        PlayerPacketListener.getInstance().removeListener(playerData.getPlayer());
    }

    @EventHandler
    private void onChatEvent(AsyncChatEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if(!PLAYER_DATA.containsKey(uuid)) return;
        DPlayerData playerData = PLAYER_DATA.get(uuid);

        if(playerData.fulfillInput(ComponentUtil.cleanComponent(event.message()))) {
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

    @EventHandler
    private void itemPickupItemEvent(PlayerAttemptPickupItemEvent event) {
        CraftItem craftItem = (CraftItem) event.getItem();
        ItemStack itemStack = craftItem.getItemStack();

        if(!itemStack.getPersistentDataContainer().has(DataKey.DESTRUCTIBLE_ITEM, ItemDataType.ITEM)) return;
        craftItem.remove();
        event.setCancelled(true);

        ItemEntity itemEntity = new ItemEntity(
                craftItem.getHandle().level(),
                craftItem.getX(),
                craftItem.getY(),
                craftItem.getZ(),
                craftItem.getHandle().getItem()
        );

        DPlayer player = new DPlayer((CraftPlayer) event.getPlayer());

        player.broadcastPacket(new ClientboundAddEntityPacket(
                itemEntity.getId(), itemEntity.getUUID(),
                itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                0f, 0f,
                itemEntity.getType(),
                0,
                new Vec3(0, 0, 0),
                0
        ));
        player.broadcastPacket(new ClientboundSetEntityDataPacket(itemEntity.getId(), craftItem.getHandle().getEntityData().packAll()));
        player.broadcastPacket(new ClientboundTakeItemEntityPacket(itemEntity.getId(), player.getEntityId(), itemEntity.getItem().getCount()));

        player.give(itemStack);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if(!itemEntity.isAlive()) itemEntity.remove(Entity.RemovalReason.DISCARDED);
        }, 40);
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