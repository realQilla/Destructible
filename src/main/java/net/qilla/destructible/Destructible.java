package net.qilla.destructible;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.command.ToolCommand;
import net.qilla.destructible.mining.PlayerPacketListener;
import net.qilla.destructible.mining.block.DBlocks;
import net.qilla.destructible.mining.item.tool.DTools;
import net.qilla.destructible.mining.player.PlayerSetup;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Destructible extends JavaPlugin {

    private LifecycleEventManager<Plugin> lifecycleMan;
    private PlayerPacketListener playerPacketListener;

    static {
        new DTools();
        new DBlocks();
    }

    @Override
    public void onEnable() {
        this.lifecycleMan = this.getLifecycleManager();
        this.playerPacketListener = new PlayerPacketListener(this);

        initListener();
        initCommand();
    }

    private void initListener() {
        getServer().getPluginManager().registerEvents(new PlayerSetup(this), this);
    }

    private void initCommand() {
        this.lifecycleMan.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();
            new ToolCommand(this, commands).register();
        });
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(player -> player.kick(MiniMessage.miniMessage().deserialize("<red>Server Reloaded.")));
    }

    public PlayerPacketListener getPlayerPacketListener() {
        return this.playerPacketListener;
    }

    public static Destructible getInstance() {
        return getPlugin(Destructible.class);
    }
}
