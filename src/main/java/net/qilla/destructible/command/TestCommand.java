package net.qilla.destructible.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.qilla.destructible.Destructible;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class TestCommand {

    private final Destructible plugin;
    private final Commands commands;

    private final String command = "test";
    private final List<String> alias = List.of();

    public TestCommand(final Destructible plugin, final Commands commands) {
        this.plugin = plugin;
        this.commands = commands;
    }

    public void register() {
        this.commands.register(Commands
                .literal(command)
                .requires(source -> source.getSender() instanceof Player player && player.isOp())
                        .executes(this::test)
                .build());

    }

    private int test(CommandContext<CommandSourceStack> context) {
        if(!(context.getSource().getSender() instanceof Player player)) return 0;
        final Location loc = player.getLocation();
        final ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        final ServerLevel level = (ServerLevel) serverPlayer.level();

        final BlockPos blockPos = new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        final ItemStack itemStack = new ItemStack(Items.COBBLESTONE);
        final ItemEntity itemEntity = new ItemEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack.copy());

        level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundAddEntityPacket(itemEntity.getId(), itemEntity.getUUID(), itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), 0, 0,  itemEntity.getType(), 0, new Vec3(0,0,0), 0));
        level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundSetEntityDataPacket(itemEntity.getId(), itemEntity.getEntityData().packAll()));
        level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundTakeItemEntityPacket(itemEntity.getId(), serverPlayer.getId(), 1));
        player.sendMessage("Command Success!");
        return Command.SINGLE_SUCCESS;
    }
}
