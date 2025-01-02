package net.qilla.destructible.mining.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.qilla.destructible.mining.BlockInstance;
import net.qilla.destructible.data.ChunkPos;
import net.qilla.destructible.mining.item.DItem;
import net.qilla.destructible.mining.item.DItemStack;
import net.qilla.destructible.mining.item.DTool;
import net.qilla.destructible.player.DPlayer;
import net.qilla.destructible.util.CoordUtil;
import net.qilla.destructible.util.DBlockUtil;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

public class MiningManager {

    private final DPlayer dPlayer;
    private final BlockMiner blockMiner;
    private final ToolManager toolManager;
    private volatile BlockInstance blockInstance;

    public MiningManager(@NotNull DPlayer dPlayer) {
        this.dPlayer = dPlayer;
        this.blockMiner = new BlockMiner(dPlayer);
        this.toolManager = new ToolManager(dPlayer);
    }

    public void init(@NotNull BlockPos blockPos, @NotNull Direction direction) {

        if(dPlayer.getGameMode() == GameMode.CREATIVE || this.blockInstance == null || blockPos.hashCode() != this.blockInstance.getBlockPos().hashCode()) {
            this.blockInstance = new BlockInstance(dPlayer.getWorld(), blockPos, new ChunkPos(blockPos), CoordUtil.posToChunkLocalPos(blockPos), direction);
            this.blockInstance.setDBlock(DBlockUtil.getDBlock(this.blockInstance.getChunkPos(), this.blockInstance.getChunkInt()));
        }
    }

    public void tickBlock(@NotNull InteractionHand interactionHand) {
        if(this.blockInstance == null || this.blockInstance.getDBlock().getDurability() < 0 || !interactionHand.equals(InteractionHand.MAIN_HAND))
            return;

        DItem dItem = DItemStack.getDItem(this.dPlayer.getEquipment().getItemInMainHand());
        if(!(dItem instanceof DTool dTool)) return;

        if(!this.toolManager.canMine(dTool, this.blockInstance)) return;
        if(toolManager.isToolBroken()) return;
        this.blockMiner.tickBlock(this.blockInstance, dTool, toolManager);
    }

    public void stop() {
        if(this.blockInstance == null) return;
        this.blockMiner.endProgress(this.blockInstance);
        this.blockInstance = null;
    }
}