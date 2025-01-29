package net.qilla.destructible.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.registry.DPlayerDataRegistry;
import net.qilla.destructible.menugeneral.menu.OverflowMenu;
import net.qilla.destructible.player.DPlayerData;
import net.qilla.qlibrary.player.CooldownType;
import org.bukkit.entity.Player;
import java.util.List;

public class OverflowCommand {

    private static final DPlayerDataRegistry PLAYER_DATA_REGISTRY = DPlayerDataRegistry.getInstance();
    private static final String COMMAND = "overflow";
    private static final List<String> ALIAS = List.of("o", "stash");

    private final Destructible plugin;
    private final Commands commands;

    public OverflowCommand(final Destructible plugin, final Commands commands) {
        this.plugin = plugin;
        this.commands = commands;
    }

    public void register() {
        this.commands.register(Commands.literal(COMMAND)
                .requires(source -> source.getSender() instanceof Player)
                .executes(this::openMenu).build(), ALIAS);
    }

    private int openMenu(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        DPlayerData playerData = PLAYER_DATA_REGISTRY.getData(player);

        if(playerData.hasCooldown(CooldownType.OPEN_MENU)) {
            playerData.getPlayer().sendMessage("<red>Please wait a bit before accessing this menu.");
            return 0;
        }
        playerData.hasCooldown(CooldownType.OPEN_MENU);

        playerData.newMenu(new OverflowMenu(plugin, playerData));
        return Command.SINGLE_SUCCESS;
    }
}