package net.qilla.destructible.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.menugeneral.menu.OverflowMenu;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.player.Overflow;
import net.qilla.destructible.data.DRegistry;
import net.qilla.destructible.util.ComponentUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
                .executes(this::openMenu).build(), ALIAS);
    }

    private int openMenu(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        DPlayer dPlayer = DRegistry.DESTRUCTIBLE_PLAYERS.get(player.getUniqueId());

        if(dPlayer.getCooldown().has(CooldownType.OPEN_MENU)) {
            dPlayer.sendMessage("<red>Please wait a bit before accessing this menu.");
            return 0;
        }
        dPlayer.getCooldown().has(CooldownType.OPEN_MENU);

        dPlayer.getMenuHolder().newMenu(new OverflowMenu(dPlayer));
        return Command.SINGLE_SUCCESS;
    }

    private int clear(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();

        DPlayer dPlayer = DRegistry.DESTRUCTIBLE_PLAYERS.get(player.getUniqueId());

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

        DPlayer dPlayer = DRegistry.DESTRUCTIBLE_PLAYERS.get(player.getUniqueId());
        Overflow overflow = dPlayer.getOverflow();

        if(overflow.isEmpty()) {
            dPlayer.sendMessage("<red>Your overflow stash is empty!");
            return 0;
        }

        if(dPlayer.getCraftPlayer().getInventory().firstEmpty() == -1) {
            dPlayer.sendMessage("<red>Your inventory is full!");
            return 0;
        }

        List<ItemStack> itemStacks = overflow.take();

        if(itemStacks.isEmpty()) {
            dPlayer.sendMessage("<red>There was a problem claiming your items!");
            return 0;
        }

        itemStacks.forEach(dPlayer::give);
        dPlayer.sendMessage("<yellow>You have successfully claimed: ");
        itemStacks.forEach(item -> dPlayer.getCraftPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<yellow>+").append(ComponentUtil.getItemAmountAndType(item))));
        dPlayer.getCraftPlayer().playSound(dPlayer.getCraftPlayer().getLocation(), Sound.ENTITY_HORSE_SADDLE, 1.0f, 1.0f);
        return Command.SINGLE_SUCCESS;
    }
}