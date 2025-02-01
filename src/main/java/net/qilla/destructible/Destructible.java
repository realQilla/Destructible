package net.qilla.destructible;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.qilla.destructible.command.DestructibleCommand;
import net.qilla.destructible.command.OverflowCommand;
import net.qilla.destructible.command.temp.SelectCommand;
import net.qilla.destructible.files.*;
import net.qilla.destructible.mining.item.attributes.AttributeTypes;
import net.qilla.destructible.player.GeneralListener;
import net.qilla.qlibrary.menu.MenuEventHandlers;
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

    private final LifecycleEventManager<Plugin> pluginLifecycle = this.getLifecycleManager();

    @Override
    public void onEnable() {

        CustomItemsFile.getInstance().load();
        CustomBlocksFile.getInstance().load();
        LoadedDestructibleBlocksFile.getInstance().load();

        initListeners();
        initCommands();
    }

    private void initListeners() {
        getServer().getPluginManager().registerEvents(new GeneralListener(this), this);
        getServer().getPluginManager().registerEvents(MenuEventHandlers.initiateEventHandlers(), this);
    }

    private void initCommands() {
        this.pluginLifecycle.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            new DestructibleCommand(this, event.registrar()).register();
            new OverflowCommand(this, event.registrar()).register();
            new SelectCommand(this, event.registrar()).register();
        });
    }


    public static Destructible getInstance() {
        return getPlugin(Destructible.class);
    }

    @Override
    public @NotNull Logger getLogger() {
        return LOGGER;
    }
}