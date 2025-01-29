package net.qilla.destructible.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.registry.DPlayerDataRegistry;
import net.qilla.destructible.menugeneral.menu.BlockCoreMenu;
import net.qilla.destructible.menugeneral.menu.ItemOverviewMenu;
import net.qilla.destructible.player.DPlayerData;
import net.qilla.qlibrary.data.PlayerData;
import net.qilla.qlibrary.player.CooldownType;
import org.bukkit.entity.Player;

import java.util.*;

public class DestructibleCommand {

    private static final DPlayerDataRegistry PLAYER_DATA_REGISTRY = DPlayerDataRegistry.getInstance();
    private static final String COMMAND = "destructible";
    private static final List<String> ALIAS = List.of("dest", "d");
    private static final String ITEM = "item";
    private static final String TOOL = "tool";
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
                .then(Commands.literal(TOOL)
                        .executes(this::itemMenu)
                )
                .then(Commands.literal(BLOCK)
                        .executes(this::blockMenu)
                ).build(), ALIAS);
    }

    private int itemMenu(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        DPlayerData playerData = PLAYER_DATA_REGISTRY.getData(player);

        if(playerData.hasCooldown(CooldownType.OPEN_MENU)) {
            playerData.getPlayer().sendMessage("<red>Please wait a bit before accessing this menu.");
            return 0;
        }
        playerData.setCooldown(CooldownType.OPEN_MENU);

        playerData.newMenu(new ItemOverviewMenu(plugin, playerData));
        return Command.SINGLE_SUCCESS;
    }

    private int blockMenu(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        DPlayerData playerData = PLAYER_DATA_REGISTRY.getData(player);

        if(playerData.hasCooldown(CooldownType.OPEN_MENU)) {
            playerData.getPlayer().sendMessage("<red>Please wait a bit before accessing this menu.");
            return 0;
        }
        playerData.setCooldown(CooldownType.OPEN_MENU);

        playerData.newMenu(new BlockCoreMenu(plugin, playerData));
        return Command.SINGLE_SUCCESS;
    }
}