package net.qilla.destructible;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.command.destructible.DestructibleCom;
import net.qilla.destructible.files.CustomBlocksFile;
import net.qilla.destructible.files.CustomItemsFile;
import net.qilla.destructible.files.CustomToolsFile;
import net.qilla.destructible.files.LoadedCachedCustomBlocksFile;
import net.qilla.destructible.mining.MiningPacketListener;
import net.qilla.destructible.mining.block.DBlocks;
import net.qilla.destructible.mining.item.DItems;
import net.qilla.destructible.mining.item.DTools;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public final class Destructible extends JavaPlugin {

    private LifecycleEventManager<Plugin> lifecycleMan;
    private MiningPacketListener packetListener;
    private CustomItemsFile customItemsFile;
    private CustomToolsFile customToolsFile;
    private CustomBlocksFile customBlocksFile;
    private LoadedCachedCustomBlocksFile loadedCachedCustomBlocksFile;

    static {
        new DItems();
        new DTools();
        new DBlocks();
    }

    @Override
    public void onEnable() {
        this.lifecycleMan = this.getLifecycleManager();

        this.packetListener = new MiningPacketListener(this);
        this.customItemsFile = new CustomItemsFile();
        this.customToolsFile = new CustomToolsFile();
        this.customBlocksFile = new CustomBlocksFile();
        this.loadedCachedCustomBlocksFile = new LoadedCachedCustomBlocksFile();

        this.customItemsFile.load();
        this.customBlocksFile.load();
        this.loadedCachedCustomBlocksFile.load();

        initListener();
        initCommand();
    }

    private void initListener() {
        getServer().getPluginManager().registerEvents(new PluginListener(this), this);
    }

    private void initCommand() {
        this.lifecycleMan.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();
            new DestructibleCom(this, commands).register();
        });
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(player -> player.kick(MiniMessage.miniMessage().deserialize("<red>Server Reloaded.")));
    }

    public CustomItemsFile getCustomItemsFile() {
        return this.customItemsFile;
    }

    public CustomToolsFile getCustomToolsFile() {
        return this.customToolsFile;
    }

    public CustomBlocksFile getCustomBlocksFile() {
        return this.customBlocksFile;
    }


    public LoadedCachedCustomBlocksFile getLoadedBlocksFile() {
        return this.loadedCachedCustomBlocksFile;
    }

    public MiningPacketListener getPlayerPacketListener() {
        return this.packetListener;
    }

    public static Destructible getInstance() {
        return getPlugin(Destructible.class);
    }

    @NotNull
    public static Logger getPluginLogger() {
        return PluginLogger.getLogger("Destructible");
    }
}