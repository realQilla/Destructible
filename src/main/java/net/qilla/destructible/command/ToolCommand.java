package net.qilla.destructible.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.qilla.destructible.Destructible;
import org.bukkit.entity.Player;

import java.util.List;

public class ToolCommand {

    private final Destructible plugin;
    private final Commands commands;

    private final String command = "tool";
    private final List<String> alias = List.of();
    private final String argumentType = "type";

    public ToolCommand(final Destructible plugin, final Commands commands) {
        this.plugin = plugin;
        this.commands = commands;
    }

    public void register() {
        this.commands.register(Commands
                .literal(command)
                .requires(source -> source.getSender() instanceof Player player && player.isOp())
                .then(Commands.literal("get"))
                .then(Commands.literal("remove"))
                .build());

    }

    private int usage(CommandContext<CommandSourceStack> context) {

        return 0;
    }

    private int get(CommandContext<CommandSourceStack> context) {

        return Command.SINGLE_SUCCESS;
    }
}
