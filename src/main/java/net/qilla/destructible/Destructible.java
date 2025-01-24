package net.qilla.destructible;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.command.DestructibleCommand;
import net.qilla.destructible.command.OverflowCommand;
import net.qilla.destructible.files.*;
import net.qilla.destructible.menugeneral.MenuListener;
import net.qilla.destructible.mining.item.attributes.AttributeType;
import net.qilla.destructible.mining.item.attributes.AttributeTypes;
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
    private final DExecutor dExecutor = new DExecutor(this, 4);
    private final PlayerPacketListener packetListener = new PlayerPacketListener();
    private final CustomItemsFile customItemsFile = new CustomItemsFile();
    private final CustomBlocksFile customBlocksFile = new CustomBlocksFile();
    private final LoadedDestructibleBlocksFile loadedDestructibleBlocksFile = new LoadedDestructibleBlocksFile();
    private final LoadedDestructibleBlocksGroupedFile loadedDestructibleBlocksGroupedFile = new LoadedDestructibleBlocksGroupedFile();

    @Override
    public void onEnable() {
        Bukkit.getOnlinePlayers().forEach(player -> player.kick(MiniMessage.miniMessage().deserialize("<red>Rejoin to revalidate your player information.")));

        new AttributeTypes();
        this.lifecycleMan = this.getLifecycleManager();

        this.customItemsFile.load();
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