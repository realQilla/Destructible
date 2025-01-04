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

        if(dPlayer.getCraftPlayer().getGameMode() == GameMode.CREATIVE || blockInstance == null || blockPos.hashCode() != blockInstance.getBlockPos().hashCode()) {
            blockInstance = new BlockInstance(dPlayer.getCraftPlayer().getWorld(), blockPos, new ChunkPos(blockPos), CoordUtil.posToChunkLocalPos(blockPos), direction);
            blockInstance.setDBlock(DBlockUtil.getDBlock(blockInstance.getChunkPos(), blockInstance.getChunkInt()));
        }
    }

    public void tickBlock(@NotNull InteractionHand interactionHand) {
        if(blockInstance == null || blockInstance.getDBlock().getBlockDurability() < 0 ||
                !interactionHand.equals(InteractionHand.MAIN_HAND)) return;

        DItem dItem = DItemStack.getDItem(dPlayer.getCraftPlayer().getEquipment().getItemInMainHand());
        if(!(dItem instanceof DTool dTool)) return;

        if(!toolManager.canMine(dTool, blockInstance)) return;
        if(toolManager.isToolBroken()) return;
        blockMiner.tickBlock(blockInstance, dTool, toolManager);
    }

    public void stop() {
        if(blockInstance == null) return;
        blockMiner.endProgress(blockInstance);
        blockInstance = null;
    }
}