package net.qilla.destructible.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.*;
import net.qilla.destructible.menugeneral.menu.BlockMenu;
import net.qilla.destructible.menugeneral.menu.BlockOverviewMenu;
import net.qilla.destructible.menugeneral.menu.ItemOverviewMenu;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.DItemStack;
import net.qilla.destructible.player.BlockHighlight;
import net.qilla.destructible.player.CooldownType;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class DestructibleCommand {

    private static final String COMMAND = "destructible";
    private static final List<String> ALIAS = List.of("dest", "d");
    private static final String ITEM = "item";
    private static final String BLOCK = "block";
    private static final String CONFIG = "config";
    private static final String TYPE = "type";
    private static final String ALL = "all";
    private static final String BLOCK_MODIFY = "modify";
    private static final String BLOCK_VIEW = "view";
    private static final String BLOCK_INFO = "info";
    private static final String BLOCK_RECURSIVE_SIZE = "size";
    private static final String CONFIG_ITEMS = "DESTRUCTIBLE_ITEMS";
    private static final String CONFIG_TOOLS = "DESTRUCTIBLE_TOOLS";
    private static final String CONFIG_BLOCKS = "DESTRUCTIBLE_BLOCKS";
    private static final String CONFIG_LOADED_BLOCKS = "LOADED_DESTRUCTIBLE_BLOCKS";
    private static final String CONFIG_SAVE = "save";
    private static final String CONFIG_CLEAR = "clear";
    private static final String CONFIG_RESET = "reset";
    private static final String CONFIG_LOAD = "load";

    private final Destructible plugin;
    private final Commands commands;


    public DestructibleCommand(final Destructible plugin, final Commands commands) {
        this.plugin = plugin;
        this.commands = commands;
    }

    public void register() {
        this.commands.register(Commands
                .literal(COMMAND)
                .requires(source -> source.getSender() instanceof Player player && player.isOp())
                .then(Commands.literal(ITEM)
                        .executes(this::itemMenu)
                        .then(Commands.argument(TYPE, StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    String argument = builder.getRemaining();
                                    for(String id : DRegistry.DESTRUCTIBLE_ITEMS.keySet()) {
                                        if(id.regionMatches(true, 0, argument, 0, argument.length())) {
                                            builder.suggest(id);
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(this::item))
                )
                .then(Commands.literal(BLOCK)
                        .executes(this::blockMenu)
                        .then(Commands.literal(BLOCK_MODIFY)
                                .then(Commands.argument(TYPE, StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            String argument = builder.getRemaining();

                                            for(String id : DRegistry.DESTRUCTIBLE_BLOCKS.keySet()) {
                                                if(id.regionMatches(true, 0, argument, 0, argument.length())) {
                                                    builder.suggest(id);
                                                }
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(this::blockModify)
                                        .then(Commands.argument(BLOCK_RECURSIVE_SIZE, IntegerArgumentType.integer(1, 65792))
                                                .executes(this::blockModifyRecursiveSize))
                                ).executes(this::endBlockModify))
                        .then(Commands.literal(BLOCK_VIEW)
                                .then(Commands.argument(TYPE, StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            String argument = builder.getRemaining();

                                            for(String id : DRegistry.DESTRUCTIBLE_BLOCKS.keySet()) {
                                                if(id.regionMatches(true, 0, argument, 0, argument.length())) {
                                                    builder.suggest(id);
                                                }
                                            }
                                            return builder.buildFuture();
                                        }).executes(this::blockView))
                                .then(Commands.literal(ALL)
                                        .executes(this::blockViewAll)))
                        .then(Commands.literal(BLOCK_INFO)
                                .executes(this::info))
                )
                .then(Commands.literal(CONFIG)
                        .then(Commands.literal(CONFIG_ITEMS)
                                .then(Commands.literal(CONFIG_SAVE)
                                        .executes(this::saveCustomItems))
                                .then(Commands.literal(CONFIG_LOAD)
                                        .executes(this::loadCustomItems))
                                .then(Commands.literal(CONFIG_RESET)
                                        .executes(this::resetCustomItems))
                                .then(Commands.literal(CONFIG_CLEAR)
                                        .executes(this::clearCustomItems)))
                        .then(Commands.literal(CONFIG_BLOCKS)
                                .then(Commands.literal(CONFIG_SAVE)
                                        .executes(this::saveCustomBlocks))
                                .then(Commands.literal(CONFIG_LOAD)
                                        .executes(this::loadCustomBlocks))
                                .then(Commands.literal(CONFIG_RESET)
                                        .executes(this::resetCustomBlocks))
                                .then(Commands.literal(CONFIG_CLEAR)
                                        .executes(this::clearCustomBlocks)))
                        .then(Commands.literal(CONFIG_LOADED_BLOCKS)
                                .then(Commands.literal(CONFIG_SAVE)
                                        .executes(this::saveLoadedBlocks))
                                .then(Commands.literal(CONFIG_CLEAR)
                                        .executes(this::clearLoadedBlocks))
                        )).build(), ALIAS);
    }

    private int item(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        DPlayer dPlayer = DRegistry.DESTRUCTIBLE_PLAYERS.get(player.getUniqueId());
        String toolStr = context.getArgument(TYPE, String.class);
        DItem dTool = DRegistry.DESTRUCTIBLE_ITEMS.get(toolStr);
        if(dTool == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>An invalid Destructible tool was specified!"));
            return 0;
        }

        dPlayer.give(DItemStack.of(dTool, 1));
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>You received ").append(dTool.getDisplayName()).append(MiniMessage.miniMessage().deserialize("<green>!")));
        return Command.SINGLE_SUCCESS;
    }

    private int itemMenu(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        DPlayer dPlayer = DRegistry.DESTRUCTIBLE_PLAYERS.get(player.getUniqueId());

        if(dPlayer.getCooldown().has(CooldownType.OPEN_MENU)) {
            dPlayer.sendMessage("<red>Please wait a bit before accessing this menu.");
            return 0;
        }
        dPlayer.getCooldown().set(CooldownType.OPEN_MENU);

        dPlayer.getMenuHolder().newMenu(new ItemOverviewMenu(dPlayer));
        return Command.SINGLE_SUCCESS;
    }

    private int blockMenu(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        DPlayer dPlayer = DRegistry.DESTRUCTIBLE_PLAYERS.get(player.getUniqueId());

        if(dPlayer.getCooldown().has(CooldownType.OPEN_MENU)) {
            dPlayer.sendMessage("<red>Please wait a bit before accessing this menu.");
            return 0;
        }
        dPlayer.getCooldown().set(CooldownType.OPEN_MENU);

        dPlayer.getMenuHolder().newMenu(new BlockMenu(dPlayer));
        return Command.SINGLE_SUCCESS;
    }

    private int endBlockModify(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        DPlayer dPlayer = DRegistry.DESTRUCTIBLE_PLAYERS.get(player.getUniqueId());

        if(dPlayer.hasDBlockEdit()) {
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                dPlayer.getDBlockEdit().getBlockHighlight().removeHighlightsAll();
                dPlayer.removeDBlockEdit();
                DRegistry.DESTRUCTIBLE_BLOCK_EDITORS.remove(dPlayer);
            });
            dPlayer.sendPacket(new ClientboundRemoveMobEffectPacket(dPlayer.getCraftPlayer().getEntityId(), MobEffects.NIGHT_VISION));
            player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>You are no longer in Destructible modification mode!"));
        } else {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You are not currently in Destructible modification mode!"));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int blockModify(CommandContext<CommandSourceStack> context) {
        return blockModify(context, 0);
    }

    private int blockModifyRecursiveSize(CommandContext<CommandSourceStack> context) {
        return blockModify(context, context.getArgument(BLOCK_RECURSIVE_SIZE, Integer.class));
    }

    private int blockModify(CommandContext<CommandSourceStack> context, int size) {
        Player player = (Player) context.getSource().getSender();
        String blockStr = context.getArgument(TYPE, String.class);
        DBlock dBlock = DRegistry.DESTRUCTIBLE_BLOCKS.get(blockStr);
        if(dBlock == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>An invalid Destructible block was specified."));
            return 0;
        }

        DPlayer dPlayer = DRegistry.DESTRUCTIBLE_PLAYERS.get(player.getUniqueId());
        dPlayer.getDBlockEdit().setDblock(dBlock);
        dPlayer.getDBlockEdit().setRecursionSize(size);
        dPlayer.getDBlockEdit().getBlockHighlight().addVisibleDBlock(dBlock.getId());
        DRegistry.DESTRUCTIBLE_BLOCK_EDITORS.add(dPlayer);

        dPlayer.sendPacket(new ClientboundUpdateMobEffectPacket(dPlayer.getCraftPlayer().getEntityId(), new MobEffectInstance(MobEffects.NIGHT_VISION, -1), false));
        String message = size > 0
                ? "<yellow>You have enabled Destructible <red><bold>RECURSIVE</red> build mode, <gold>" + size + "</gold> adjacent blocks will be recursively set to <gold>" + dBlock.getId() + "</gold>."
                : "<yellow>You have enabled Destructible build mode, all place blocks will be marked as <gold>" + dBlock.getId() + "</gold>.";
        player.sendMessage(MiniMessage.miniMessage().deserialize(message));
        return Command.SINGLE_SUCCESS;
    }

    private int blockView(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        String blockStr = context.getArgument(TYPE, String.class);
        DPlayer dPlayer = DRegistry.DESTRUCTIBLE_PLAYERS.get(player.getUniqueId());
        BlockHighlight blockHighlight = dPlayer.getDBlockEdit().getBlockHighlight();

        if(DRegistry.DESTRUCTIBLE_BLOCKS.get(blockStr) == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>An invalid Destructible block was specified."));
            return 0;
        }

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            blockHighlight.removeHighlights(blockStr);

            if(!blockHighlight.isDBlockVisible(blockStr)) {
                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Destructible block <gold><bold>" + blockStr + "</gold> is now <green><bold>VISIBLE</green>."));
                });
                blockHighlight.addVisibleDBlock(blockStr);
                blockHighlight.createHighlights(blockStr);
            } else {
                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Destructible block <gold><bold>" + blockStr + "</gold> is now <red><bold>INVISIBLE</red>."));
                });
                blockHighlight.removeVisibleDBlock(blockStr);
                blockHighlight.removeHighlights(blockStr);
            }
        });
        return Command.SINGLE_SUCCESS;
    }

    private int blockViewAll(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        DPlayer dPlayer = DRegistry.DESTRUCTIBLE_PLAYERS.get(player.getUniqueId());
        BlockHighlight blockHighlight = dPlayer.getDBlockEdit().getBlockHighlight();

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            blockHighlight.removeHighlightsAll();

            if(!blockHighlight.isDBlockVisibleAny()) {
                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    serverPlayer.connection.send(new ClientboundUpdateMobEffectPacket(serverPlayer.getId(), new MobEffectInstance(MobEffects.NIGHT_VISION, -1), false));
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>You can now see all Destructible blocks."));
                });
                blockHighlight.addVisibleDBlockAll();
                blockHighlight.createVisibleHighlights();
            } else {
                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    serverPlayer.connection.send(new ClientboundRemoveMobEffectPacket(serverPlayer.getId(), MobEffects.NIGHT_VISION));
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>You can no longer see Destructible blocks."));
                });
                blockHighlight.removeVisibleDBlockAll();
                blockHighlight.removeHighlightsAll();
            }
        });
        return Command.SINGLE_SUCCESS;
    }

    private int saveLoadedBlocks(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();

        this.plugin.getLoadedBlocksFile().save();
        this.plugin.getLoadedBlocksGroupedFile().save();
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>All cached Destructible blocks changes have been <green><bold>SAVED</green>!"));
        return Command.SINGLE_SUCCESS;
    }

    private int clearLoadedBlocks(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            this.plugin.getLoadedBlocksFile().clear();
            this.plugin.getLoadedBlocksGroupedFile().clear();
            DRegistry.DESTRUCTIBLE_BLOCK_EDITORS.forEach(dPlayer -> dPlayer.getDBlockEdit().getBlockHighlight().removeHighlightsAll());
        });
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>All cached Destructible blocks have been <red><bold>CLEARED</red>!"));
        return Command.SINGLE_SUCCESS;
    }

    private int saveCustomItems(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();

        this.plugin.getCustomItemsFile().save();
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Destructible item changes have been <green><bold>SAVED</green> to config!"));
        return Command.SINGLE_SUCCESS;
    }

    private int loadCustomItems(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();

        this.plugin.getCustomItemsFile().load();
        this.plugin.getCustomToolsFile().load();
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Destructible item changes have been <green><bold>LOADED</green> from config!"));
        return Command.SINGLE_SUCCESS;
    }

    private int resetCustomItems(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();

        this.plugin.getCustomItemsFile().reset();
        this.plugin.getCustomToolsFile().reset();
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>All custom Destructible items have been <red><bold>RESET</red>!"));
        return Command.SINGLE_SUCCESS;
    }

    private int clearCustomItems(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();

        this.plugin.getCustomItemsFile().clear();
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>All custom Destructible items have been <red><bold>CLEARED</red>!"));
        return Command.SINGLE_SUCCESS;
    }

    private int saveCustomBlocks(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();

        this.plugin.getCustomBlocksFile().save();
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Destructible block changes have been <green><bold>SAVED</green> to config!"));
        return Command.SINGLE_SUCCESS;
    }

    private int loadCustomBlocks(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();

        this.plugin.getCustomBlocksFile().load();
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Destructible block changes have been <green><bold>LOADED</green> from config!"));
        return Command.SINGLE_SUCCESS;
    }

    private int resetCustomBlocks(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();

        this.plugin.getCustomBlocksFile().reset();
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>All custom Destructible blocks have been <red><bold>RESET</red>!"));
        return Command.SINGLE_SUCCESS;
    }

    private int clearCustomBlocks(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();

        this.plugin.getCustomBlocksFile().clear();
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>All custom Destructible blocks have been <red><bold>CLEARED</red>!"));
        return Command.SINGLE_SUCCESS;
    }

    private int info(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();

        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>There are currently <gold>" +
                NumberUtil.numberComma(DRegistry.LOADED_DESTRUCTIBLE_BLOCKS.values().stream().mapToInt(Map::size).sum()) + "</gold> Destructible blocks spread across <gold>" +
                NumberUtil.numberComma(DRegistry.LOADED_DESTRUCTIBLE_BLOCKS.size()) + "</gold> chunks."));
        return Command.SINGLE_SUCCESS;
    }
}