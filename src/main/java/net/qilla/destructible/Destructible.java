package net.qilla.destructible;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.command.DestructibleCommand;
import net.qilla.destructible.command.OverflowCommand;
import net.qilla.destructible.files.*;
import net.qilla.destructible.menugeneral.MenuListener;
import net.qilla.destructible.player.PlayerPacketListener;
import net.qilla.destructible.player.GeneralListener;
import net.qilla.destructible.util.DExecutor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public final class Destructible extends JavaPlugin {

    private LifecycleEventManager<Plugin> lifecycleMan;
    private DExecutor dExecutor;
    private PlayerPacketListener packetListener;
    private CustomItemsFile customItemsFile;
    private CustomToolsFile customToolsFile;
    private CustomBlocksFile customBlocksFile;
    private LoadedDestructibleBlocksFile loadedDestructibleBlocksFile;
    private LoadedDestructibleBlocksGroupedFile loadedDestructibleBlocksGroupedFile;

    @Override
    public void onEnable() {
        this.lifecycleMan = this.getLifecycleManager();
        this.dExecutor = new DExecutor(this, 4);

        this.packetListener = new PlayerPacketListener();
        this.customItemsFile = new CustomItemsFile();
        this.customToolsFile = new CustomToolsFile();
        this.customBlocksFile = new CustomBlocksFile();
        this.loadedDestructibleBlocksFile = new LoadedDestructibleBlocksFile();
        this.loadedDestructibleBlocksGroupedFile = new LoadedDestructibleBlocksGroupedFile();

        this.customItemsFile.load();
        this.customToolsFile.load();
        this.customBlocksFile.load();
        this.loadedDestructibleBlocksFile.load();
        this.loadedDestructibleBlocksGroupedFile.load();

        initListener();
        initCommand();
    }

    private void initListener() {
        getServer().getPluginManager().registerEvents(new GeneralListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
    }

    private void initCommand() {
        this.lifecycleMan.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();
            new DestructibleCommand(this, commands).register();
            new OverflowCommand(this, commands).register();
            //new TestCommand(this, commands).register();
        });
    }
    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(player -> player.kick(MiniMessage.miniMessage().deserialize("<red>Server Reloaded.")));
        if(dExecutor != null) dExecutor.shutdown();
    }

    public DExecutor getdExecutor() {
        return this.dExecutor;
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


    public LoadedDestructibleBlocksFile getLoadedBlocksFile() {
        return this.loadedDestructibleBlocksFile;
    }
    public LoadedDestructibleBlocksGroupedFile getLoadedBlocksGroupedFile() {
        return this.loadedDestructibleBlocksGroupedFile;
    }

    public PlayerPacketListener getPlayerPacketListener() {
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