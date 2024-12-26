package net.qilla.destructible.command.destructible;

import com.google.common.collect.ArrayListMultimap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.*;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.tool.DTool;
import net.qilla.destructible.util.CoordUtil;
import net.qilla.destructible.util.EntityUtil;
import net.qilla.destructible.util.FormatUtil;
import net.qilla.destructible.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class DestructibleCom {
    private final Destructible plugin;
    private final Commands commands;
    private static final String command = "destructible";
    private static final List<String> alias = List.of("dest", "d");
    private static final String TOOLS = "tools";
    private static final String BLOCKS = "blocks";
    private static final String TOOL_TYPE = "type";
    private static final String BLOCK_MODIFY = "modify";
    private static final String BLOCK_TYPE = "type";
    private static final String BLOCK_RECURSIVE = "recursive";
    private static final String BLOCK_VIEW = "view";
    private static final String BLOCK_SAVE = "save";
    private static final String BLOCK_CLEAR = "clear";
    private static final String BLOCK_INFO = "info";
    private static final String BLOCK_RECURSIVE_SIZE = "size";

    public DestructibleCom(final Destructible plugin, final Commands commands) {
        this.plugin = plugin;
        this.commands = commands;
    }

    public void register() {
        this.commands.register(Commands
                .literal(command)
                .requires(source -> source.getSender() instanceof Player player && player.isOp())
                .then(Commands.literal(TOOLS)
                        .then(Commands.argument(TOOL_TYPE, StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    String argument = builder.getRemaining();
                                    for(String id : Registries.DESTRUCTIBLE_TOOLS.keySet()) {
                                        if(id.regionMatches(true, 0, argument, 0, argument.length())) {
                                            builder.suggest(id);
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(this::tool))
                )
                .then(Commands.literal(BLOCKS)
                        .then(Commands.literal(BLOCK_MODIFY)
                                .then(Commands.argument(BLOCK_TYPE, StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            String argument = builder.getRemaining();
                                            for(String id : Registries.DESTRUCTIBLE_BLOCKS.keySet()) {
                                                if(id.regionMatches(true, 0, argument, 0, argument.length())) {
                                                    builder.suggest(id);
                                                }
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(this::blockModify)
                                        .then(Commands.argument(BLOCK_RECURSIVE, BoolArgumentType.bool())
                                                .executes(this::blockModifyRec))
                                        .then(Commands.argument(BLOCK_RECURSIVE_SIZE, IntegerArgumentType.integer(1, 65792))
                                                .executes(this::blockModifyRecSize))
                                ).executes(this::endBlockModify))
                        .then(Commands.literal(BLOCK_VIEW)
                                .executes(this::blockView))
                        .then(Commands.literal(BLOCK_SAVE)
                                .executes(this::save))
                        .then(Commands.literal(BLOCK_CLEAR)
                                .executes(this::clear))
                        .then(Commands.literal(BLOCK_INFO)
                                .executes(this::info))
                )
                .build(), alias);
    }

    private int tool(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        String toolStr = context.getArgument(TOOL_TYPE, String.class);
        DTool dTool = Registries.DESTRUCTIBLE_TOOLS.get(toolStr);
        if(dTool == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>An invalid Destructible tool was specified."));
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
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>You have been received Destructible tool: <gold>" + dTool.getId() + "</gold>."));
        return Command.SINGLE_SUCCESS;
    }

    private int endBlockModify(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        DBlockEditor DBlockEditor = Registries.DESTRUCTIBLE_BLOCK_EDITORS.get(player.getUniqueId());

        if(DBlockEditor != null && DBlockEditor.getDblock() != null) {
            if(DBlockEditor.isHighlightLocked()) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Please wait for the current highlight operation to finish."));
                return 0;
            }

            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                DBlockEditor.setLockHighlight(true);
                DBlockEditor.getBlockHighlight().forEach((k, v) -> {
                    v.forEach((k2, v2) ->
                            Bukkit.getScheduler().runTask(this.plugin, () ->
                                    ((CraftPlayer) player).getHandle().connection.send(new ClientboundRemoveEntitiesPacket(v2))));
                    try {
                        Thread.sleep(25);
                    } catch(InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                DBlockEditor.setLockHighlight(false);
            });
            Registries.DESTRUCTIBLE_BLOCK_EDITORS.remove(player.getUniqueId());
            player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>You are no longer in Destructible build mode."));
        } else {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You are not currently in Destructible build mode."));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int blockModify(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        String blockStr = context.getArgument(BLOCK_TYPE, String.class);

        DBlock dBlock = Registries.DESTRUCTIBLE_BLOCKS.get(blockStr);
        if(dBlock == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>An invalid Destructible block was specified."));
            return 0;
        }

        Registries.DESTRUCTIBLE_BLOCK_EDITORS.computeIfAbsent(player.getUniqueId(), v ->
                new DBlockEditor(player)).setDblock(dBlock, false);

        player.sendMessage(MiniMessage.miniMessage().deserialize(
                "<yellow>You have enabled Destructible build mode, all place blocks will be marked as <gold>" + dBlock.getId() + "</gold>."));
        return Command.SINGLE_SUCCESS;
    }

    private int blockModifyRec(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        String blockStr = context.getArgument(BLOCK_TYPE, String.class);
        boolean recursive = context.getArgument(BLOCK_RECURSIVE, Boolean.class);
        int recursionSize = 4096;

        DBlock dBlock = Registries.DESTRUCTIBLE_BLOCKS.get(blockStr);
        if(dBlock == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>An invalid Destructible block was specified."));
            return 0;
        }

        Registries.DESTRUCTIBLE_BLOCK_EDITORS.computeIfAbsent(player.getUniqueId(), v ->
                new DBlockEditor(player)).setDblock(dBlock, recursive, recursionSize);

        player.sendMessage(MiniMessage.miniMessage().deserialize(
                "<yellow>You have enabled Destructible <red><bold>RECURSIVE</red> build mode, <gold>" + recursionSize + "</gold> adjacent blocks will be recursively set to <gold>" + dBlock.getId() + "</gold>."));
        return Command.SINGLE_SUCCESS;
    }

    private int blockModifyRecSize(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        String blockStr = context.getArgument(BLOCK_TYPE, String.class);
        int recursionSize = context.getArgument(BLOCK_RECURSIVE_SIZE, Integer.class);

        DBlock dBlock = Registries.DESTRUCTIBLE_BLOCKS.get(blockStr);
        if(dBlock == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>An invalid Destructible block was specified."));
            return 0;
        }

        Registries.DESTRUCTIBLE_BLOCK_EDITORS.computeIfAbsent(player.getUniqueId(), v ->
                new DBlockEditor(player)).setDblock(dBlock, true, recursionSize);

        player.sendMessage(MiniMessage.miniMessage().deserialize(
                "<yellow>You have enabled Destructible <red><bold>RECURSIVE</red> build mode, <gold>" + recursionSize + "</gold> adjacent blocks will be recursively set to <gold>" + dBlock.getId() + "</gold>."));
        return Command.SINGLE_SUCCESS;
    }

    private int blockView(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        DBlockEditor DBlockEditor = Registries.DESTRUCTIBLE_BLOCK_EDITORS.computeIfAbsent(player.getUniqueId(), v -> new DBlockEditor(player));

        if(DBlockEditor.isHighlightLocked()) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Please wait for the current highlight operation to finish."));
            return 0;
        }

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            if(!DBlockEditor.getBlockHighlight().isEmpty()) {
                DBlockEditor.setLockHighlight(true);
                DBlockEditor.getBlockHighlight().forEach((k, v) -> {
                    v.forEach((k2, v2) -> {
                        serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(v2));
                    });
                    try {
                        Thread.sleep(25);
                    } catch(InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                DBlockEditor.getBlockHighlight().clear();
                DBlockEditor.setLockHighlight(false);
            }

            if(!DBlockEditor.isHighlight()) {
                DBlockEditor.setLockHighlight(true);
                DBlockEditor.setHighlight(true);
                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    serverPlayer.connection.send(new ClientboundUpdateMobEffectPacket(serverPlayer.getId(), new MobEffectInstance(MobEffects.NIGHT_VISION, -1), false));
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>You can now see all Destructible blocks."));
                });
                Registries.DESTRUCTIBLE_BLOCKS_CACHE.forEach((k, v) -> {
                    v.forEach((k2, v2) -> {
                        BlockPos blockPos = CoordUtil.chunkIntToPos(k2, k);
                        CraftEntity entity = EntityUtil.getHighlight(serverPlayer.serverLevel());

                        Bukkit.getScheduler().runTask(this.plugin, () -> {
                            serverPlayer.connection.send(new ClientboundAddEntityPacket(entity.getHandle(), 0, blockPos));
                            serverPlayer.connection.send(new ClientboundSetEntityDataPacket(entity.getEntityId(), entity.getHandle().getEntityData().packAll()));
                        });
                        DBlockEditor.getBlockHighlight()
                                .computeIfAbsent(k, v3 -> new DestructibleRegistry<>())
                                .computeIfAbsent(k2, v4 -> entity.getEntityId());
                    });
                    try {
                        Thread.sleep(25);
                    } catch(InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                DBlockEditor.setLockHighlight(false);
            } else {
                DBlockEditor.setLockHighlight(true);
                DBlockEditor.setHighlight(false);

                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    serverPlayer.connection.send(new ClientboundRemoveMobEffectPacket(serverPlayer.getId(), MobEffects.NIGHT_VISION));
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>You can no longer see Destructible blocks."));
                });
                DBlockEditor.getBlockHighlight().forEach((k, v) -> {
                    v.forEach((k2, v2) -> Bukkit.getScheduler().runTask(this.plugin, () ->
                            serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(v2))));
                    try {
                        Thread.sleep(25);
                    } catch(InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                DBlockEditor.getBlockHighlight().clear();
                DBlockEditor.setLockHighlight(false);
            }
        });
        return Command.SINGLE_SUCCESS;
    }

    private int save(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();

        this.plugin.getdBlockCache().save();
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>All cached Destructible blocks changes have been <green><bold>SAVED</green>!"));
        return Command.SINGLE_SUCCESS;
    }

    private int clear(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        DBlockEditor DBlockEditor = Registries.DESTRUCTIBLE_BLOCK_EDITORS.computeIfAbsent(player.getUniqueId(), v -> new DBlockEditor(player));

        if(DBlockEditor.isHighlightLocked()) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Please wait for the current highlight operation to finish."));
            return 0;
        }

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            Registries.DESTRUCTIBLE_BLOCKS_CACHE.clear();
            DBlockEditor.setLockHighlight(true);
            Registries.DESTRUCTIBLE_BLOCK_EDITORS.forEach((k, v) -> {
                v.getBlockHighlight().forEach((k2, v2) -> {
                    v2.forEach((k3, v3) -> Bukkit.getScheduler().runTask(this.plugin, () -> {
                        Bukkit.getScheduler().runTask(this.plugin, () ->
                                serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(v3)));
                    }));
                    try {
                        Thread.sleep(25);
                    } catch(InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
            Registries.DESTRUCTIBLE_BLOCK_EDITORS.clear();
            DBlockEditor.setLockHighlight(false);
        });
        player.sendMessage(MiniMessage.miniMessage().deserialize(
                "<yellow>All cached Destructible blocks have been <red><bold>CLEARED</red>!"));
        return Command.SINGLE_SUCCESS;
    }

    private int info(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();

        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>There are currently <gold>" + FormatUtil.numberComma(Registries.DESTRUCTIBLE_BLOCKS_CACHE.values().stream().mapToInt(Map::size).sum()) + "</gold> Destructible blocks spread across <gold>" + FormatUtil.numberComma(Registries.DESTRUCTIBLE_BLOCKS_CACHE.size()) + "</gold> chunks."));
        return Command.SINGLE_SUCCESS;
    }
}