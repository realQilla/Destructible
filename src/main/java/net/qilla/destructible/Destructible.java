package net.qilla.destructible;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.mining.DestructibleMining;
import net.qilla.destructible.mining.PlayerPacketListener;
import net.qilla.destructible.player.PlayerSetup;
import net.qilla.destructible.player.data.InstancePlayerData;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Destructible extends JavaPlugin {

    private static Destructible instance = null;

    private DestructibleMining destructibleMining = null;

    private InstancePlayerData instancePlayerData = null;
    private PlayerPacketListener playerPacketListener = null;
    private PlayerSetup playerSetup = null;

    @Override
    public void onEnable() {
        instance = this;

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
