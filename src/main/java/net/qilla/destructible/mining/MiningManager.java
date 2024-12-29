package net.qilla.destructible.mining;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.qilla.destructible.data.ChunkPos;
import net.qilla.destructible.mining.item.DTool;
import net.qilla.destructible.util.CoordUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MiningManager {

    private final BlockMiner blockMiner;
    private final ToolManager toolManager;
    private volatile BlockInstance blockInstance;

    public MiningManager(@NotNull BlockMiner blockMiner, @NotNull ToolManager toolManager) {
        this.blockMiner = blockMiner;
        this.toolManager = toolManager;
    }

    public void init(@NotNull Player player, @NotNull BlockPos blockPos, @NotNull Direction direction) {

        if(player.getGameMode() == GameMode.CREATIVE ||
                this.blockInstance == null ||
                blockPos.hashCode() != this.blockInstance.getBlockPos().hashCode()) {
            this.blockInstance = new BlockInstance(player.getWorld(), blockPos, new ChunkPos(blockPos), CoordUtil.posToChunkLocalPos(blockPos), direction);
            this.blockInstance.setDBlock(new BlockManager().getDBlock(this.blockInstance));
        }
    }

    public void tickBlock(@NotNull InteractionHand interactionHand) {
        if(this.blockInstance == null || this.blockInstance.getDBlock().getDurability() < 0 || !interactionHand.equals(InteractionHand.MAIN_HAND)) return;

        DTool dTool = this.toolManager.getDTool();

        if(!this.toolManager.canMine(dTool, this.blockInstance)) return;
        this.blockMiner.tickBlock(this.blockInstance, dTool, toolManager);
    }

    public void stop() {
        if(this.blockInstance == null) return;
        this.blockMiner.endProgress(this.blockInstance);
        this.blockInstance = null;
    }
}
