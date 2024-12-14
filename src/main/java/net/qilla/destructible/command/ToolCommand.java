package net.qilla.destructible.command;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.DataKey;
import net.qilla.destructible.mining.item.ItemRegistry;
import net.qilla.destructible.mining.item.Tool;
import net.qilla.destructible.util.ItemUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

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
                .then(Commands.literal("get")
                .then(Commands.argument(argumentType, StringArgumentType.string())
                        .suggests((context, builder) -> {
                            final String argument = builder.getRemaining();
                            for(String id : ItemRegistry.getInstance().getTools().keySet()) {
                                if(id.regionMatches(true, 0, argument, 0, argument.length())) {
                                    builder.suggest(id);
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(this::get)))
                .build());
    }

    private int get(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        String toolName = context.getArgument(argumentType, String.class).toLowerCase();
        Tool tool = ItemRegistry.getInstance().getTool(toolName);
        if(tool == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Invalid tool"));
            return 0;
        }

        ItemStack item = ItemStack.of(tool.getMaterial());

        item.editMeta(meta -> {
           meta.getPersistentDataContainer().set(DataKey.TOOL, PersistentDataType.STRING, tool.getId());
           meta.displayName(tool.getDisplayName());
           meta.setEnchantmentGlintOverride(true);

           meta.setAttributeModifiers(ArrayListMultimap.create());

           List<Component> lore = List.of(
                   MiniMessage.miniMessage().deserialize("<!italic><gray>Efficiency: " + tool.getEfficiency()),
                   MiniMessage.miniMessage().deserialize("<!italic><gray>Strength: " + tool.getStrength()),
                   MiniMessage.miniMessage().deserialize(""),
                   tool.getRarity().getFormatted()
                   );

           meta.lore(lore);
        });

        ItemUtil.give(player, item);
        return Command.SINGLE_SUCCESS;
    }
}
