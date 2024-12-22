package net.qilla.destructible.command.destructible;

import com.google.common.collect.ArrayListMultimap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.ChunkPos;
import net.qilla.destructible.data.DataKey;
import net.qilla.destructible.data.DestructibleRegistry;
import net.qilla.destructible.data.Registries;
import net.qilla.destructible.mining.block.DBlock;
import net.qilla.destructible.mining.item.tool.DTool;
import net.qilla.destructible.util.CoordUtil;
import net.qilla.destructible.util.EntityUtil;
import net.qilla.destructible.util.ItemUtil;
import org.bukkit.Bukkit;
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
    private static final String[] toolArgs = {"tools", "type"};
    private static final String[] blockArgs = {"blocks", "modify", "type" , "view", "save", "reset"};

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
                                .executes(this::blockView))
                        .then(Commands.literal(blockArgs[4])
                                .executes(this::save))
                        .then(Commands.literal(blockArgs[5])
                                .executes(this::reset))
                )
                .build(), alias);
    }

    private int tool(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        String toolStr = context.getArgument(toolArgs[1], String.class);
        DTool dTool = Registries.DTOOLS.get(toolStr);
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
        return Command.SINGLE_SUCCESS;
    }

    private int endBlockModify(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();

        if(Registries.DBLOCK_EDITOR.containsKey(player.getUniqueId())) {
            Registries.DBLOCK_VIEWER.computeIfPresent(player.getUniqueId(), (k, v) -> {
                v.forEach((k2, v2) -> v2.forEach((k3, v3) -> ((CraftPlayer) player).getHandle().connection.send(new ClientboundRemoveEntitiesPacket(v3))));
                return null;
            });
            Registries.DBLOCK_EDITOR.remove(player.getUniqueId());
            player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>You are no longer modifying Destructible blocks in the world space!"));
        } else {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You are not currently modifying Destructible blocks in the world space."));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int blockModify(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        String blockStr = context.getArgument(blockArgs[2], String.class);
        DBlock dBlock = Registries.DBLOCKS.get(blockStr);
        if(dBlock == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>An invalid Destructible block was specified."));
            return 0;
        }

        Registries.DBLOCK_EDITOR.put(player.getUniqueId(), dBlock);
        Registries.DBLOCK_VIEWER.computeIfAbsent(player.getUniqueId(), k -> new DestructibleRegistry<>());
        player.sendMessage(MiniMessage.miniMessage().deserialize(
                "<yellow>You are now modifying Destructible blocks in the world space!\n" +
                "<green>Any placed blocks will be registered as " + dBlock.getId() + ", destroyed blocks will be unregistered."));
        return Command.SINGLE_SUCCESS;
    }

    private int blockView(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();


        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            DestructibleRegistry<ChunkPos, DestructibleRegistry<Integer, Integer>> viewMap = Registries.DBLOCK_VIEWER.get(player.getUniqueId());

            if(viewMap == null) {
                DestructibleRegistry<ChunkPos, DestructibleRegistry<Integer, Integer>> viewerMap = new DestructibleRegistry<>();
                Registries.DBLOCK_CACHE.forEach((k, v) -> {
                    v.forEach((k2, v2) -> {
                        BlockPos blockPos = CoordUtil.chunkIntToPos(k2, k);
                        Entity entity = EntityUtil.getHighlight(serverPlayer.serverLevel());

                        try {
                            Thread.sleep(1);
                        } catch(InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        Bukkit.getScheduler().runTask(this.plugin, () -> {
                            serverPlayer.connection.send(new ClientboundAddEntityPacket(entity, 0, blockPos));
                            serverPlayer.connection.send(new ClientboundSetEntityDataPacket(entity.getId(), entity.getEntityData().packAll()));
                        });
                        viewerMap.computeIfAbsent(k, v3 -> new DestructibleRegistry<>()).computeIfAbsent(k2, v4 -> entity.getId());
                    });
                });
                Registries.DBLOCK_VIEWER.put(player.getUniqueId(), viewerMap);
                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    serverPlayer.connection.send(new ClientboundUpdateMobEffectPacket(serverPlayer.getId(), new MobEffectInstance(MobEffects.NIGHT_VISION, -1), false));
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>You are now viewing Destructible Blocks in the world space."));
                });
            } else {
                viewMap.forEach((k, v) -> v.forEach((k2, v2) -> serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(v2))));
                Registries.DBLOCK_VIEWER.remove(player.getUniqueId());
                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    serverPlayer.connection.send(new ClientboundRemoveMobEffectPacket(serverPlayer.getId(), MobEffects.NIGHT_VISION));
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>You are no longer viewing Destructible Blocks in the world space."));
                });
            }
        });
        return Command.SINGLE_SUCCESS;
    }

    private int save(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();

        this.plugin.getdBlockCache().save();
        player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>All cached Destructible blocks have been <green><bold>SAVED</green>!"));
        return Command.SINGLE_SUCCESS;
    }

    private int reset(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();

        Registries.DBLOCK_CACHE.clear();
        Registries.DBLOCK_VIEWER.forEach((k, v) -> v.forEach((k2, v2) -> v2.forEach((k3, v3) -> serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(v3)))));
        Registries.DBLOCK_VIEWER.clear();
        player.sendMessage(MiniMessage.miniMessage().deserialize(
                "<yellow>All cached Destructible blocks have been <red><bold>DELETED</red>!" +
                        "\n<green>Use the save subcommand to save changes."));
        return Command.SINGLE_SUCCESS;
    }
}