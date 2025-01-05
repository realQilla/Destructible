package net.qilla.destructible.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.menus.DestructibleMenu;
import net.qilla.destructible.menus.OverflowMenu;
import net.qilla.destructible.mining.item.DItemStack;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.player.Overflow;
import net.qilla.destructible.data.Registries;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import java.util.List;

public class OverflowCommand {

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
                .executes(this::openGUI)
                //.then(Commands.literal(COLLECT)
                //.executes(this::collect))
                .then(Commands.literal(CLEAR)
                        .executes(this::clear)).build(), ALIAS);
    }

    private int openGUI(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        DPlayer dPlayer = Registries.DESTRUCTIBLE_PLAYERS.get(player.getUniqueId());

        if(dPlayer.getCooldown().has(CooldownType.OPEN_MENU)) {
            dPlayer.sendMessage("<red>Please wait a bit before accessing this menu.");
            return 0;
        }
        dPlayer.getCooldown().has(CooldownType.OPEN_MENU);

        DestructibleMenu gui = new OverflowMenu(dPlayer);
        gui.openInventory();
        return Command.SINGLE_SUCCESS;
    }

    private int clear(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();

        DPlayer dPlayer = Registries.DESTRUCTIBLE_PLAYERS.get(player.getUniqueId());

        if(dPlayer.getOverflow().isEmpty()) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Your overflow stash is already empty."));
            return 0;
        }

        dPlayer.getOverflow().clear();
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 0.5f, 0.0f);
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Your overflow stash has been <red><bold>CLEARED<red>."));
        return Command.SINGLE_SUCCESS;
    }

    private int collect(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();

        DPlayer dPlayer = Registries.DESTRUCTIBLE_PLAYERS.get(player.getUniqueId());
        Overflow overflow = dPlayer.getOverflow();

        if(overflow.isEmpty()) {
            dPlayer.sendMessage("<red>Your overflow stash is empty!");
            return 0;
        }

        if(dPlayer.getCraftPlayer().getInventory().firstEmpty() == -1) {
            dPlayer.sendMessage("<red>Your inventory is full!");
            return 0;
        }

        List<DItemStack> itemList = overflow.take();

        if(itemList.isEmpty()) {
            dPlayer.sendMessage("<red>There was a problem claiming your items!");
            return 0;
        }

        itemList.forEach(dPlayer::give);
        dPlayer.sendMessage("<yellow>You have successfully claimed: ");
        itemList.forEach(item -> dPlayer.getCraftPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<yellow>+" + item.getAmount() + " ")
                .append(item.getDItem().getDisplayName())));
        dPlayer.getCraftPlayer().playSound(dPlayer.getCraftPlayer().getLocation(), Sound.ENTITY_HORSE_SADDLE, 1.0f, 1.0f);
        return Command.SINGLE_SUCCESS;
    }
}