package net.qilla.destructible.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.menugeneral.menu.OverflowMenu;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.data.registry.DRegistryMaster;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OverflowCommand {

    private static final Map<UUID, DPlayer> DPLAYERS = DRegistry.DPLAYERS;
    private static final String COMMAND = "overflow";
    private static final List<String> ALIAS = List.of("o", "stash");
    private static final String COLLECT = "collect";
    private static final String CLEAR = "clear";

    private final Destructible plugin;
    private final Commands commands;

    public OverflowCommand(Destructible plugin, Commands commands) {
        this.plugin = plugin;
        this.commands = commands;
    }

    public void register() {
        this.commands.register(Commands.literal(COMMAND)
                .requires(source -> source.getSender() instanceof Player)
                .executes(this::openMenu).build(), ALIAS);
    }

    private int openMenu(CommandContext<CommandSourceStack> context) {
        UUID uuid = ((Player) context.getSource().getSender()).getUniqueId();
        DPlayer dPlayer = DPLAYERS.get(uuid);

        if(dPlayer.getCooldown().has(CooldownType.OPEN_MENU)) {
            dPlayer.sendMessage("<red>Please wait a bit before accessing this menu.");
            return 0;
        }
        dPlayer.getCooldown().has(CooldownType.OPEN_MENU);

        dPlayer.getMenuHolder().newMenu(new OverflowMenu(plugin, dPlayer));
        return Command.SINGLE_SUCCESS;
    }
}