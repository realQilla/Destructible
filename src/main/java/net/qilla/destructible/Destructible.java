package net.qilla.destructible;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.command.DestructibleCommand;
import net.qilla.destructible.command.OverflowCommand;
import net.qilla.destructible.command.temp.SelectCommand;
import net.qilla.destructible.files.*;
import net.qilla.destructible.menugeneral.MenuListener;
import net.qilla.destructible.mining.item.attributes.AttributeTypes;
import net.qilla.destructible.player.GeneralListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.logging.Logger;

public final class Destructible extends JavaPlugin {

    private final Logger LOGGER = new PluginLogger(this);

    static {
        try {
            Class.forName(AttributeTypes.class.getName());
        } catch(ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private final LifecycleEventManager<Plugin> pluginLifecycle =this.getLifecycleManager();
    private final CustomItemsFile customItemsFile = CustomItemsFile.getInstance();
    private final CustomBlocksFile customBlocksFile = CustomBlocksFile.getInstance();
    private final LoadedDestructibleBlocksFile loadedDestructibleBlocksFile = LoadedDestructibleBlocksFile.getInstance();
    private final LoadedDestructibleBlocksGroupedFile loadedDestructibleBlocksGroupedFile = LoadedDestructibleBlocksGroupedFile.getInstance();

    @Override
    public void onEnable() {
        Bukkit.getOnlinePlayers().forEach(player -> player.kick(MiniMessage.miniMessage().deserialize("<red>Rejoin to revalidate your player information.")));

        this.customItemsFile.load();
        this.customBlocksFile.load();
        this.loadedDestructibleBlocksFile.load();
        this.loadedDestructibleBlocksGroupedFile.load();

        initListeners();
        initCommands();
    }

    private void initListeners() {
        getServer().getPluginManager().registerEvents(new GeneralListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
    }

    private void initCommands() {
        this.pluginLifecycle.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            new DestructibleCommand(this, event.registrar()).register();
            new OverflowCommand(this, event.registrar()).register();
            new SelectCommand(this, event.registrar()).register();
        });
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(player -> player.kick(MiniMessage.miniMessage().deserialize("<red>Server Reloaded.")));
    }


    public static Destructible getInstance() {
        return getPlugin(Destructible.class);
    }

    @Override
    public @NotNull Logger getLogger() {
        return LOGGER;
    }
}