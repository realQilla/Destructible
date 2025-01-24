package net.qilla.destructible.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.data.registry.DRegistryMaster;
import net.qilla.destructible.menugeneral.menu.BlockMenu;
import net.qilla.destructible.menugeneral.menu.ItemOverviewMenu;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class DestructibleCommand {

    private static final Map<UUID, DPlayer> DPLAYERS = DRegistry.DPLAYERS;
    private static final String COMMAND = "destructible";
    private static final List<String> ALIAS = List.of("dest", "d");
    private static final String ITEM = "item";
    private static final String BLOCK = "block";

    private final Destructible plugin;
    private final Commands commands;


    public DestructibleCommand(final Destructible plugin, final Commands commands) {
        this.plugin = plugin;
        this.commands = commands;
    }

    public void register() {
        this.commands.register(Commands
                .literal(COMMAND)
                .requires(source -> source.getSender() instanceof Player player && player.isOp())
                .then(Commands.literal(ITEM)
                        .executes(this::itemMenu)
                )
                .then(Commands.literal(BLOCK)
                        .executes(this::blockMenu)
                ).build(), ALIAS);
    }

    private int itemMenu(CommandContext<CommandSourceStack> context) {
        UUID uuid = ((Player) context.getSource().getSender()).getUniqueId();
        DPlayer dPlayer = DPLAYERS.get(uuid);

        if(dPlayer.getCooldown().has(CooldownType.OPEN_MENU)) {
            dPlayer.sendMessage("<red>Please wait a bit before accessing this menu.");
            return 0;
        }
        dPlayer.getCooldown().set(CooldownType.OPEN_MENU);

        dPlayer.getMenuHolder().newMenu(new ItemOverviewMenu(plugin, dPlayer));
        return Command.SINGLE_SUCCESS;
    }

    private int blockMenu(CommandContext<CommandSourceStack> context) {
        UUID uuid = ((Player) context.getSource().getSender()).getUniqueId();
        DPlayer dPlayer = DPLAYERS.get(uuid);

        if(dPlayer.getCooldown().has(CooldownType.OPEN_MENU)) {
            dPlayer.sendMessage("<red>Please wait a bit before accessing this menu.");
            return 0;
        }
        dPlayer.getCooldown().set(CooldownType.OPEN_MENU);

        dPlayer.getMenuHolder().newMenu(new BlockMenu(plugin, dPlayer));
        return Command.SINGLE_SUCCESS;
    }
}