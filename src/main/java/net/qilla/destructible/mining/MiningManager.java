package net.qilla.destructible.mining;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.qilla.destructible.data.ChunkPos;
import net.qilla.destructible.mining.item.tool.DTool;
import net.qilla.destructible.util.CoordUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MiningManager {

    private final BlockMiner blockMiner;
    private final ToolManager toolManager;
    private volatile DData dData;

    public MiningManager(@NotNull BlockMiner blockMiner, @NotNull ToolManager toolManager) {
        this.blockMiner = blockMiner;
        this.toolManager = toolManager;
    }

    public void init(@NotNull Player player, @NotNull BlockPos blockPos, @NotNull Direction direction) {
        if(player.getGameMode() == GameMode.CREATIVE) return;

        if(this.dData == null || blockPos.hashCode() != this.dData.getBlockPos().hashCode()) {
            this.dData = new DData(player.getWorld(), blockPos, new ChunkPos(blockPos), CoordUtil.posToChunkLocalPos(blockPos), direction);
            this.dData.setDBlock(new BlockManager().getDBlock(this.dData));
        }
    }

    public void tickBlock(@NotNull InteractionHand interactionHand) {
        if(this.dData == null || this.dData.getDBlock().getDurability() < 0 || !interactionHand.equals(InteractionHand.MAIN_HAND)) return;

        DTool dTool = this.toolManager.getDTool();

        if(this.toolManager.onCoolDown(this.dData)) return;
        if(!this.toolManager.canMine(dTool, this.dData)) {
            this.blockMiner.nonMineable(this.dData);
            return;
        } else {
            this.blockMiner.isMineable(this.dData);
        }
        this.blockMiner.tickBlock(this.dData, dTool, toolManager);
    }

    public void stop() {
        if(this.dData == null) return;
        this.blockMiner.endProgress(this.dData);
        this.dData = null;
    }
}
