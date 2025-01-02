package net.qilla.destructible.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.player.Overflow;
import net.qilla.destructible.data.Registries;
import org.bukkit.entity.Player;
import java.util.List;

public class OverflowCom {

    private static final String COMMAND = "overflow";
    private static final List<String> ALIAS = List.of("o", "stash");
    private static final String COLLECT = "collect";
    private static final String CLEAR = "clear";

    private final Destructible plugin;
    private final Commands commands;

    public OverflowCom(Destructible plugin, Commands commands) {
        this.plugin = plugin;
        this.commands = commands;
    }

    public void register() {
        this.commands.register(Commands.literal(COMMAND)
                .requires(source -> source.getSender() instanceof Player)
                .then(Commands.literal(COLLECT)
                        .executes(this::collect))
                .then(Commands.literal(CLEAR)
                        .executes(this::clear)).build(), ALIAS);
    }

    private int clear(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();

        Overflow overflow = Registries.DESTRUCTIBLE_PLAYERS.get(player.getUniqueId()).getOverflow();

        if(overflow.isEmpty()) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Your overflow stash is already empty."));
            return 0;
        }

        overflow.clear();
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Your overflow stash has been <red><bold>CLEARED<red>."));
        return Command.SINGLE_SUCCESS;
    }

    private int collect(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();

        DPlayer dPlayer = Registries.DESTRUCTIBLE_PLAYERS.get(player.getUniqueId());
        Overflow overflow = dPlayer.getOverflow();

        if(overflow.isEmpty()) {
            dPlayer.sendMessage(MiniMessage.miniMessage().deserialize("<red>Your overflow stash is empty!"));
            return 0;
        }

        if(dPlayer.getInventory().firstEmpty() == -1) {
            dPlayer.sendMessage(MiniMessage.miniMessage().deserialize("<red>Your inventory is full!"));
            return 0;
        }

        dPlayer.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>You have successfully claimed: "));
        overflow.take(dPlayer).forEach(item -> dPlayer.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>+" + item.getAmount() + " ")
                .append(item.getDItem().getDisplayName())));
        return Command.SINGLE_SUCCESS;
    }
}