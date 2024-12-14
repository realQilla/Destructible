package net.qilla.destructible.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.mining.item.ItemRegistry;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class TestCommand {

    private final Destructible plugin;
    private final Commands commands;

    private final String command = "test";
    private final List<String> alias = List.of();

    public TestCommand(final Destructible plugin, final Commands commands) {
        this.plugin = plugin;
        this.commands = commands;
    }

    public void register() {
        this.commands.register(Commands
                .literal(command)
                .requires(source -> source.getSender() instanceof Player player && player.isOp())
                        .executes(this::test)
                .build());

    }

    private int test(CommandContext<CommandSourceStack> context) {
        if(!(context.getSource().getSender() instanceof Player player)) return 0;
        final Location loc = player.getLocation();
        player.sendMessage(ItemRegistry.getInstance().getTools().size() + "");

        player.sendMessage("Command Success!");
        return Command.SINGLE_SUCCESS;
    }
}
