package net.qilla.destructible.mining;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.qilla.destructible.Destructible;
import net.qilla.destructible.data.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Necessary player related data.
 */
public final class MiningCore {

    private final Destructible plugin;
    private final Player player;
    private final MiningManager miningManager;

    public MiningCore(@NotNull Destructible plugin, @NotNull Player player, @NotNull Equipment equipment) {
        this.plugin = plugin;
        this.player = player;
        this.miningManager = new MiningManager(new BlockMiner(plugin, player), new ToolManager(player, equipment));
    }

    public void init(@NotNull BlockPos blockPos, @NotNull Direction direction) {
        this.miningManager.init(player, blockPos, direction);
    }

    public void tickBlock(final InteractionHand interactionHand) {
        this.miningManager.tickBlock(interactionHand);
    }

    public void stop() {
        this.miningManager.stop();
    }
}