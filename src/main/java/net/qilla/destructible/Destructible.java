package net.qilla.destructible;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.command.destructible.DestructibleCom;
import net.qilla.destructible.files.CustomBlocksFile;
import net.qilla.destructible.files.CustomItemsFile;
import net.qilla.destructible.files.LoadedCachedCustomBlocksFile;
import net.qilla.destructible.mining.MiningPacketListener;
import net.qilla.destructible.mining.block.DBlocks;
import net.qilla.destructible.mining.item.DItems;
import net.qilla.destructible.mining.item.DTools;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Destructible extends JavaPlugin {

    private LifecycleEventManager<Plugin> lifecycleMan;
    private MiningPacketListener packetListener;
    private CustomItemsFile customItemsFile;
    private CustomBlocksFile customBlocksFile;
    private LoadedCachedCustomBlocksFile loadedCachedCustomBlocksFile;

    @Override
    public void onEnable() {
        this.lifecycleMan = this.getLifecycleManager();

        this.packetListener = new MiningPacketListener(this);
        this.customItemsFile = new CustomItemsFile();
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

    //public CustomDropsFile getCustomDropsFile() {
    //    return this.customDropsFile;
    //}

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
}
