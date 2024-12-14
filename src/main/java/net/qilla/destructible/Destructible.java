package net.qilla.destructible;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.command.TestCommand;
import net.qilla.destructible.command.ToolCommand;
import net.qilla.destructible.mining.DestructibleMining;
import net.qilla.destructible.mining.PlayerPacketListener;
import net.qilla.destructible.mining.block.DestructibleBlocks;
import net.qilla.destructible.mining.item.*;
import net.qilla.destructible.mining.player.PlayerSetup;
import net.qilla.destructible.mining.player.data.InstancePlayerData;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Destructible extends JavaPlugin {

    private final LifecycleEventManager<Plugin> lifecycleMan = this.getLifecycleManager();
    private DestructibleMining destructibleMining = null;
    private InstancePlayerData instancePlayerData = null;
    private PlayerPacketListener playerPacketListener = null;
    private PlayerSetup playerSetup = null;

    static {
        Class<?> toolsClass = Tools.class;
        Class<?> destructibleBlocksClass = DestructibleBlocks.class;
    }

    @Override
    public void onEnable() {


        this.instancePlayerData = new InstancePlayerData();
        this.destructibleMining = new DestructibleMining();
        this.playerPacketListener = new PlayerPacketListener(this.destructibleMining, this.instancePlayerData);
        this.playerSetup = new PlayerSetup(this);

        initListener();
        initCommand();
    }

    private void initListener() {
        getServer().getPluginManager().registerEvents(new PlayerSetup(this), this);
    }

    private void initCommand() {
        this.lifecycleMan.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            new ToolCommand(this, commands).register();
            new TestCommand(this, commands).register();
        });
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(player -> player.kick(MiniMessage.miniMessage().deserialize("<red>Server Reloaded.")));
    }

    public DestructibleMining getDestructibleMining() {
        return this.destructibleMining;
    }

    public InstancePlayerData getInstancePlayerData() {
        return this.instancePlayerData;
    }

    public PlayerPacketListener getPlayerPacketListener() {
        return this.playerPacketListener;
    }

    public static Destructible getInstance() {
        return getPlugin(Destructible.class);
    }
}
