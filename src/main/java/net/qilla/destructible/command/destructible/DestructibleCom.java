package net.qilla.destructible.command.destructible;

import com.google.common.collect.ArrayListMultimap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.DataKey;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.tool.DTool;
import net.qilla.destructible.util.ItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class DestructibleCom {

    private final Destructible plugin;
    private final Commands commands;

    private final String command = "destructible";
    private static final List<String> alias = List.of("dest", "d");
    private static final String[] toolArgs = {"tool", "type"};

    private static final String[] blockArgs = {"block", "modify", "type" , "view"};

    public DestructibleCom(final Destructible plugin, final Commands commands) {
        this.plugin = plugin;
        this.commands = commands;
    }

    public void register() {
        this.commands.register(Commands
                .literal(command)
                .requires(source -> source.getSender() instanceof Player player && player.isOp())
                .then(Commands.literal(toolArgs[0])
                        .then(Commands.argument(toolArgs[1], StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    String argument = builder.getRemaining();
                                    for(String id : Registries.DTOOLS.keySet()) {
                                        if(id.regionMatches(true, 0, argument, 0, argument.length())) {
                                            builder.suggest(id);
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(this::tool))
                )
                .then(Commands.literal(blockArgs[0])
                        .then(Commands.literal(blockArgs[1])
                                .then(Commands.argument(blockArgs[2], StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            String argument = builder.getRemaining();
                                            for(String id : Registries.DBLOCKS.keySet()) {
                                                if(id.regionMatches(true, 0, argument, 0, argument.length())) {
                                                    builder.suggest(id);
                                                }
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(this::blockModify)
                                ).executes(this::endBlockModify))
                        .then(Commands.literal(blockArgs[3])
                                .executes(this::blockView)))
                .build(), alias);
    }

    private int endBlockModify(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();

        if(Registries.DBLOCK_EDITOR.containsKey(player.getUniqueId())) {
            Registries.DBLOCK_EDITOR.remove(player.getUniqueId());
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>You are no longer modifying DBlocks in the world space."));
        } else {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You are not currently modifying DBlocks in the world space!"));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int blockModify(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        String blockStr = context.getArgument(blockArgs[2], String.class);
        DBlock dBlock = Registries.DBLOCKS.get(blockStr);
        if(dBlock == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Invalid DBlock"));
            return 0;
        }

        Registries.DBLOCK_EDITOR.put(player.getUniqueId(), dBlock);
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>You are now modifying DBlocks in the world space."));
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>All placed blocks will be converted to '<yellow>" + dBlock.getId()+ "</yellow>', any destroyed will be unregistered."));
        return Command.SINGLE_SUCCESS;
    }

    private int blockView(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();

        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>You are now viewing all DBlocks in the world space."));
        return Command.SINGLE_SUCCESS;
    }

    private int tool(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        String toolStr = context.getArgument(toolArgs[1], String.class);
        DTool dTool = Registries.DTOOLS.get(toolStr);
        if(dTool == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Invalid DTool"));
            return 0;
        }

        ItemStack item = ItemStack.of(dTool.getMaterial());

        item.editMeta(meta -> {
           meta.getPersistentDataContainer().set(DataKey.TOOL, PersistentDataType.STRING, dTool.getId());
           if(dTool.getDurability() != -1) {
               meta.getPersistentDataContainer().set(DataKey.DURABILITY, PersistentDataType.INTEGER, dTool.getDurability());
           }
           meta.displayName(dTool.getDisplayName());
           meta.setEnchantmentGlintOverride(true);

           meta.setAttributeModifiers(ArrayListMultimap.create());

           List<Component> lore = List.of(
                   MiniMessage.miniMessage().deserialize("<!italic><gray>Efficiency: " + dTool.getEfficiency()),
                   MiniMessage.miniMessage().deserialize("<!italic><gray>Strength: " + dTool.getStrength()),
                   MiniMessage.miniMessage().deserialize(""),
                   dTool.getRarity().getFormatted()
                   );

           meta.lore(lore);
        });

        if(dTool.getDurability() != -1) {
            Damageable damageable = (Damageable) item.getItemMeta();
            damageable.setMaxDamage(dTool.getDurability());
            item.setItemMeta(damageable);
        }

        ItemUtil.give(player, item);
        return Command.SINGLE_SUCCESS;
    }
}
