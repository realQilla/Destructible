package net.qilla.destructible.command.temp;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.DSounds;
import net.qilla.destructible.data.registry.DPlayerDataRegistry;
import net.qilla.destructible.data.registry.DRegistry;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.player.DPlayerData;
import net.qilla.destructible.util.NMSUtil;
import net.qilla.qlibrary.util.tools.CoordUtil;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.craftbukkit.entity.CraftBlockDisplay;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class SelectCommand {

    private static final DPlayerDataRegistry PLAYER_DATA_REGISTRY = DPlayerDataRegistry.getInstance();
    private static final Map<UUID, SelectedArea> SELECTED_AREA = new HashMap<>();
    private static final Map<UUID, Set<CraftEntity>> SAVED_ENTITIES = new HashMap<>();
    private static final String COMMAND = "select";
    private static final List<String> ALIAS = List.of("sel");

    private static final String FIRST = "first";
    private static final String SECOND = "second";
    private static final String VIEW = "view";

    private static final int DISTANCE = 50;

    private final Destructible plugin;
    private final Commands commands;

    public SelectCommand(final Destructible plugin, final Commands commands) {
        this.plugin = plugin;
        this.commands = commands;
    }

    public void register() {
        this.commands.register(Commands.literal(COMMAND)
                .requires(source -> source.getSender() instanceof Player)
                .then(Commands.literal(FIRST)
                        .executes(this::selectFirst))
                .then(Commands.literal(SECOND)
                        .executes(this::selectSecond))
                .then(Commands.literal(VIEW)
                        .executes(this::view))
                .build(), ALIAS);
    }

    private int selectFirst(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        DPlayerData playerData = PLAYER_DATA_REGISTRY.getData(player);
        DPlayer dPlayer = playerData.getPlayer();

        RayTraceResult rayTrace = playerData.getPlayer().getWorld().rayTraceBlocks(
                dPlayer.getEyeLocation(), dPlayer.getEyeLocation().getDirection(), DISTANCE, FluidCollisionMode.NEVER, true);

        if(rayTrace == null || rayTrace.getHitBlock() == null) {
            dPlayer.sendMessage("Raytrace failed, try again.");
            return 0;
        }
        BlockPos blockPos = CoordUtil.getBlockPos(rayTrace.getHitBlock());

        SELECTED_AREA.computeIfAbsent(dPlayer.getUniqueId(), k -> new SelectedArea()).first(blockPos);
        this.createTempEntity(dPlayer, blockPos);
        dPlayer.sendMessage("<yellow>Raytrace completed!");
        dPlayer.playSound(DSounds.GENERAL_SUCCESS_2, true);
        return Command.SINGLE_SUCCESS;
    }

    private int selectSecond(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        DPlayerData playerData = PLAYER_DATA_REGISTRY.getData(player);
        DPlayer dPlayer = playerData.getPlayer();

        RayTraceResult rayTrace = playerData.getPlayer().getWorld().rayTraceBlocks(
                dPlayer.getEyeLocation(), dPlayer.getEyeLocation().getDirection(), DISTANCE, FluidCollisionMode.NEVER, true);

        if(rayTrace == null || rayTrace.getHitBlock() == null) {
            dPlayer.sendMessage("Raytrace failed, try again.");
            return 0;
        }
        BlockPos blockPos = CoordUtil.getBlockPos(rayTrace.getHitBlock());

        SELECTED_AREA.computeIfAbsent(dPlayer.getUniqueId(), k -> new SelectedArea()).second(blockPos);
        this.createTempEntity(dPlayer, blockPos);
        dPlayer.sendMessage("<yellow>Raytrace completed!");
        dPlayer.playSound(DSounds.GENERAL_SUCCESS_2, true);
        return Command.SINGLE_SUCCESS;
    }

    private int view(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        DPlayerData playerData = PLAYER_DATA_REGISTRY.getData(player);
        DPlayer dPlayer = playerData.getPlayer();

        if(SELECTED_AREA.computeIfPresent(dPlayer.getUniqueId(), (k, v) -> v.isSelected() ? v : null) == null) {
            dPlayer.sendMessage("<red>You must first make two selections!");
            dPlayer.playSound(DSounds.GENERAL_ERROR, true);
            return 0;
        }

        SAVED_ENTITIES.compute(dPlayer.getUniqueId(), (k, v) -> {
            if(v == null) return this.createCuboid(dPlayer, SELECTED_AREA.get(dPlayer.getUniqueId()));
            else {
                v.forEach(entity -> dPlayer.sendPacket(new ClientboundRemoveEntitiesPacket(entity.getEntityId())));
                return null;
            }
        });
        return Command.SINGLE_SUCCESS;
    }

    private void createTempEntity(DPlayer player, BlockPos blockPos) {
        CraftEntity entity = NMSUtil.createHighlightEntity(player.getWorld());

        player.sendPacket(new ClientboundAddEntityPacket(entity.getHandle(), 0, blockPos));
        player.sendPacket(new ClientboundSetEntityDataPacket(entity.getEntityId(), entity.getHandle().getEntityData().packAll()));

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            player.sendPacket(new ClientboundRemoveEntitiesPacket(entity.getEntityId()));
        }, 80);
    }

    private @NotNull Set<CraftEntity> createCuboid(@NotNull DPlayer player, @NotNull SelectedArea area) {
        BlockPos first = area.getFirst();
        BlockPos second = area.getSecond();

        int minX = Math.min(first.getX(), second.getX());
        int maxX = Math.max(first.getX(), second.getX());

        int minY = Math.min(first.getY(), second.getY());
        int maxY = Math.max(first.getY(), second.getY());

        int minZ = Math.min(first.getZ(), second.getZ());
        int maxZ = Math.max(first.getZ(), second.getZ());

        int xDist = maxX - minX + 1;
        int yDist = maxY - minY + 1;
        int zDist = maxZ - minZ + 1;

        Set<CraftEntity> displaySet = new HashSet<>();
        for(int i = 0; i < 12; i++) {
            final Vector3f position = this.getPositions(xDist, yDist, zDist)[i];
            final Vector3f size = this.getSizes(xDist, yDist, zDist)[i];

            CraftBlockDisplay display =  NMSUtil.createHighlightEntity(player.getWorld());
            display.setTransformation(new Transformation(
                    position,
                    new Quaternionf(),
                    size,
                    new Quaternionf()));
            displaySet.add(display);

            BlockPos blockPos = new BlockPos(area.getFirst().getX(), area.getFirst().getY(), area.getFirst().getZ());
            player.sendPacket(new ClientboundAddEntityPacket(display.getHandle(), display.getEntityId(), blockPos));
            player.sendPacket(new ClientboundSetEntityDataPacket(display.getHandle().getId(), display.getHandle().getEntityData().packAll()));
        }
        return displaySet;
    }

    private CraftEntity createEntity(DPlayer player, BlockPos pos) {
        CraftBlockDisplay display =  NMSUtil.createHighlightEntity(player.getWorld());
        display.setTransformation(new Transformation(
                new Vector3f(pos.getX(), pos.getY(), pos.getZ()),
                new Quaternionf(),
                new Vector3f(0.05f, 0.05f, 0.05f),
                new Quaternionf()));
        display.setGlowing(true);
        display.setInvisible(true);
        display.setCustomNameVisible(true);

        return display;
    }

    public @NotNull Vector3f[] getPositions(int xDistance, int yDistance, int zDistance) {
        return new Vector3f[]{new Vector3f(0.05f, yDistance - 0.05f, 0), // Top Left Front
                new Vector3f(0, yDistance - 0.05f, 0.05f), // Top Right Front
                new Vector3f(xDistance - 0.05f, yDistance - 0.05f, 0.05f), // Top Left Back
                new Vector3f(0.05f, yDistance - 0.05f, zDistance - 0.05f), // Top Right Back

                new Vector3f(0.05f, 0, 0), // Bottom Left Front
                new Vector3f(0, 0, 0.05f), // Bottom Right Front
                new Vector3f(xDistance - 0.05f, 0, 0.05f), // Bottom Left Back
                new Vector3f(0.05f, 0, zDistance - 0.05f), // Bottom Right Back

                new Vector3f(xDistance - 0.05f, 0.05f, 0), //Left Edge
                new Vector3f(0, 0.05f, zDistance - 0.05f), // Right Edge
                new Vector3f(0, 0.05f, 0), // Front Edge
                new Vector3f(xDistance - 0.05f, 0.05f, zDistance - 0.05f) // Back Edge
        };
    }

    private static final float EDGE_SIZE = 0.05f;

    public @NotNull Vector3f[] getSizes(int xDistance, int yDistance, int zDistance) {
        return new Vector3f[]{new Vector3f(xDistance - 0.1f, EDGE_SIZE, EDGE_SIZE), // Top Left Front
                new Vector3f(EDGE_SIZE, EDGE_SIZE, zDistance - 0.1f), // Top Right Front
                new Vector3f(EDGE_SIZE, EDGE_SIZE, zDistance - 0.1f), // Top Left Back
                new Vector3f(xDistance - 0.1f, EDGE_SIZE, EDGE_SIZE), // Top Right Back

                new Vector3f(xDistance - 0.1f, EDGE_SIZE, EDGE_SIZE), // Bottom Left Front
                new Vector3f(EDGE_SIZE, EDGE_SIZE, zDistance - 0.1f), // Bottom Right Front
                new Vector3f(EDGE_SIZE, EDGE_SIZE, zDistance - 0.1f), // Bottom Right Back
                new Vector3f(xDistance - 0.1f, EDGE_SIZE, EDGE_SIZE), // Bottom Right Back

                new Vector3f(EDGE_SIZE, yDistance - 0.1f, EDGE_SIZE), // Left Edge
                new Vector3f(EDGE_SIZE, yDistance - 0.1f, EDGE_SIZE), // Right Edge
                new Vector3f(EDGE_SIZE, yDistance - 0.1f, EDGE_SIZE), // Front Edge
                new Vector3f(EDGE_SIZE, yDistance - 0.1f, EDGE_SIZE), // Back Edge
        };
    }
}