package net.qilla.destructible;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.command.destructible.DestructibleCom;
import net.qilla.destructible.mining.block.DBlockCache;
import net.qilla.destructible.mining.GeneralListener;
import net.qilla.destructible.mining.DListener;
import net.qilla.destructible.mining.MinePacketListener;
import net.qilla.destructible.mining.block.DBlocks;
import net.qilla.destructible.mining.item.tool.DTools;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Destructible extends JavaPlugin {

    private LifecycleEventManager<Plugin> lifecycleMan;
    private MinePacketListener packetListener;
    private DBlockCache dBlockCache;

    static {
        new DTools();
        new DBlocks();
    }

    @Override
    public void onEnable() {
        this.lifecycleMan = this.getLifecycleManager();
        this.packetListener = new MinePacketListener(this);
        this.dBlockCache = new DBlockCache(this);

        initListener();
        initCommand();
        dBlockCache.load();
    }

    private void initListener() {
        getServer().getPluginManager().registerEvents(new DListener(this), this);
        getServer().getPluginManager().registerEvents(new GeneralListener(this), this);
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

    public MinePacketListener getPlayerPacketListener() {
        return this.packetListener;
    }

    public DBlockCache getdBlockCache() {
        return this.dBlockCache;
    }

    public static Destructible getInstance() {
        return getPlugin(Destructible.class);
    }
}
