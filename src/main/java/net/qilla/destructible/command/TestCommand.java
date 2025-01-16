package net.qilla.destructible.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.data.Registries;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TestCommand {

    private static final String COMMAND = "test";
    private static final List<String> ALIAS = List.of();

    private final Destructible plugin;
    private final Commands commands;

    public TestCommand(Destructible plugin, Commands commands) {
        this.plugin = plugin;
        this.commands = commands;
    }

    public void register() {
        this.commands.register(Commands.literal(COMMAND)
                .requires(source -> source.getSender() instanceof Player)
                .executes(this::test).build(), ALIAS);
    }

    private int test(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();

        DPlayer dPlayer = Registries.DESTRUCTIBLE_PLAYERS.get(player.getUniqueId());
        return Command.SINGLE_SUCCESS;
    }
}