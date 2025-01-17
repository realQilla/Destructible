package net.qilla.destructible;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.command.DestructibleCommand;
import net.qilla.destructible.command.OverflowCommand;
import net.qilla.destructible.command.TestCommand;
import net.qilla.destructible.files.*;
import net.qilla.destructible.menugeneral.MenuListener;
import net.qilla.destructible.player.PlayerPacketListener;
import net.qilla.destructible.mining.MiningListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class Destructible extends JavaPlugin {

    private LifecycleEventManager<Plugin> lifecycleMan;
    private List<Thread> activeThreads;
    private PlayerPacketListener packetListener;
    private CustomItemsFile customItemsFile;
    private CustomToolsFile customToolsFile;
    private CustomBlocksFile customBlocksFile;
    private LoadedDestructibleBlocksFile loadedDestructibleBlocksFile;
    private LoadedDestructibleBlocksGroupedFile loadedDestructibleBlocksGroupedFile;

    @Override
    public void onEnable() {
        this.lifecycleMan = this.getLifecycleManager();
        this.activeThreads = new ArrayList<>();

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
        getServer().getPluginManager().registerEvents(new MiningListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
    }

    private void initCommand() {
        this.lifecycleMan.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();
            new DestructibleCommand(this, commands).register();
            new OverflowCommand(this, commands).register();
            new TestCommand(this, commands).register();
        });
    }

    public void addThread(Thread thread) {
        this.activeThreads.add(thread);
    }

    public void removeThread(Thread thread) {
        this.activeThreads.remove(thread);
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(player -> player.kick(MiniMessage.miniMessage().deserialize("<red>Server Reloaded.")));
        this.activeThreads.forEach(Thread::interrupt);
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