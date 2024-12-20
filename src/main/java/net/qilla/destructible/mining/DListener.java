package net.qilla.destructible.mining;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.core.BlockPos;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.ChunkCoord;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.data.Registry;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.util.CoordUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.Block;
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

    @EventHandler(priority = EventPriority.LOWEST)
    private void onBlockPlace(final BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if(!Registries.DBLOCK_EDITOR.containsKey(player.getUniqueId())) return;
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            DBlock dBlock = Registries.DBLOCK_EDITOR.get(player.getUniqueId());
            ChunkCoord chunkCoord = new ChunkCoord(block.getLocation());
            int coords = CoordUtil.getPosInChunk(block.getLocation());

            Registries.CACHED_DBLOCKS.computeIfAbsent(chunkCoord, k -> new Registry<>()).put(coords, dBlock);
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>'" + dBlock.getId() + "' successfully registered!"));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 0.40f, 2.0f);
            });
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onBlockBreak(final BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if(!Registries.DBLOCK_EDITOR.containsKey(player.getUniqueId())) return;
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            ChunkCoord chunkCoord = new ChunkCoord(block.getLocation());
            int coords = CoordUtil.getPosInChunk(block.getLocation());

            var registry = Registries.CACHED_DBLOCKS.computeIfPresent(chunkCoord, (k, v) -> v);
            if(registry == null) return;
            registry.computeIfPresent(coords, (k, v) -> {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>DBlock " + v.getId() + " has been unregistered!"));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 0.40f, 1.0f);
                return null;
            });
        });
    }
}